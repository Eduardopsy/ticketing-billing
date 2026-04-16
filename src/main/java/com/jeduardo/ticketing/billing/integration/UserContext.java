package com.jeduardo.ticketing.billing.integration;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserContext {
    private String idUsuario;
    private String givenName;
    private String familyName;
    private String cpf;
}