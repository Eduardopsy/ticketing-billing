package com.jeduardo.ticketing.billing.integration;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MockPagamentoGatewayClient implements PagamentoGatewayClient {

    @Override
    public String gerarTxid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public String gerarCopiaEcola(String txid, BigDecimal valor) {
        return "000201260058br.gov.bcb.pix0136" + txid + "5204000053039865802BR6009SAO PAULO62070503***6304ABCD";
    }

    @Override
    public String gerarTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}