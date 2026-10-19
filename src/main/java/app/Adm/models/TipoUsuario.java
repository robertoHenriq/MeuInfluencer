package app.Adm.models;

public enum TipoUsuario {
    ADMIN("Administrador"),
    EMPRESA("Empresa"),
    INFLUENCER("Influencer");

    private final String descricao;

    TipoUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
