package com.Facturacion.FacturacionM.repository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.model.Factura;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DetalleFacturaRepositoryTest {

    @Autowired
    private DetalleFacturaRepository detalleFacturaRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Test
    void findByFactura_retornaDetalles() {
        Factura factura = Factura.builder()
                .numero("FCT-D001").fechaEmision(LocalDate.now())
                .total(0.0).estado("EMITIDA").pedidoId(1).usuarioRut(12345678L).build();
        Factura saved = facturaRepository.save(factura);

        DetalleFactura d1 = DetalleFactura.builder()
                .descripcion("Producto A").cantidad(2).precioUnitario(500.0).subtotal(1000.0)
                .factura(saved).build();
        DetalleFactura d2 = DetalleFactura.builder()
                .descripcion("Producto B").cantidad(1).precioUnitario(800.0).subtotal(800.0)
                .factura(saved).build();

        detalleFacturaRepository.saveAll(List.of(d1, d2));

        List<DetalleFactura> resultado = detalleFacturaRepository.findByFactura(saved);

        assertEquals(2, resultado.size());
        assertEquals("Producto A", resultado.get(0).getDescripcion());
        assertEquals("Producto B", resultado.get(1).getDescripcion());
    }

    @Test
    void findByFactura_sinDetalles() {
        Factura factura = Factura.builder()
                .numero("FCT-D002").fechaEmision(LocalDate.now())
                .total(0.0).estado("EMITIDA").pedidoId(2).usuarioRut(12345678L).build();
        Factura saved = facturaRepository.save(factura);

        List<DetalleFactura> resultado = detalleFacturaRepository.findByFactura(saved);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByFactura_noMezclaDetallesDeOtraFactura() {
        Factura facturaA = Factura.builder()
                .numero("FCT-D003").fechaEmision(LocalDate.now())
                .total(0.0).estado("EMITIDA").pedidoId(3).usuarioRut(12345678L).build();
        Factura facturaB = Factura.builder()
                .numero("FCT-D004").fechaEmision(LocalDate.now())
                .total(0.0).estado("EMITIDA").pedidoId(4).usuarioRut(12345678L).build();

        Factura savedA = facturaRepository.save(facturaA);
        Factura savedB = facturaRepository.save(facturaB);

        DetalleFactura detalleA = DetalleFactura.builder()
                .descripcion("Solo de A").cantidad(1).precioUnitario(100.0).subtotal(100.0)
                .factura(savedA).build();
        DetalleFactura detalleB = DetalleFactura.builder()
                .descripcion("Solo de B").cantidad(1).precioUnitario(200.0).subtotal(200.0)
                .factura(savedB).build();

        detalleFacturaRepository.saveAll(List.of(detalleA, detalleB));

        List<DetalleFactura> resultadoA = detalleFacturaRepository.findByFactura(savedA);
        List<DetalleFactura> resultadoB = detalleFacturaRepository.findByFactura(savedB);

        assertEquals(1, resultadoA.size());
        assertEquals("Solo de A", resultadoA.get(0).getDescripcion());

        assertEquals(1, resultadoB.size());
        assertEquals("Solo de B", resultadoB.get(0).getDescripcion());
    }
}
