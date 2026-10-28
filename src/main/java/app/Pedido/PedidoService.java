package app.Pedido;

import app.Empresa.models.Empresa;
import app.Empresa.repository.EmpresaRepository;
import app.Influencer.models.Influencer;
import app.Influencer.models.Pacote;
import app.Influencer.repository.PacoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final EmpresaRepository empresaRepository;
    private final PacoteRepository pacoteRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            EmpresaRepository empresaRepository,
            PacoteRepository pacoteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.empresaRepository = empresaRepository;
        this.pacoteRepository = pacoteRepository;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarDaEmpresa(String email) {
        return pedidoRepository.findByEmpresaEmailIgnoreCaseOrderByCriadoEmDesc(email);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarDoInfluencer(String email) {
        return pedidoRepository.findByInfluencerEmailIgnoreCaseOrderByCriadoEmDesc(email);
    }

    @Transactional
    public void criar(String emailEmpresa, Integer pacoteId, String briefing) {
        Empresa empresa = buscarEmpresa(emailEmpresa);
        validarPerfilEmpresa(empresa);
        if (briefing == null || briefing.isBlank() || briefing.length() > 2000) {
            throw new IllegalArgumentException("Descreva o briefing do trabalho (até 2.000 caracteres).");
        }

        Pacote pacote = pacoteRepository.findById(pacoteId)
                .orElseThrow(() -> new IllegalArgumentException("Pacote não encontrado."));
        Influencer influencer = pacote.getInfluencer();
        if (!influencer.isEmailVerificado() || !influencer.getPacotes().stream()
                .anyMatch(item -> item.getId().equals(pacoteId) && item.isAtivo())) {
            throw new IllegalArgumentException("Este pacote não está disponível para contratação.");
        }
        if (influencer.getRedesSociais().isEmpty()
                || influencer.getNome() == null || influencer.getNome().isBlank()) {
            throw new IllegalArgumentException("O perfil do influenciador ainda não está completo.");
        }

        Pedido pedido = new Pedido();
        pedido.setEmpresa(empresa);
        pedido.setInfluencer(influencer);
        pedido.setPacoteTitulo(pacote.getTitulo());
        pedido.setPacoteDescricao(pacote.getDescricao());
        pedido.setPrecoCongelado(pacote.getPreco());
        pedido.setPrazoDias(pacote.getPrazoDias());
        pedido.setBriefing(briefing.trim());
        pedidoRepository.save(pedido);
    }

    @Transactional
    public void aceitar(String emailInfluencer, Integer pedidoId) {
        Pedido pedido = pedidoDoInfluencer(emailInfluencer, pedidoId);
        exigirStatus(pedido, StatusPedido.AGUARDANDO_ACEITE);
        pedido.atualizarStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
    }

    @Transactional
    public void recusar(String emailInfluencer, Integer pedidoId) {
        Pedido pedido = pedidoDoInfluencer(emailInfluencer, pedidoId);
        exigirStatus(pedido, StatusPedido.AGUARDANDO_ACEITE);
        pedido.atualizarStatus(StatusPedido.RECUSADO);
    }

    @Transactional
    public void pagarSimulado(String emailEmpresa, Integer pedidoId) {
        Pedido pedido = pedidoDaEmpresa(emailEmpresa, pedidoId);
        exigirStatus(pedido, StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.atualizarStatus(StatusPedido.EM_PRODUCAO);
    }

    @Transactional
    public void entregar(String emailInfluencer, Integer pedidoId, String entrega) {
        Pedido pedido = pedidoDoInfluencer(emailInfluencer, pedidoId);
        exigirStatus(pedido, StatusPedido.EM_PRODUCAO);
        if (entrega == null || entrega.isBlank() || entrega.length() > 2000) {
            throw new IllegalArgumentException("Informe o link ou detalhes da entrega (até 2.000 caracteres).");
        }
        pedido.setEntrega(entrega.trim());
        pedido.atualizarStatus(StatusPedido.ENTREGUE);
    }

    @Transactional
    public void aprovar(String emailEmpresa, Integer pedidoId) {
        Pedido pedido = pedidoDaEmpresa(emailEmpresa, pedidoId);
        exigirStatus(pedido, StatusPedido.ENTREGUE);
        pedido.atualizarStatus(StatusPedido.CONCLUIDO);
    }

    @Transactional
    public void contestar(String emailEmpresa, Integer pedidoId) {
        Pedido pedido = pedidoDaEmpresa(emailEmpresa, pedidoId);
        exigirStatus(pedido, StatusPedido.ENTREGUE);
        pedido.atualizarStatus(StatusPedido.EM_DISPUTA);
    }

    private Empresa buscarEmpresa(String email) {
        return empresaRepository.findByEmailIgnoreCaseAndEmailVerificadoTrue(email)
                .orElseThrow(() -> new IllegalArgumentException("Conta de empresa não encontrada."));
    }

    private void validarPerfilEmpresa(Empresa empresa) {
        if (empresa.getNome() == null || empresa.getNome().isBlank()
                || empresa.getCnpj() == null || empresa.getCnpj().isBlank()
                || empresa.getSegmento() == null || empresa.getSegmento().isBlank()) {
            throw new IllegalArgumentException("Complete o nome, CNPJ e segmento da empresa antes de contratar.");
        }
    }

    private Pedido pedidoDaEmpresa(String email, Integer pedidoId) {
        return pedidoRepository.findByIdAndEmpresaEmailIgnoreCase(pedidoId, email)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado para esta empresa."));
    }

    private Pedido pedidoDoInfluencer(String email, Integer pedidoId) {
        return pedidoRepository.findByIdAndInfluencerEmailIgnoreCase(pedidoId, email)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado para este influenciador."));
    }

    private void exigirStatus(Pedido pedido, StatusPedido esperado) {
        if (pedido.getStatus() != esperado) {
            throw new IllegalArgumentException("Esta ação não está disponível para o status atual do pedido.");
        }
    }
}
