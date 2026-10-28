package app.Pedido;

public enum StatusPedido {
    AGUARDANDO_ACEITE,
    AGUARDANDO_PAGAMENTO,
    EM_PRODUCAO,
    ENTREGUE,
    EM_DISPUTA,
    CONCLUIDO,
    RECUSADO,
    CANCELADO,
    REEMBOLSADO;

    public String getDescricao() {
        return switch (this) {
            case AGUARDANDO_ACEITE -> "Aguardando resposta";
            case AGUARDANDO_PAGAMENTO -> "Aguardando pagamento simulado";
            case EM_PRODUCAO -> "Em produção";
            case ENTREGUE -> "Entregue";
            case EM_DISPUTA -> "Em disputa";
            case CONCLUIDO -> "Concluído";
            case RECUSADO -> "Recusado";
            case CANCELADO -> "Cancelado";
            case REEMBOLSADO -> "Reembolsado";
        };
    }
}
