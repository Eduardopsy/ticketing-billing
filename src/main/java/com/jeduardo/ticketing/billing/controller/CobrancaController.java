package com.jeduardo.ticketing.billing.controller;

import com.jeduardo.ticketing.billing.domain.entities.Cobranca;
import com.jeduardo.ticketing.billing.dto.*;
import com.jeduardo.ticketing.billing.integration.UserContextProvider;
import com.jeduardo.ticketing.billing.service.CobrancaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cobrancas")
@RequiredArgsConstructor
public class CobrancaController {

    private final CobrancaService cobrancaService;
    private final UserContextProvider userContextProvider;

    @PostMapping
    public ResponseEntity<CobrancaBasicoResponseDTO> criar(
            @Valid @RequestBody CobrancaRequestDTO request) {

        Cobranca cobranca = cobrancaService.criarCobranca(request, userContextProvider.getCurrentUser());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                CobrancaBasicoResponseDTO.builder()
                        .id(cobranca.getId())
                        .txid(cobranca.getTxid())
                        .copiaEcola(cobranca.getCopiaEcola())
                        .dataExpiracao(cobranca.getDataExpiracao())
                        .status(cobranca.getStatus())
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CobrancaCompletoResponseDTO> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(cobrancaService.consultarPorId(id));
    }

    @PostMapping("/webhook/pix")
    public ResponseEntity<Void> webhookPix(@RequestBody(required = false) PixWebhookDTO webhook) {
        cobrancaService.processarNotificacaoWebhookPix(webhook);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{transactionId}/validate")
    public ResponseEntity<Void> validarCheckout(
            @PathVariable String transactionId,
            @Valid @RequestBody CheckoutValidacaoRequestDTO request) {

        cobrancaService.validarCheckout(transactionId, request);
        return ResponseEntity.ok().build();
    }
}