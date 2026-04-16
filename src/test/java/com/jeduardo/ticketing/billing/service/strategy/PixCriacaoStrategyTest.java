package com.jeduardo.ticketing.billing.service.strategy;

import com.jeduardo.ticketing.billing.domain.entities.Cobranca;
import com.jeduardo.ticketing.billing.integration.PagamentoGatewayClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PixCriacaoStrategyTest {

    @Mock
    private PagamentoGatewayClient gatewayClient;

    @InjectMocks
    private PixCriacaoStrategy strategy;

    @Test
    void deveAplicarDadosPix() {

        Cobranca cobranca = Cobranca.builder()
                .valorSolicitacao(new BigDecimal("25.50"))
                .build();

        when(gatewayClient.gerarTxid()).thenReturn("tx123");
        when(gatewayClient.gerarCopiaEcola("tx123", new BigDecimal("25.50")))
                .thenReturn("copia-e-cola");

        strategy.aplicar(cobranca);

        assertThat(cobranca.getTxid()).isEqualTo("tx123");
        assertThat(cobranca.getCopiaEcola()).isEqualTo("copia-e-cola");
        assertThat(cobranca.getDataExpiracao()).isNotNull();
    }
}
