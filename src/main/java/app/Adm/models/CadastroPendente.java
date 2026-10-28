package app.Adm.models;

import java.io.Serializable;

public record CadastroPendente(
        String email,
        String senhaCriptografada,
        TipoUsuario tipo,
        String codigo) implements Serializable {
}
