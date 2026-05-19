package com.Facturacion.FacturacionM.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.model.Factura;
import com.Facturacion.FacturacionM.repository.DetalleFacturaRepository;
import com.Facturacion.FacturacionM.repository.FacturaRepository;

@Service
public class DetalleFacturaService {

    private final DetalleFacturaRepository detalleFacturaRepository;
    private final FacturaRepository facturaRepository;

    public DetalleFacturaService(DetalleFacturaRepository detalleFacturaRepository,
                                  FacturaRepository facturaRepository) {
        this.detalleFacturaRepository = detalleFacturaRepository;
        this.facturaRepository = facturaRepository;
    }

    public List<DetalleFactura> listarTodosDetallesF() {
        return detalleFacturaRepository.findAll();
    }

    public Optional<DetalleFactura> obtenerPorId(Long idDetalleFactura) {
        return detalleFacturaRepository.findById(idDetalleFactura);
    }

    public List<DetalleFactura> listarPorFactura(Long idFactura) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + idFactura));
        return detalleFacturaRepository.findByFactura(factura);
    }

    public void eliminarDetalleFactura(Long idDetalleFactura) {
        detalleFacturaRepository.deleteById(idDetalleFactura);
    }

    public DetalleFactura calcularSubTotal(Long idDetalleFactura) {
        DetalleFactura detalle = detalleFacturaRepository.findById(idDetalleFactura)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado: " + idDetalleFactura));
        detalle.calcularSubtotal();
        return detalleFacturaRepository.save(detalle);
    }

    public DetalleFactura agregarDetalle(Long idFactura, DetalleFactura detalleFactura) {
        Factura factura = facturaRepository.findById(idFactura)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + idFactura));
        detalleFactura.setFactura(factura);
        detalleFactura.calcularSubtotal();
        return detalleFacturaRepository.save(detalleFactura);
    }
}
