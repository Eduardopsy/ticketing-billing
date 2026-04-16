package com.jeduardo.ticketing.billing.service.strategy;

import com.jeduardo.ticketing.billing.domain.entities.Cobranca;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaMetodoEnum;
import com.jeduardo.ticketing.billing.integration.PagamentoGatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class PixCriacaoStrategy implements CobrancaCriacaoStrategy {

    private final PagamentoGatewayClient gatewayClient;

    @Override
    public CobrancaMetodoEnum getMetodo() {
        return CobrancaMetodoEnum.PIX;
    }

    @Override
    public void aplicar(Cobranca cobranca) {
        String txid = gatewayClient.gerarTxid();
        cobranca.setTxid(txid);
        cobranca.setCopiaEcola(gatewayClient.gerarCopiaEcola(txid, cobranca.getValorSolicitacao()));
        cobranca.setDataExpiracao(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).plusHours(2));
    }
}