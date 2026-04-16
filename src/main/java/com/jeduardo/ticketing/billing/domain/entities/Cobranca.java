package com.jeduardo.ticketing.billing.domain.entities;

import com.jeduardo.ticketing.billing.domain.enums.CobrancaMetodoEnum;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaStatusEnum;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaTipoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cobrancas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String idUsuario;

    @Column(nullable = false)
    private String nomeSolicitante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CobrancaTipoEnum tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CobrancaMetodoEnum metodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CobrancaStatusEnum status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSolicitacao;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorPago;

    private String txid;
    private String copiaEcola;
    private String transactionId;
    private String acsUrl;

    @Column(columnDefinition = "TEXT")
    private String threeDsPayload;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataExpiracao;
    private LocalDateTime dataFinalizada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cobranca_pai_id")
    private Cobranca cobrancaPai;
}
