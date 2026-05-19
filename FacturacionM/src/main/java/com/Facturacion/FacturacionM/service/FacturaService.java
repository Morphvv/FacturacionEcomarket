package com.Facturacion.FacturacionM.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.Facturacion.FacturacionM.feign.ClienteFeignClient;
import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.model.Factura;
import com.Facturacion.FacturacionM.repository.DetalleFacturaRepository;
import com.Facturacion.FacturacionM.repository.FacturaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final ClienteFeignClient clienteFeignClient;

    public List<Factura> listarTodasFacturas(){
        return facturaRepository.findAll();
    }

    public Optional<Factura> obtenerPorId(Long idFactura){
        return facturaRepository.findByIdFactura(idFactura);
    }

    public List <Factura> listarPorUsuario(Long usuarioRut){
        return facturaRepository.findByUsuarioRut(usuarioRut);
    }

    public void eliminarFactura(Long idFactura){
        facturaRepository.deleteByIdFactura(idFactura);
    }

    public Factura generarFacturas(Long usuarioRut, int pedidoId, List<DetalleFactura> detalles){
        Map<String, Object> usuario = clienteFeignClient.obtenerUsuarioPorRut(usuarioRut);

        if ("Usuario no disponible".equals(usuario.get("nombre"))){
            throw new RuntimeException(
                "El microservicio se cayo :.v"
            )
        }

        Factura factura = Factura.builder()
                .numero("FCT-" + UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase())
                .fechaEmision(LocalDate.now())
                .estado("EMITIDA")
                .pedidoId(pedidoId)
                .usuarioRut(usuarioRut)
                .total(0)
                .build();

        Factura saved = facturaRepository.save(factura);

        for (DetalleFactura detalle : detalles) {
            detalle.calcularSubtotal();
            detalle.setFactura(saved);
            detalleFacturaRepository.save(detalle);
        }

        saved.setTotal(calcularTotal(saved.getId()));
        return facturaRepository.save(saved);
    }
    
    public double calcularTotal(Long idFactura){
        return detalleFacturaRepository.findByFacturaId(idFactura)
            .stream()
            .mapToDouble(DetalleFactura::getSubtotal)
            .sum();
    }

    public Factura anularFactura(int id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    "Factura no encontrada: " + id));
        if ("ANULADA".equalsIgnoreCase(factura.getEstado()))
            throw new RuntimeException("La factura ya está anulada.");
        factura.setEstado("ANULADA");
        return facturaRepository.save(factura);
    } 

    public Map<String, Object> obtenerFacturaConUsuario(int facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new RuntimeException(
                    "Factura no encontrada: " + facturaId));
        Map<String, Object> usuario =
                clienteFeignClient.obtenerUsuarioPorRut(factura.getUsuarioRut());
        return Map.of("factura", factura, "usuario", usuario);
    }
}
