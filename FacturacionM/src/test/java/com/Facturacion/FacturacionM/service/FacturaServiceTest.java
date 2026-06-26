package com.Facturacion.FacturacionM.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.Facturacion.FacturacionM.exception.BusinessException;
import com.Facturacion.FacturacionM.exception.ResourceNotFoundException;
import com.Facturacion.FacturacionM.feign.ClienteFeignClient;
import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.model.Factura;
import com.Facturacion.FacturacionM.repository.DetalleFacturaRepository;
import com.Facturacion.FacturacionM.repository.FacturaRepository;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private DetalleFacturaRepository detalleFacturaRepository;

    @Mock
    private ClienteFeignClient clienteFeignClient;

    @InjectMocks
    private FacturaService facturaService;

    @Test
    void listarTodasFacturas() {
        Factura f1 = Factura.builder().idFactura(1L).numero("FCT-001").estado("EMITIDA").build();
        Factura f2 = Factura.builder().idFactura(2L).numero("FCT-002").estado("ANULADA").build();

        when(facturaRepository.findAll()).thenReturn(List.of(f1, f2));

        List<Factura> resultado = facturaService.listarTodasFactura();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("FCT-001", resultado.get(0).getNumero());
        verify(facturaRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorIdExistente() {
        Factura factura = Factura.builder().idFactura(1L).estado("EMITIDA").build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));

        Optional<Factura> resultado = facturaService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdFactura());
        verify(facturaRepository, times(1)).findById(1L);
    }

    @Test
    void obtenerPorIdNoExistente() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Factura> resultado = facturaService.obtenerPorId(99L);

        assertFalse(resultado.isPresent());
        verify(facturaRepository, times(1)).findById(99L);
    }

    @Test
    void listarPorUsuario() {
        Factura factura = Factura.builder().idFactura(1L).usuarioRut(12345678L).build();

        when(facturaRepository.findByUsuarioRut(12345678L)).thenReturn(List.of(factura));

        List<Factura> resultado = facturaService.listarPorUsuario(12345678L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(12345678L, resultado.get(0).getUsuarioRut());
        verify(facturaRepository, times(1)).findByUsuarioRut(12345678L);
    }

    @Test
    void eliminarFacturaExistente() {
        when(facturaRepository.existsById(1L)).thenReturn(true);

        facturaService.eliminarFactura(1L);

        verify(facturaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarFacturaNoExistente() {
        when(facturaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> facturaService.eliminarFactura(99L));
        verify(facturaRepository, never()).deleteById(any());
    }

    @Test
    void generarFactura_exitoso() {
        Map<String, Object> usuarioValido = Map.of("nombre", "Juan Pérez", "estadoUsuario", "Activo");
        DetalleFactura detalle = DetalleFactura.builder()
                .descripcion("Producto A").cantidad(2).precioUnitario(1000.0).build();
        Factura facturaGuardada = Factura.builder()
                .idFactura(1L).estado("EMITIDA").total(2000.0).build();

        when(clienteFeignClient.obtenerUsuarioPorRut(12345678L)).thenReturn(usuarioValido);
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaGuardada);
        when(detalleFacturaRepository.save(any(DetalleFactura.class))).thenReturn(detalle);

        Factura resultado = facturaService.generarFactura(12345678L, 10, List.of(detalle));

        assertNotNull(resultado);
        assertEquals("EMITIDA", resultado.getEstado());
        verify(facturaRepository, times(2)).save(any(Factura.class));
        verify(detalleFacturaRepository, times(1)).save(any(DetalleFactura.class));
    }

    @Test
    void generarFactura_usuarioNoDisponible() {
        Map<String, Object> fallback = Map.of("nombre", "Usuario no disponible");

        when(clienteFeignClient.obtenerUsuarioPorRut(anyLong())).thenReturn(fallback);

        assertThrows(BusinessException.class, () ->
                facturaService.generarFactura(99L, 10, List.of(new DetalleFactura())));

        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    void generarFactura_sinDetalles() {
        Map<String, Object> usuarioValido = Map.of("nombre", "Juan Pérez");

        when(clienteFeignClient.obtenerUsuarioPorRut(anyLong())).thenReturn(usuarioValido);

        assertThrows(BusinessException.class, () ->
                facturaService.generarFactura(12345678L, 10, List.of()));

        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    void calcularTotal() {
        Factura factura = Factura.builder().idFactura(1L).build();
        DetalleFactura d1 = DetalleFactura.builder().subtotal(500.0).build();
        DetalleFactura d2 = DetalleFactura.builder().subtotal(300.0).build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(detalleFacturaRepository.findByFactura(factura)).thenReturn(List.of(d1, d2));

        double total = facturaService.calcularTotal(1L);

        assertEquals(800.0, total);
        verify(facturaRepository, times(1)).findById(1L);
        verify(detalleFacturaRepository, times(1)).findByFactura(factura);
    }

    @Test
    void calcularTotal_facturaNoExiste() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> facturaService.calcularTotal(99L));
    }

    @Test
    void anularFactura_exitoso() {
        Factura factura = Factura.builder().idFactura(1L).estado("EMITIDA").build();
        Factura anulada = Factura.builder().idFactura(1L).estado("ANULADA").build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenReturn(anulada);

        Factura resultado = facturaService.anularFactura(1L);

        assertNotNull(resultado);
        assertEquals("ANULADA", resultado.getEstado());
        verify(facturaRepository, times(1)).save(factura);
    }

    @Test
    void anularFactura_yaAnulada() {
        Factura factura = Factura.builder().idFactura(1L).estado("ANULADA").build();

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));

        assertThrows(BusinessException.class, () -> facturaService.anularFactura(1L));
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    void anularFactura_noExiste() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> facturaService.anularFactura(99L));
    }

    @Test
    void obtenerFacturaConUsuario() {
        Factura factura = Factura.builder().idFactura(1L).usuarioRut(12345678L).build();
        Map<String, Object> usuario = Map.of("nombre", "Juan Pérez");

        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(clienteFeignClient.obtenerUsuarioPorRut(12345678L)).thenReturn(usuario);

        Map<String, Object> resultado = facturaService.obtenerFacturaConUsuario(1L);

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("factura"));
        assertTrue(resultado.containsKey("usuario"));
        verify(facturaRepository, times(1)).findById(1L);
        verify(clienteFeignClient, times(1)).obtenerUsuarioPorRut(12345678L);
    }

    @Test
    void obtenerFacturaConUsuario_noExiste() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                facturaService.obtenerFacturaConUsuario(99L));
    }
}
