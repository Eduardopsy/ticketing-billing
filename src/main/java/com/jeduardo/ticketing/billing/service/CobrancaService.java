package com.jeduardo.ticketing.billing.service;

import com.jeduardo.ticketing.billing.domain.entities.Cobranca;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaMetodoEnum;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaStatusEnum;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaTipoEnum;
import com.jeduardo.ticketing.billing.dto.*;
import com.jeduardo.ticketing.billing.exception.NegocioException;
import com.jeduardo.ticketing.billing.exception.RecursoNaoEncontradoException;
import com.jeduardo.ticketing.billing.integration.CheckoutValidationClient;
import com.jeduardo.ticketing.billing.integration.StatusConsultaExternaClient;
import com.jeduardo.ticketing.billing.integration.UserContext;
import com.jeduardo.ticketing.billing.lock.LockExecutor;
import com.jeduardo.ticketing.billing.repository.CobrancaRepository;
import com.jeduardo.ticketing.billing.service.strategy.CobrancaCriacaoStrategyRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CobrancaService {

    private static final ZoneId ZONE_SP = ZoneId.of("America/Sao_Paulo");
    private static final Set<CobrancaStatusEnum> STATUS_CONSULTA_EXTERNA =
            Set.of(CobrancaStatusEnum.SOLICITADA, CobrancaStatusEnum.EXPIRADA,
                    CobrancaStatusEnum.ERRO_APROVACAO_PEDIDO, CobrancaStatusEnum.EM_REPROCESSAMENTO,
                    CobrancaStatusEnum.ERRO_ANALISE_PENDENTE);

    private final CobrancaRepository repository;
    private final LockExecutor lockExecutor;
    private final CobrancaCriacaoStrategyRegistry strategyRegistry;
    private final CheckoutValidationClient checkoutValidationClient;
    private final StatusConsultaExternaClient statusConsultaExternaClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Cobranca criarCobranca(CobrancaRequestDTO request, UserContext userContext) {
        String lockKey = "cobrancas:" + userContext.getIdUsuario();

        return lockExecutor.executeWithLock(lockKey, 5, () -> {
            try {
                CobrancaMetodoEnum metodo = request.getMetodo() != null
                        ? request.getMetodo() : CobrancaMetodoEnum.PIX;
                CobrancaTipoEnum tipo = request.getTipo() != null
                        ? request.getTipo() : CobrancaTipoEnum.RECARGA;

                Cobranca cobranca = Cobranca.builder()
                        .idUsuario(userContext.getIdUsuario())
                        .nomeSolicitante(userContext.getGivenName() + " " + userContext.getFamilyName())
                        .valorSolicitacao(request.getValor())
                        .metodo(metodo)
                        .tipo(tipo)
                        .status(CobrancaStatusEnum.SOLICITADA)
                        .dataCriacao(LocalDateTime.now(ZONE_SP))
                        .build();

                strategyRegistry.getStrategy(metodo).aplicar(cobranca);

                Cobranca saved = repository.save(cobranca);

                kafkaTemplate.send("cobranca.criada", saved.getId().toString(), saved.getId());
                log.info("Cobranca criada com sucesso. id={}", saved.getId());

                return saved;
            } catch (NegocioException e) {
                throw e;
            } catch (Exception e) {
                log.error("Erro inesperado ao criar cobrança", e);
                throw new NegocioException("Erro ao criar cobranca", e);
            }
        });
    }

    @Transactional
    public CobrancaCompletoResponseDTO consultarPorId(Long id) {

        Cobranca cobranca = repository.findTopByIdOrderByIdDesc(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cobrança nao encontrada para id: " + id));

        if (cobranca.getMetodo() == CobrancaMetodoEnum.PIX
                && STATUS_CONSULTA_EXTERNA.contains(cobranca.getStatus())) {

            cobranca = consultarEAtualizarStatusExterno(cobranca);
        }

        return toCompletoResponse(cobranca);
    }


    @Transactional
    public void processarNotificacaoWebhookPix(PixWebhookDTO webhook) {
        if (webhook == null || CollectionUtils.isEmpty(webhook.getPix())) {
            log.info("Webhook PIX recebido vazio ou nulo. Ignorando.");
            return;
        }

        for (PixWebhookDTO.PixItem item : webhook.getPix()) {
            processarItemPix(item);
        }
    }

    @Transactional
    public void validarCheckout(String transactionId, CheckoutValidacaoRequestDTO request) {
        Cobranca cobranca = repository.findTopByTransactionIdOrderByIdDesc(transactionId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cobrança não encontrada para transactionId: " + transactionId));

        CheckoutValidacaoResponseDTO validacao = checkoutValidationClient.validar(request);

        cobranca.setTransactionId(validacao.getTransactionId());
        cobranca.setAcsUrl(validacao.getAcsUrl());
        cobranca.setThreeDsPayload(validacao.getThreeDsPayload());

        repository.save(cobranca);
        log.info("Checkout validado para transactionId={}", transactionId);
    }

    private void processarItemPix(PixWebhookDTO.PixItem item) {
        if (item.getTxid() == null || item.getTxid().isBlank()) {
            log.warn("Item PIX com txid vazio. Ignorando.");
            return;
        }

        repository.findTopByTxidOrderByIdDesc(item.getTxid())
                .ifPresentOrElse(cobranca -> {
            if (cobranca.getStatus() == CobrancaStatusEnum.FINALIZADA) {
                log.info("Cobrança txid={} já está FINALIZADA. Ignorando.", item.getTxid());
                return;
            }

            Cobranca novaVersao = criarNovaVersao(cobranca);
            novaVersao.setStatus(CobrancaStatusEnum.FINALIZADA);
            novaVersao.setValorPago(item.getValor());
            novaVersao.setDataFinalizada(LocalDateTime.now(ZONE_SP));

            repository.save(novaVersao);
            log.info("Cobrança finalizada pelo webhook PIX. txid={}", item.getTxid());

            kafkaTemplate.send("cobranca.finalizada", item.getTxid(), novaVersao.getId());

        }, () -> log.warn("Cobrança não encontrada com txid={}. Ignorando.", item.getTxid()));
    }

    private Cobranca consultarEAtualizarStatusExterno(Cobranca cobranca) {
        CobrancaStatusEnum statusExterno = statusConsultaExternaClient.consultarStatus(cobranca.getTxid());

        if (statusExterno != null && statusExterno != cobranca.getStatus()) {
            Cobranca novaVersao = criarNovaVersao(cobranca);
            novaVersao.setStatus(statusExterno);
            return repository.save(novaVersao);
        }

        return cobranca;
    }

    private Cobranca criarNovaVersao(Cobranca original) {
        return Cobranca.builder()
                .idUsuario(original.getIdUsuario())
                .nomeSolicitante(original.getNomeSolicitante())
                .tipo(original.getTipo())
                .metodo(original.getMetodo())
                .status(original.getStatus())
                .valorSolicitacao(original.getValorSolicitacao())
                .txid(original.getTxid())
                .copiaEcola(original.getCopiaEcola())
                .transactionId(original.getTransactionId())
                .acsUrl(original.getAcsUrl())
                .threeDsPayload(original.getThreeDsPayload())
                .dataCriacao(original.getDataCriacao())
                .dataExpiracao(original.getDataExpiracao())
                .cobrancaPai(original)
                .build();
    }

    private CobrancaCompletoResponseDTO toCompletoResponse(Cobranca c) {
        return CobrancaCompletoResponseDTO.builder()
                .id(c.getId())
                .txid(c.getTxid())
                .idUsuario(c.getIdUsuario())
                .tipo(c.getTipo())
                .metodo(c.getMetodo())
                .status(c.getStatus())
                .valorSolicitado(c.getValorSolicitacao())
                .valorPago(c.getValorPago())
                .dataCriacao(c.getDataCriacao())
                .dataExpiracao(c.getDataExpiracao())
                .dataFinalizada(c.getDataFinalizada())
                .build();
    }
}