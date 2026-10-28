package app.Pedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByEmpresaEmailIgnoreCaseOrderByCriadoEmDesc(String email);
    List<Pedido> findByInfluencerEmailIgnoreCaseOrderByCriadoEmDesc(String email);
    Optional<Pedido> findByIdAndEmpresaEmailIgnoreCase(Integer id, String email);
    Optional<Pedido> findByIdAndInfluencerEmailIgnoreCase(Integer id, String email);
}
