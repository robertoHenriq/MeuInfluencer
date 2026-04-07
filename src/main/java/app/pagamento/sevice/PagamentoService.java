package app.pagamento.sevice;

import app.pagamento.dto.PagamentoDTO;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.stereotype.Service;
import com.mercadopago.client.payment.*;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.payment.Payment;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PagamentoService {

    private final String ACCESS_TOKEN = "TEST-f53fb791-76b0-4e09-ba40-ddd7daa554dc";

    public String criarPreferencia() {
        PreferenceClient client = new PreferenceClient();

        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(PreferenceItemRequest.builder()
                .title("Produto Exemplo")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build());

        PreferenceRequest request = PreferenceRequest.builder()
                .items(items)
                .build();

        try {
            Preference preference = client.create(request);
            return preference.getId();
        } catch (com.mercadopago.exceptions.MPApiException e) {
            // Isso vai imprimir no console o erro REAL da API (ex: "invalid_access_token")
            System.out.println("Erro detalhado do Mercado Pago: " + e.getApiResponse().getContent());
            throw new RuntimeException("Erro MP: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro inesperado: " + e.getMessage());
        }
    }



    public Payment processPayment(PagamentoDTO dto) {
        // Configuração do Token (Certifique-se de que é o mesmo nível do Front - Teste com Teste)
        com.mercadopago.MercadoPagoConfig.setAccessToken("SEU_ACCESS_TOKEN");

        PaymentClient client = new PaymentClient();

        // CORREÇÃO DO ERRO: Criando o mapa de headers corretamente
        Map<String, String> headers = new HashMap<>();
        headers.put("x-idempotency-key", UUID.randomUUID().toString());

        MPRequestOptions requestOptions = MPRequestOptions.builder()
                .customHeaders(headers) // Use o plural 'customHeaders'
                .build();

        // Montagem do Request (Pix)
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(dto.transaction_amount())
                .description("Pagamento via Pix")
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder()
                        .email(dto.payer().email())
                        .firstName("Nome") // Ideal: extrair do DTO se disponível
                        .lastName("Sobrenome")
                        .identification(IdentificationRequest.builder()
                                .type(dto.payer().identification().type())
                                .number(dto.payer().identification().number())
                                .build())
                        .build())
                .build();

        try {
            // Envia o request e as opções com o header de idempotência
            return client.create(request, requestOptions);
        } catch (com.mercadopago.exceptions.MPApiException e) {
            System.err.println("Erro na API MP: " + e.getApiResponse().getContent());
            throw new RuntimeException(e.getApiResponse().getContent());
        } catch (Exception e) {
            throw new RuntimeException("Erro inesperado: " + e.getMessage());
        }
    }
    }


