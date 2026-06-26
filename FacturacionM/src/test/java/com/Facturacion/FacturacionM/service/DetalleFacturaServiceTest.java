package com.Facturacion.FacturacionM.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.Facturacion.FacturacionM.exception.ResourceNotFoundException;
import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.model.Factura;
import com.Facturacion.FacturacionM.repository.DetalleFacturaRepository;
import com.Facturacion.FacturacionM.repository.FacturaRepository;

@ExtendWith(MockitoExtension.class)
class DetalleFacturaServiceTest {

    @Mock
    private DetalleFacturaRepository detalleFacturaRepository;

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private DetalleFacturaService detalleFacturaService;

    @Test
    void listarTodosDetalles() {
        DetalleFactura d1 = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto A").cantidad(2).precioUnitario(500.0).subtotal(1000.0)
                .build();
        DetalleFactura d2 = DetalleFactura.builder()
                .idDetalleFactura(2L).descripcion("Producto B").cantidad(1).precioUnitario(800.0).subtotal(800.0)
                .build();

        when(detalleFacturaRepository.findAll()).thenReturn(List.of(d1, d2));

        List<DetalleFactura> resultado = detalleFacturaService.listarTodosDetallesF();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Producto A", resultado.get(0).getDescripcion());
        verify(detalleFacturaRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorIdExistente() {
        DetalleFactura detalle = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto C").cantidad(3).precioUnitario(200.0).subtotal(600.0)
                .build();

        when(detalleFacturaRepository.findById(1L)).thenReturn(Optional.of(detalle));

        Optional<DetalleFactura> resultado = detalleFacturaService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdDetalleFactura());
        assertEquals("Producto C", resultado.get().getDescripcion());
        verify(detalleFacturaRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorIdNoExistente() {
        when(detalleFacturaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<DetalleFactura> resultado = detalleFacturaService.obtenerPorId(99L);

        assertFalse(resultado.isPresent());
        verify(detalleFacturaRepository, times(1)).findById(99L);
    }

    @Test
    void listarPorFactura() {
        Factura factura = Factura.builder().idFactura(1L).estado("EMITIDA").build();
        DetalleFactura detalle = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto D").subtotal(400.0)
                .build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(detalleFacturaRepository.findByFactura(factura)).thenReturn(List.of(detalle));

        List<DetalleFactura> resultado = detalleFacturaService.listarPorFactura(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(facturaRepository, times(1)).findById(1L);
        verify(detalleFacturaRepository, times(1)).findByFactura(factura);
    }

    @Test
    void listarPorFactura_facturaNoExiste() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                detalleFacturaService.listarPorFactura(99L));

        verify(detalleFacturaRepository, never()).findByFactura(any());
    }

    @Test
    void eliminarDetalleExistente() {
        when(detalleFacturaRepository.existsById(1L)).thenReturn(true);

        detalleFacturaService.eliminarDetalleFactura(1L);

        verify(detalleFacturaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarDetalleNoExistente() {
        when(detalleFacturaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                detalleFacturaService.eliminarDetalleFactura(99L));

        verify(detalleFacturaRepository, never()).deleteById(any());
    }

    @Test
    void calcularSubTotal_exitoso() {
        DetalleFactura detalle = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto E").cantidad(4).precioUnitario(250.0).subtotal(0.0)
                .build();
        DetalleFactura detalleActualizado = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto E").cantidad(4).precioUnitario(250.0).subtotal(1000.0)
                .build();

        when(detalleFacturaRepository.findById(1L)).thenReturn(Optional.of(detalle));
        when(detalleFacturaRepository.save(any(DetalleFactura.class))).thenReturn(detalleActualizado);

        DetalleFactura resultado = detalleFacturaService.calcularSubTotal(1L);

        assertNotNull(resultado);
        assertEquals(1000.0, resultado.getSubtotal());
        verify(detalleFacturaRepository, times(1)).save(any(DetalleFactura.class));
    }

    @Test
    void calcularSubTotal_noExiste() {
        when(detalleFacturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                detalleFacturaService.calcularSubTotal(99L));
    }

    @Test
    void agregarDetalle_exitoso() {
        Factura factura = Factura.builder().idFactura(1L).estado("EMITIDA").build();
        DetalleFactura detalleEntrada = DetalleFactura.builder()
                .descripcion("Producto F").cantidad(2).precioUnitario(300.0)
                .build();
        DetalleFactura detalleGuardado = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto F").cantidad(2).precioUnitario(300.0).subtotal(600.0)
                .build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(detalleFacturaRepository.save(any(DetalleFactura.class))).thenReturn(detalleGuardado);

        DetalleFactura resultado = detalleFacturaService.agregarDetalle(1L, detalleEntrada);

        assertNotNull(resultado);
        assertEquals(600.0, resultado.getSubtotal());
        assertEquals("Producto F", resultado.getDescripcion());
        verify(facturaRepository, times(1)).findById(1L);
        verify(detalleFacturaRepository, times(1)).save(any(DetalleFactura.class));
    }

    @Test
    void agregarDetalle_facturaNoExiste() {
        DetalleFactura detalleEntrada = DetalleFactura.builder()
                .descripcion("Producto G").cantidad(1).precioUnitario(100.0)
                .build();

        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                detalleFacturaService.agregarDetalle(99L, detalleEntrada));

        verify(detalleFacturaRepository, never()).save(any(DetalleFactura.class));
    }
}
