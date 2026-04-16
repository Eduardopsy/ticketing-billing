package com.jeduardo.ticketing.billing.integration;

import com.jeduardo.ticketing.billing.dto.CheckoutValidacaoRequestDTO;
import com.jeduardo.ticketing.billing.dto.CheckoutValidacaoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class MockCheckoutValidationClient implements CheckoutValidationClient {

    @Override
    public CheckoutValidacaoResponseDTO validar(CheckoutValidacaoRequestDTO request) {
        CheckoutValidacaoResponseDTO response = new CheckoutValidacaoResponseDTO();
        response.setTransactionId("TXN-VALIDATED-" + request.getXid());
        response.setAcsUrl("https://acs.mock.com/auth");
        response.setThreeDsPayload("{\"version\":\"2.2\",\"eci\":\"" + request.getEci() + "\"}");
        response.setAutorizado(true);
        return response;
    }
}