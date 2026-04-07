package app.pagamento.dto;

import java.math.BigDecimal;

public record PagamentoDTO(
        BigDecimal transaction_amount,
        String description,
        String payment_method_id, // Adicione este para saber se é pix
        PayerDTO payer
) {
    public record PayerDTO(
            String email,
            String first_name,
            String last_name,
            IdentificationDTO identification // O nome aqui deve ser igual ao do JSON
    ) {}

    public record IdentificationDTO(
            String type,
            String number
    ) {}
}