package com.jeduardo.ticketing.billing.dto;

import lombok.Data;

@Data
public class CheckoutValidacaoRequestDTO {
    private String cavv;
    private String xid;
    private String eci;
}