package app.Influencer.services;

import app.Influencer.models.Influencer;
import app.Influencer.models.Pacote;
import app.Influencer.repository.InfluencerRepository;
import app.Influencer.repository.PacoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PacoteService {
    private final PacoteRepository pacoteRepository;
    private final InfluencerRepository influencerRepository;

    public PacoteService(PacoteRepository pacoteRepository, InfluencerRepository influencerRepository) {
        this.pacoteRepository = pacoteRepository;
        this.influencerRepository = influencerRepository;
    }

    @Transactional(readOnly = true)
    public List<Pacote> listarDoInfluencer(Integer influencerId) {
        return pacoteRepository.findByInfluencerIdOrderByIdDesc(influencerId);
    }

    @Transactional
    public void salvar(String email, Integer pacoteId, String titulo, String descricao, BigDecimal preco, int prazoDias) {
        validar(titulo, descricao, preco, prazoDias);
        Influencer influencer = influencerRepository.findFirstByEmailIgnoreCaseAndEmailVerificadoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de influenciador não encontrado."));

        Pacote pacote = pacoteId == null
                ? new Pacote()
                : pacoteRepository.findByIdAndInfluencerId(pacoteId, influencer.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Pacote não encontrado."));
        pacote.setInfluencer(influencer);
        pacote.setTitulo(titulo.trim());
        pacote.setDescricao(descricao.trim());
        pacote.setPreco(preco);
        pacote.setPrazoDias(prazoDias);
        pacote.setAtivo(true);
        pacoteRepository.save(pacote);
    }

    @Transactional
    public void alterarStatus(String email, Integer pacoteId, boolean ativo) {
        Influencer influencer = influencerRepository.findFirstByEmailIgnoreCaseAndEmailVerificadoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de influenciador não encontrado."));
        Pacote pacote = pacoteRepository.findByIdAndInfluencerId(pacoteId, influencer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Pacote não encontrado."));
        pacote.setAtivo(ativo);
    }

    @Transactional
    public void excluir(String email, Integer pacoteId) {
        Influencer influencer = influencerRepository.findFirstByEmailIgnoreCaseAndEmailVerificadoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de influenciador não encontrado."));
        Pacote pacote = pacoteRepository.findByIdAndInfluencerId(pacoteId, influencer.getId())
                .orElseThrow(() -> new IllegalArgumentException("Pacote não encontrado."));
        pacoteRepository.delete(pacote);
    }

    private void validar(String titulo, String descricao, BigDecimal preco, int prazoDias) {
        if (titulo == null || titulo.isBlank() || titulo.length() > 120) {
            throw new IllegalArgumentException("Informe um título com até 120 caracteres.");
        }
        if (descricao == null || descricao.isBlank() || descricao.length() > 1500) {
            throw new IllegalArgumentException("Informe uma descrição com até 1.500 caracteres.");
        }
        if (preco == null || preco.signum() <= 0 || preco.scale() > 2) {
            throw new IllegalArgumentException("O preço precisa ser maior que zero e ter no máximo duas casas decimais.");
        }
        if (prazoDias < 1 || prazoDias > 90) {
            throw new IllegalArgumentException("O prazo deve ser de 1 a 90 dias.");
        }
    }
}
