package com.jeduardo.ticketing.billing.dto;

import lombok.Data;

@Data
public class CheckoutValidacaoResponseDTO {
    private String transactionId;
    private String acsUrl;
    private String threeDsPayload;
    private boolean autorizado;
}