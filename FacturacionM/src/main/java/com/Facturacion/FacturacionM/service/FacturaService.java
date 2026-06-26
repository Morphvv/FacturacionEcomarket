package com.Facturacion.FacturacionM.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.Facturacion.FacturacionM.exception.BusinessException;
import com.Facturacion.FacturacionM.exception.ResourceNotFoundException;
import com.Facturacion.FacturacionM.feign.ClienteFeignClient;
import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.model.Factura;
import com.Facturacion.FacturacionM.repository.DetalleFacturaRepository;
import com.Facturacion.FacturacionM.repository.FacturaRepository;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final ClienteFeignClient clienteFeignClient;

    public FacturaService(FacturaRepository facturaRepository,
                          DetalleFacturaRepository detalleFacturaRepository,
                          ClienteFeignClient clienteFeignClient) {
        this.facturaRepository = facturaRepository;
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.clienteFeignClient = clienteFeignClient;
    }

    public List<Factura> listarTodasFactura() {
        return facturaRepository.findAll();
    }

    public Optional<Factura> obtenerPorId(Long idFactura) {
        return facturaRepository.findById(idFactura);
    }

    public List<Factura> listarPorUsuario(Long usuarioRut) {
        return facturaRepository.findByUsuarioRut(usuarioRut);
    }

    public void eliminarFactura(Long idFactura) {
        if (!facturaRepository.existsById(idFactura)) {
            throw new ResourceNotFoundException("Factura no encontrada: " + idFactura);
        }
        facturaRepository.deleteById(idFactura);
    }

    public Factura generarFactura(Long usuarioRut, int pedidoId, List<DetalleFactura> detalles) {
        Map<String, Object> usuario = clienteFeignClient.obtenerUsuarioPorRut(usuarioRut);

        if ("Usuario no disponible".equals(usuario.get("nombre"))) {
            throw new BusinessException("El microservicio de usuarios no está disponible.");
        }

        if (detalles == null || detalles.isEmpty()) {
            throw new BusinessException("La factura debe tener al menos un detalle.");
        }

        Factura factura = Factura.builder()
                .numero("FCT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
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

        saved.setTotal(detalles.stream().mapToDouble(DetalleFactura::getSubtotal).sum());
        return facturaRepository.save(saved);
    }

    public double calcularTotal(Long idFactura) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + idFactura));
        return detalleFacturaRepository.findByFactura(factura)
                .stream()
                .mapToDouble(DetalleFactura::getSubtotal)
                .sum();
    }

    public Factura anularFactura(Long idFactura) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + idFactura));
        if ("ANULADA".equalsIgnoreCase(factura.getEstado())) {
            throw new BusinessException("La factura ya está anulada.");
        }
        factura.setEstado("ANULADA");
        return facturaRepository.save(factura);
    }

    public Map<String, Object> obtenerFacturaConUsuario(Long idFactura) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada: " + idFactura));
        Map<String, Object> usuario = clienteFeignClient.obtenerUsuarioPorRut(factura.getUsuarioRut());
        return Map.of("factura", factura, "usuario", usuario);
    }
}
