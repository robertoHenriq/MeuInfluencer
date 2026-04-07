package app.pagamento.dto;

public record PagamentoResponse(
        String status,
        Long id,
        String qrCodeBase64,
        String qrCode
) {}