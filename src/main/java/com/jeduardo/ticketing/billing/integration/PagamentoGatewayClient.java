package com.jeduardo.ticketing.billing.integration;

public interface PagamentoGatewayClient {
    String gerarTxid();
    String gerarCopiaEcola(String txid, java.math.BigDecimal valor);
    String gerarTransactionId();
}