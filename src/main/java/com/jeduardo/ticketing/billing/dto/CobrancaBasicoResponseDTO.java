package com.jeduardo.ticketing.billing.dto;

import com.jeduardo.ticketing.billing.domain.enums.CobrancaStatusEnum;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CobrancaBasicoResponseDTO {
    private Long id;
    private String txid;
    private String copiaEcola;
    private LocalDateTime dataExpiracao;
    private CobrancaStatusEnum status;
}