package com.jeduardo.ticketing.billing.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PixWebhookDTO {

    private List<PixItem> pix;

    @Data
    public static class PixItem {
        private String txid;
        private LocalDateTime horario;
        private BigDecimal valor;
    }
}