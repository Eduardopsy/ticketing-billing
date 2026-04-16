package com.jeduardo.ticketing.billing.integration;

import org.springframework.stereotype.Component;

@Component
public class MockUserContextProvider implements UserContextProvider {

    @Override
    public UserContext getCurrentUser() {
        return UserContext.builder()
                .idUsuario("user-1")
                .givenName("Jorge")
                .familyName("Eduardo")
                .cpf("000.000.000-00")
                .build();
    }
}