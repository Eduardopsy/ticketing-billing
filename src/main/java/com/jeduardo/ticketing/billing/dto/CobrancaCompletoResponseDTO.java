package com.jeduardo.ticketing.billing.dto;

import com.jeduardo.ticketing.billing.domain.enums.CobrancaMetodoEnum;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaStatusEnum;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaTipoEnum;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CobrancaCompletoResponseDTO {
    private Long id;
    private String txid;
    private String idUsuario;
    private CobrancaTipoEnum tipo;
    private CobrancaMetodoEnum metodo;
    private CobrancaStatusEnum status;
    private BigDecimal valorSolicitado;
    private BigDecimal valorPago;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataFinalizada;
}