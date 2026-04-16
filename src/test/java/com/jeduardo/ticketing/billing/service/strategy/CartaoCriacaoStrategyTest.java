package com.jeduardo.ticketing.billing.service.strategy;

import com.jeduardo.ticketing.billing.domain.entities.Cobranca;
import com.jeduardo.ticketing.billing.integration.PagamentoGatewayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartaoCriacaoStrategyTest {

    @Mock
    private PagamentoGatewayClient gatewayClient;

    @InjectMocks
    private CartaoCriacaoStrategy strategy;

    @Test
    void deveAplicarDadosCartao() {

        Cobranca cobranca = new Cobranca();

        when(gatewayClient.gerarTransactionId()).thenReturn("TX123");

        strategy.aplicar(cobranca);

        assertThat(cobranca.getTransactionId()).isEqualTo("TX123");
        assertThat(cobranca.getAcsUrl()).isNotNull();
        assertThat(cobranca.getThreeDsPayload()).isNotNull();
    }
}

