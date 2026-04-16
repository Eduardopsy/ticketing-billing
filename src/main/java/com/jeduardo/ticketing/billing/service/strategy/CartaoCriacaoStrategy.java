package com.jeduardo.ticketing.billing.service.strategy;

import com.jeduardo.ticketing.billing.domain.entities.Cobranca;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaMetodoEnum;
import com.jeduardo.ticketing.billing.integration.PagamentoGatewayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartaoCriacaoStrategy implements CobrancaCriacaoStrategy {

    private final PagamentoGatewayClient gatewayClient;

    @Override
    public CobrancaMetodoEnum getMetodo() {
        return CobrancaMetodoEnum.CARTAO_CREDITO;
    }

    @Override
    public void aplicar(Cobranca cobranca) {
        cobranca.setTransactionId(gatewayClient.gerarTransactionId());
        cobranca.setAcsUrl("https://acs.mock.com/authenticate");
        cobranca.setThreeDsPayload("{\"version\":\"2.2\",\"enrolled\":true}");
    }
}