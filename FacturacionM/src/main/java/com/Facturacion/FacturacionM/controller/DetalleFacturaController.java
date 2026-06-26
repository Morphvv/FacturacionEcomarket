package com.Facturacion.FacturacionM.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.service.DetalleFacturaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/detalleFactura")
@RequiredArgsConstructor
@Tag(name = "DetalleFactura", description = "Operaciones sobre detalles de facturación")
public class DetalleFacturaController {

    private final DetalleFacturaService detalleFacturaService;

    @Operation(summary = "Listar todos los detalles de factura")
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @GetMapping("/listarTodos")
    public ResponseEntity<List<DetalleFactura>> listarTodosDetallesF() {
        return ResponseEntity.ok(detalleFacturaService.listarTodosDetallesF());
    }

    @Operation(summary = "Obtener detalle por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @GetMapping("/listarPorIdDetalleF/{idDetalleFactura}")
    public ResponseEntity<DetalleFactura> obtenerPorIdDetalleF(@PathVariable Long idDetalleFactura) {
        return detalleFacturaService.obtenerPorId(idDetalleFactura)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar detalles por ID de factura")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @GetMapping("/listarPorFactura/{idFactura}")
    public ResponseEntity<List<DetalleFactura>> listarPorFactura(@PathVariable Long idFactura) {
        return ResponseEntity.ok(detalleFacturaService.listarPorFactura(idFactura));
    }

    @Operation(summary = "Agregar un detalle a una factura existente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Detalle agregado correctamente"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @PostMapping("/agregarDetalle/{idFactura}")
    public ResponseEntity<DetalleFactura> agregarDetalle(@PathVariable Long idFactura,
            @RequestBody DetalleFactura detalleFactura) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(detalleFacturaService.agregarDetalle(idFactura, detalleFactura));
    }

    @Operation(summary = "Recalcular el subtotal de un detalle")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subtotal calculado correctamente"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @PutMapping("/subtotal/{idDetalleFactura}")
    public ResponseEntity<DetalleFactura> calcularSubTotal(@PathVariable Long idDetalleFactura) {
        return ResponseEntity.ok(detalleFacturaService.calcularSubTotal(idDetalleFactura));
    }

    @Operation(summary = "Eliminar un detalle de factura por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Detalle eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @DeleteMapping("/eliminarDetalleFactura/{idDetalleFactura}")
    public ResponseEntity<Void> eliminarDetalleFactura(@PathVariable Long idDetalleFactura) {
        detalleFacturaService.eliminarDetalleFactura(idDetalleFactura);
        return ResponseEntity.noContent().build();
    }
}
