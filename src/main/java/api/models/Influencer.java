package api.models;

import java.util.List;

public class Influencer {
    private String nome;
    private int quantidadeDeSeguidoresTotal;
    private List<RedeSocial> redesSociais;

    public String cadastrarNome(String nome){
        return this.nome = nome;
    }

    public List<RedeSocial> cadastrarRedeSocial(String nome, int quantidadeDeSeguidores, String linkDoPerfil ) {
        RedeSocial redeSocialNova = new RedeSocial(nome, quantidadeDeSeguidores, linkDoPerfil);
        this.redesSociais.add(redeSocialNova);
        this.quantidadeDeSeguidoresTotal += quantidadeDeSeguidores;
        return this.redesSociais;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidadeDeSeguidoresTotal() {
        return quantidadeDeSeguidoresTotal;
    }

    public List<RedeSocial> removerRedeSocialPeloNome(String nome) {
        for(RedeSocial redeSocial: this.redesSociais){
            if(redeSocial.getNome().equals(nome)){
                this.redesSociais.remove(redeSocial);
                return this.redesSociais;
            }else {
                return null;
            }
        }
        return null;
    }
}
