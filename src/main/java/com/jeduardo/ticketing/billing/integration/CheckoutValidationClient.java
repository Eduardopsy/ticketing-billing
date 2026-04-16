package com.jeduardo.ticketing.billing.integration;

import com.jeduardo.ticketing.billing.dto.CheckoutValidacaoRequestDTO;
import com.jeduardo.ticketing.billing.dto.CheckoutValidacaoResponseDTO;

public interface CheckoutValidationClient {
    CheckoutValidacaoResponseDTO validar(CheckoutValidacaoRequestDTO request);
}