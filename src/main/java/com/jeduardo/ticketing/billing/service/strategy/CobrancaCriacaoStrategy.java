package com.jeduardo.ticketing.billing.service.strategy;

import com.jeduardo.ticketing.billing.domain.entities.Cobranca;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaMetodoEnum;

public interface CobrancaCriacaoStrategy {
    CobrancaMetodoEnum getMetodo();
    void aplicar(Cobranca cobranca);
}