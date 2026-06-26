package com.Facturacion.FacturacionM.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.Facturacion.FacturacionM.dto.GenerarFacturaRequest;
import com.Facturacion.FacturacionM.exception.BusinessException;
import com.Facturacion.FacturacionM.exception.ResourceNotFoundException;
import com.Facturacion.FacturacionM.feign.ClienteFeignClient;
import com.Facturacion.FacturacionM.model.Factura;
import com.Facturacion.FacturacionM.service.FacturaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FacturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacturaService facturaService;

    @MockitoBean
    private ClienteFeignClient clienteFeignClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void listarTodosDevuelve200() throws Exception {
        Factura f1 = Factura.builder().idFactura(1L).numero("FCT-001").estado("EMITIDA").total(2000.0).build();
        Factura f2 = Factura.builder().idFactura(2L).numero("FCT-002").estado("ANULADA").total(500.0).build();

        when(facturaService.listarTodasFactura()).thenReturn(List.of(f1, f2));

        mockMvc.perform(get("/api/v1/factura/listarTodos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("FCT-001"))
                .andExpect(jsonPath("$[1].estado").value("ANULADA"));
    }

    @Test
    void obtenerPorIdDevuelve200() throws Exception {
        Factura factura = Factura.builder().idFactura(1L).estado("EMITIDA").total(1500.0).build();

        when(facturaService.obtenerPorId(1L)).thenReturn(Optional.of(factura));

        mockMvc.perform(get("/api/v1/factura/listarPorIdFactura/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idFactura").value(1))
                .andExpect(jsonPath("$.estado").value("EMITIDA"));
    }

    @Test
    void obtenerPorIdDevuelve404() throws Exception {
        when(facturaService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/factura/listarPorIdFactura/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generarFacturaDevuelve201() throws Exception {
        GenerarFacturaRequest.DetalleRequest det = new GenerarFacturaRequest.DetalleRequest();
        det.setDescripcion("Producto A");
        det.setCantidad(2);
        det.setPrecioUnitario(1000.0);

        GenerarFacturaRequest request = new GenerarFacturaRequest();
        request.setUsuarioRut(12345678L);
        request.setPedidoId(10);
        request.setDetalles(List.of(det));

        Factura generada = Factura.builder().idFactura(1L).estado("EMITIDA").total(2000.0).build();
        when(facturaService.generarFactura(anyLong(), anyInt(), anyList())).thenReturn(generada);

        mockMvc.perform(post("/api/v1/factura/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("EMITIDA"))
                .andExpect(jsonPath("$.total").value(2000.0));
    }

    @Test
    void generarFactura_usuarioNoDisponibleDevuelve400() throws Exception {
        GenerarFacturaRequest.DetalleRequest det = new GenerarFacturaRequest.DetalleRequest();
        det.setDescripcion("Producto B");
        det.setCantidad(1);
        det.setPrecioUnitario(500.0);

        GenerarFacturaRequest request = new GenerarFacturaRequest();
        request.setUsuarioRut(99L);
        request.setPedidoId(5);
        request.setDetalles(List.of(det));

        when(facturaService.generarFactura(anyLong(), anyInt(), anyList()))
                .thenThrow(new BusinessException("El microservicio de usuarios no está disponible."));

        mockMvc.perform(post("/api/v1/factura/generar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void calcularTotalDevuelve200() throws Exception {
        when(facturaService.calcularTotal(1L)).thenReturn(3500.0);

        mockMvc.perform(get("/api/v1/factura/total/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3500.0));
    }

    @Test
    void calcularTotal_facturaNoExisteDevuelve404() throws Exception {
        when(facturaService.calcularTotal(99L))
                .thenThrow(new ResourceNotFoundException("Factura no encontrada: 99"));

        mockMvc.perform(get("/api/v1/factura/total/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void anularFacturaDevuelve200() throws Exception {
        Factura anulada = Factura.builder().idFactura(1L).estado("ANULADA").build();

        when(facturaService.anularFactura(1L)).thenReturn(anulada);

        mockMvc.perform(put("/api/v1/factura/anular/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADA"));
    }

    @Test
    void anularFactura_yaAnuladaDevuelve400() throws Exception {
        when(facturaService.anularFactura(1L))
                .thenThrow(new BusinessException("La factura ya está anulada."));

        mockMvc.perform(put("/api/v1/factura/anular/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void eliminarFacturaDevuelve204() throws Exception {
        doNothing().when(facturaService).eliminarFactura(1L);

        mockMvc.perform(delete("/api/v1/factura/eliminar/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarFactura_noExisteDevuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Factura no encontrada: 99"))
                .when(facturaService).eliminarFactura(99L);

        mockMvc.perform(delete("/api/v1/factura/eliminar/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void listarPorUsuarioDevuelve200() throws Exception {
        Factura factura = Factura.builder().idFactura(1L).usuarioRut(12345678L).estado("EMITIDA").build();

        when(facturaService.listarPorUsuario(12345678L)).thenReturn(List.of(factura));

        mockMvc.perform(get("/api/v1/factura/usuario/12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioRut").value(12345678));
    }

    @Test
    void obtenerFacturaConUsuarioDevuelve200() throws Exception {
        Factura factura = Factura.builder().idFactura(1L).estado("EMITIDA").build();
        Map<String, Object> respuesta = Map.of("factura", factura, "usuario", Map.of("nombre", "Juan Pérez"));

        when(facturaService.obtenerFacturaConUsuario(1L)).thenReturn(respuesta);

        mockMvc.perform(get("/api/v1/factura/detalle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.nombre").value("Juan Pérez"));
    }
}
