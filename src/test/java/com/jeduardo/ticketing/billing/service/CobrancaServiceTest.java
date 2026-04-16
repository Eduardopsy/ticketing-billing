package com.jeduardo.ticketing.billing.service;

import com.jeduardo.ticketing.billing.domain.entities.Cobranca;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaMetodoEnum;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaStatusEnum;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaTipoEnum;
import com.jeduardo.ticketing.billing.dto.CobrancaRequestDTO;
import com.jeduardo.ticketing.billing.dto.PixWebhookDTO;
import com.jeduardo.ticketing.billing.exception.NegocioException;
import com.jeduardo.ticketing.billing.exception.RecursoNaoEncontradoException;
import com.jeduardo.ticketing.billing.integration.CheckoutValidationClient;
import com.jeduardo.ticketing.billing.integration.StatusConsultaExternaClient;
import com.jeduardo.ticketing.billing.integration.UserContext;
import com.jeduardo.ticketing.billing.lock.LockExecutor;
import com.jeduardo.ticketing.billing.repository.CobrancaRepository;
import com.jeduardo.ticketing.billing.service.strategy.CobrancaCriacaoStrategy;
import com.jeduardo.ticketing.billing.service.strategy.CobrancaCriacaoStrategyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CobrancaService - Testes Unitários")
class CobrancaServiceTest {

    @Mock private CobrancaRepository repository;
    @Mock private LockExecutor lockExecutor;
    @Mock private CobrancaCriacaoStrategyRegistry strategyRegistry;
    @Mock private CheckoutValidationClient checkoutValidationClient;
    @Mock private StatusConsultaExternaClient statusConsultaExternaClient;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock private CobrancaCriacaoStrategy pixStrategy;

    @InjectMocks
    private CobrancaService cobrancaService;

    private UserContext userContext;

    @BeforeEach
    void setUp() {
        userContext = UserContext.builder()
                .idUsuario("user-1")
                .givenName("João")
                .familyName("Eduardo")
                .cpf("000.000.000-00")
                .build();
    }

