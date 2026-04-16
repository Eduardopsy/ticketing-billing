package com.jeduardo.ticketing.billing.integration;

import com.jeduardo.ticketing.billing.domain.enums.CobrancaStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class MockStatusConsultaExternaClient implements StatusConsultaExternaClient {

    @Override
    public CobrancaStatusEnum consultarStatus(String txid) {
        return CobrancaStatusEnum.SOLICITADA;
    }
}