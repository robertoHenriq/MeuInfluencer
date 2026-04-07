package app.pagamento.controller;

import app.pagamento.dto.PagamentoDTO;
import app.pagamento.dto.PagamentoResponse;
import app.pagamento.sevice.PagamentoService;
import com.mercadopago.resources.payment.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/processarpagamento")
public class PagamentoController {

    @Autowired
    private PagamentoService paymentService;


    @GetMapping("/pagar")
    public String paginaDePagamento() {
        return "payment/brick_pagamento";
    }

    @GetMapping("/create_preference")
    @ResponseBody // Não esqueça disso!
    public ResponseEntity<Map<String, String>> createPreference() {
        try {
            String id = paymentService.criarPreferencia();
            return ResponseEntity.ok(Map.of("id", id));
        } catch (Exception e) {
            // Se der erro 500, o log vai aparecer aqui no console do Java
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/processar")
    public ResponseEntity<?> processarPagamento(@RequestBody PagamentoDTO dto) {
        try {
            if (dto.payer() == null || dto.payer().identification() == null) {
                return ResponseEntity.badRequest().body("Dados do pagador (CPF) ausentes.");
            }

            Payment pagamento = paymentService.processPayment(dto);

            Map<String, Object> resposta = new HashMap<>();
            resposta.put("status", pagamento.getStatus());
            resposta.put("id", pagamento.getId());

            // IMPORTANTE: Adicionar os dados do PIX na resposta para o Front-end
            if (pagamento.getPointOfInteraction() != null &&
                    pagamento.getPointOfInteraction().getTransactionData() != null) {

                resposta.put("qr_code", pagamento.getPointOfInteraction().getTransactionData().getQrCode());
                resposta.put("qr_code_base64", pagamento.getPointOfInteraction().getTransactionData().getQrCodeBase64());
            }

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));        }
    }

}