    // Cenário 1 - Criação PIX com sucesso
    @Test
    @DisplayName("1. criarCobranca - sucesso PIX")
    void criarCobranca_pixComSucesso() {
        CobrancaRequestDTO request = new CobrancaRequestDTO();
        request.setValor(new BigDecimal("25.50"));
        request.setMetodo(CobrancaMetodoEnum.PIX);
        request.setTipo(CobrancaTipoEnum.RECARGA);

        Cobranca savedCobranca = Cobranca.builder()
                .id(1L)
                .idUsuario("user-1")
                .status(CobrancaStatusEnum.SOLICITADA)
                .metodo(CobrancaMetodoEnum.PIX)
                .build();

        when(strategyRegistry.getStrategy(CobrancaMetodoEnum.PIX)).thenReturn(pixStrategy);
        when(repository.save(any(Cobranca.class))).thenReturn(savedCobranca);
        when(lockExecutor.executeWithLock(anyString(), anyLong(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(2)).get());

        Cobranca result = cobrancaService.criarCobranca(request, userContext);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(CobrancaStatusEnum.SOLICITADA);

        verify(repository, times(1)).save(any(Cobranca.class));
        verify(pixStrategy, times(1)).aplicar(any(Cobranca.class));
        verify(kafkaTemplate, times(1)).send(eq("cobranca.criada"), anyString(), any());
    }


    // Cenário 2 - Lock indisponível
    @Test
    @DisplayName("2. criarCobranca - lock indisponível lança NegocioException")
    void criarCobranca_lockIndisponivel_lancaNegocioException() {
        CobrancaRequestDTO request = new CobrancaRequestDTO();
        request.setValor(new BigDecimal("25.50"));

        when(lockExecutor.executeWithLock(anyString(), anyLong(), any()))
                .thenThrow(new NegocioException("Geracao de cobranca em andamento."));

        assertThatThrownBy(() -> cobrancaService.criarCobranca(request, userContext))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Geracao de cobranca em andamento.");

        verify(repository, never()).save(any());
    }

    // Cenário 3 - Excecão inesperada mapeada para NegocioException
    @Test
    @DisplayName("3. criarCobranca - exceção inesperada mapeada para NegocioException")
    void criarCobranca_excecaoInesperada_mapeiaParaNegocioException() {
        CobrancaRequestDTO request = new CobrancaRequestDTO();
        request.setValor(new BigDecimal("25.50"));
        request.setMetodo(CobrancaMetodoEnum.PIX);

        when(lockExecutor.executeWithLock(anyString(), anyLong(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(2)).get());
        when(strategyRegistry.getStrategy(any())).thenReturn(pixStrategy);
        when(repository.save(any())).thenThrow(new RuntimeException("DB down"));

        assertThatThrownBy(() -> cobrancaService.criarCobranca(request, userContext))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("Erro ao criar cobranca");
    }

    // Cenário 4 - Webhook PIX finaliza cobranca pendente
    @Test
    @DisplayName("4. processarNotificacaoWebhookPix - finaliza cobrança pendente")
    void processarWebhookPix_finalizaCobrancaPendente() {
        Cobranca cobrancaExistente = Cobranca.builder()
                .id(1L)
                .txid("txid-abc123")
                .status(CobrancaStatusEnum.SOLICITADA)
                .idUsuario("user-1")
                .nomeSolicitante("João Eduardo")
                .metodo(CobrancaMetodoEnum.PIX)
                .tipo(CobrancaTipoEnum.RECARGA)
                .valorSolicitacao(new BigDecimal("25.50"))
                .dataCriacao(LocalDateTime.now())
                .build();

        PixWebhookDTO.PixItem item = new PixWebhookDTO.PixItem();
        item.setTxid("txid-abc123");
        item.setValor(new BigDecimal("25.50"));
        item.setHorario(LocalDateTime.now());

        PixWebhookDTO webhook = new PixWebhookDTO();
        webhook.setPix(List.of(item));

        when(repository.findTopByTxidOrderByIdDesc("txid-abc123"))
                .thenReturn(Optional.of(cobrancaExistente));
        when(repository.save(any(Cobranca.class))).thenAnswer(inv -> inv.getArgument(0));

        cobrancaService.processarNotificacaoWebhookPix(webhook);

        verify(repository, times(1)).save(argThat(c ->
                c.getStatus() == CobrancaStatusEnum.FINALIZADA
                        && c.getValorPago().compareTo(new BigDecimal("25.50")) == 0
                        && c.getDataFinalizada() != null
                        && c.getCobrancaPai() == cobrancaExistente
        ));
    }

    // Cenário 5 - Webhook PIX ignora cobrança finalizada
    @Test
    @DisplayName("5. processarNotificacaoWebhookPix - ignora cobrança já FINALIZADA")
    void processarWebhookPix_ignoraCobrancaJaFinalizada() {
        Cobranca cobrancaFinalizada = Cobranca.builder()
                .id(1L)
                .txid("txid-xyz")
                .status(CobrancaStatusEnum.FINALIZADA)
                .build();

        PixWebhookDTO.PixItem item = new PixWebhookDTO.PixItem();
        item.setTxid("txid-xyz");
        item.setValor(new BigDecimal("10.00"));

        PixWebhookDTO webhook = new PixWebhookDTO();
        webhook.setPix(List.of(item));

        when(repository.findTopByTxidOrderByIdDesc("txid-xyz"))
                .thenReturn(Optional.of(cobrancaFinalizada));

        cobrancaService.processarNotificacaoWebhookPix(webhook);

        verify(repository, never()).save(any());
    }

    // Cenário 6 - validarCheckout atualiza dados de autorização
    @Test
    @DisplayName("6. validarCheckout - atualiza cobrança existente com dados de autorização")
    void validarCheckout_atualizaCobrancaExistente() {
        Cobranca cobranca = Cobranca.builder()
                .id(1L)
                .transactionId("TXN-001")
                .metodo(CobrancaMetodoEnum.CARTAO_CREDITO)
                .status(CobrancaStatusEnum.SOLICITADA)
                .build();

        com.jeduardo.ticketing.billing.dto.CheckoutValidacaoRequestDTO request =
                new com.jeduardo.ticketing.billing.dto.CheckoutValidacaoRequestDTO();
        request.setCavv("AAABBB");
        request.setXid("XYZ");
        request.setEci("05");

        com.jeduardo.ticketing.billing.dto.CheckoutValidacaoResponseDTO validacaoResponse =
                new com.jeduardo.ticketing.billing.dto.CheckoutValidacaoResponseDTO();
        validacaoResponse.setTransactionId("TXN-VALIDATED-XYZ");
        validacaoResponse.setAcsUrl("https://acs.mock.com/auth");
        validacaoResponse.setThreeDsPayload("{\"eci\":\"05\"}");
        validacaoResponse.setAutorizado(true);

        when(repository.findTopByTransactionIdOrderByIdDesc("TXN-001"))
                .thenReturn(Optional.of(cobranca));
        when(checkoutValidationClient.validar(request)).thenReturn(validacaoResponse);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cobrancaService.validarCheckout("TXN-001", request);

        verify(repository).save(argThat(c ->
                "TXN-VALIDATED-XYZ".equals(c.getTransactionId())
                        && "https://acs.mock.com/auth".equals(c.getAcsUrl())
        ));
    }

    // Cenário extra - consultarPorId 404
    @Test
    @DisplayName("7. consultarPorId - lança 404 quando não encontrado")
    void consultarPorId_lancaNotFound() {
        when(repository.findTopByIdOrderByIdDesc(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cobrancaService.consultarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }
}