package com.jeduardo.ticketing.billing.dto;

import com.jeduardo.ticketing.billing.domain.enums.CobrancaMetodoEnum;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaTipoEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CobrancaRequestDTO {

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    private CobrancaTipoEnum tipo;

    private CobrancaMetodoEnum metodo;
}