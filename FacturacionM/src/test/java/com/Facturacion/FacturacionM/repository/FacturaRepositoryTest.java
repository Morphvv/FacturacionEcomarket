package com.Facturacion.FacturacionM.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.Facturacion.FacturacionM.model.Factura;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class FacturaRepositoryTest {

    @Autowired
    private FacturaRepository facturaRepository;

    @Test
    void findByUsuarioRut_retornaDosFacturas() {
        Factura f1 = Factura.builder()
                .numero("FCT-R001").fechaEmision(LocalDate.now())
                .total(1000.0).estado("EMITIDA").pedidoId(1).usuarioRut(12345678L).build();
        Factura f2 = Factura.builder()
                .numero("FCT-R002").fechaEmision(LocalDate.now())
                .total(500.0).estado("EMITIDA").pedidoId(2).usuarioRut(12345678L).build();
        Factura f3 = Factura.builder()
                .numero("FCT-R003").fechaEmision(LocalDate.now())
                .total(800.0).estado("EMITIDA").pedidoId(3).usuarioRut(99999999L).build();

        facturaRepository.saveAll(List.of(f1, f2, f3));

        List<Factura> resultado = facturaRepository.findByUsuarioRut(12345678L);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(f -> f.getUsuarioRut() == 12345678L));
    }

    @Test
    void findByUsuarioRut_sinResultados() {
        List<Factura> resultado = facturaRepository.findByUsuarioRut(11111111L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByNumero_retornaFactura() {
        Factura factura = Factura.builder()
                .numero("FCT-UNICO").fechaEmision(LocalDate.now())
                .total(2000.0).estado("EMITIDA").pedidoId(1).usuarioRut(12345678L).build();

        facturaRepository.save(factura);

        Optional<Factura> resultado = facturaRepository.findByNumero("FCT-UNICO");

        assertTrue(resultado.isPresent());
        assertEquals("FCT-UNICO", resultado.get().getNumero());
        assertEquals("EMITIDA", resultado.get().getEstado());
    }

    @Test
    void findByNumero_noExiste() {
        Optional<Factura> resultado = facturaRepository.findByNumero("FCT-INEXISTENTE");

        assertFalse(resultado.isPresent());
    }

    @Test
    void findByEstado_retornaEmitidas() {
        Factura emitida1 = Factura.builder()
                .numero("FCT-R004").fechaEmision(LocalDate.now())
                .total(1000.0).estado("EMITIDA").pedidoId(1).usuarioRut(12345678L).build();
        Factura emitida2 = Factura.builder()
                .numero("FCT-R005").fechaEmision(LocalDate.now())
                .total(500.0).estado("EMITIDA").pedidoId(2).usuarioRut(12345678L).build();
        Factura anulada = Factura.builder()
                .numero("FCT-R006").fechaEmision(LocalDate.now())
                .total(300.0).estado("ANULADA").pedidoId(3).usuarioRut(12345678L).build();

        facturaRepository.saveAll(List.of(emitida1, emitida2, anulada));

        List<Factura> resultado = facturaRepository.findByEstado("EMITIDA");

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(f -> "EMITIDA".equals(f.getEstado())));
    }

    @Test
    void findByEstado_sinResultados() {
        List<Factura> resultado = facturaRepository.findByEstado("PAGADA");

        assertTrue(resultado.isEmpty());
    }
}
