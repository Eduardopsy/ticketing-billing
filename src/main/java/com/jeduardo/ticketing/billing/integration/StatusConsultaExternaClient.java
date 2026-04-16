package com.jeduardo.ticketing.billing.integration;

import com.jeduardo.ticketing.billing.domain.enums.CobrancaStatusEnum;

public interface StatusConsultaExternaClient {
    CobrancaStatusEnum consultarStatus(String txid);
}