package com.jeduardo.ticketing.billing.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CobrancaStatusEnum {
    SOLICITADA(2),
    EXPIRADA(3),
    ERRO_APROVACAO_PEDIDO(4),
    FINALIZADA(5),
    EM_REPROCESSAMENTO(6),
    ERRO_ANALISE_PENDENTE(9);

    private final int code;
}