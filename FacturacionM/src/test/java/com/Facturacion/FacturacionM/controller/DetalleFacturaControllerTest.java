package com.Facturacion.FacturacionM.controller;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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

import com.Facturacion.FacturacionM.exception.ResourceNotFoundException;
import com.Facturacion.FacturacionM.feign.ClienteFeignClient;
import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.service.DetalleFacturaService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DetalleFacturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetalleFacturaService detalleFacturaService;

    @MockitoBean
    private ClienteFeignClient clienteFeignClient;

    @Test
    void listarTodosDevuelve200() throws Exception {
        DetalleFactura d1 = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto A").cantidad(2)
                .precioUnitario(500.0).subtotal(1000.0).build();

        when(detalleFacturaService.listarTodosDetallesF()).thenReturn(List.of(d1));

        mockMvc.perform(get("/api/v1/detalleFactura/listarTodos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Producto A"))
                .andExpect(jsonPath("$[0].subtotal").value(1000.0));
    }

    @Test
    void obtenerPorIdDevuelve200() throws Exception {
        DetalleFactura detalle = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto B").cantidad(3)
                .precioUnitario(200.0).subtotal(600.0).build();

        when(detalleFacturaService.obtenerPorId(1L)).thenReturn(Optional.of(detalle));

        mockMvc.perform(get("/api/v1/detalleFactura/listarPorIdDetalleF/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDetalleFactura").value(1))
                .andExpect(jsonPath("$.descripcion").value("Producto B"));
    }

    @Test
    void obtenerPorIdDevuelve404() throws Exception {
        when(detalleFacturaService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/detalleFactura/listarPorIdDetalleF/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorFacturaDevuelve200() throws Exception {
        DetalleFactura detalle = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto C").subtotal(800.0).build();

        when(detalleFacturaService.listarPorFactura(1L)).thenReturn(List.of(detalle));

        mockMvc.perform(get("/api/v1/detalleFactura/listarPorFactura/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].descripcion").value("Producto C"));
    }

    @Test
    void listarPorFactura_noExisteDevuelve404() throws Exception {
        when(detalleFacturaService.listarPorFactura(99L))
                .thenThrow(new ResourceNotFoundException("Factura no encontrada: 99"));

        mockMvc.perform(get("/api/v1/detalleFactura/listarPorFactura/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void agregarDetalleDevuelve201() throws Exception {
        DetalleFactura guardado = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto D").cantidad(2)
                .precioUnitario(400.0).subtotal(800.0).build();

        when(detalleFacturaService.agregarDetalle(anyLong(), any(DetalleFactura.class)))
                .thenReturn(guardado);

        String json = """
                {
                  "descripcion": "Producto D",
                  "cantidad": 2,
                  "precioUnitario": 400.0
                }
                """;

        mockMvc.perform(post("/api/v1/detalleFactura/agregarDetalle/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descripcion").value("Producto D"))
                .andExpect(jsonPath("$.subtotal").value(800.0));
    }

    @Test
    void agregarDetalle_facturaNoExisteDevuelve404() throws Exception {
        when(detalleFacturaService.agregarDetalle(anyLong(), any(DetalleFactura.class)))
                .thenThrow(new ResourceNotFoundException("Factura no encontrada: 99"));

        String json = """
                {
                  "descripcion": "X",
                  "cantidad": 1,
                  "precioUnitario": 100.0
                }
                """;

        mockMvc.perform(post("/api/v1/detalleFactura/agregarDetalle/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void calcularSubTotalDevuelve200() throws Exception {
        DetalleFactura actualizado = DetalleFactura.builder()
                .idDetalleFactura(1L).descripcion("Producto E").cantidad(5)
                .precioUnitario(300.0).subtotal(1500.0).build();

        when(detalleFacturaService.calcularSubTotal(1L)).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/detalleFactura/subtotal/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(1500.0));
    }

    @Test
    void eliminarDetalleDevuelve204() throws Exception {
        doNothing().when(detalleFacturaService).eliminarDetalleFactura(1L);

        mockMvc.perform(delete("/api/v1/detalleFactura/eliminarDetalleFactura/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarDetalle_noExisteDevuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Detalle no encontrado: 99"))
                .when(detalleFacturaService).eliminarDetalleFactura(99L);

        mockMvc.perform(delete("/api/v1/detalleFactura/eliminarDetalleFactura/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
