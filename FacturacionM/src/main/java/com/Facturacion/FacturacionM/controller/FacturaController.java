package com.Facturacion.FacturacionM.controller;

import java.util.List;
import java.util.Map;

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

import com.Facturacion.FacturacionM.dto.GenerarFacturaRequest;
import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.model.Factura;
import com.Facturacion.FacturacionM.service.FacturaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/factura")
@RequiredArgsConstructor
@Tag(name = "Factura", description = "Operaciones sobre facturas")
public class FacturaController {

    private final FacturaService facturaService;

    @Operation(summary = "Listar todas las facturas")
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @GetMapping("/listarTodos")
    public ResponseEntity<List<Factura>> listarTodasFacturas() {
        return ResponseEntity.ok(facturaService.listarTodasFactura());
    }

    @Operation(summary = "Obtener factura por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Factura encontrada"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @GetMapping("/listarPorIdFactura/{idFactura}")
    public ResponseEntity<Factura> obtenerPorIdFactura(@PathVariable Long idFactura) {
        return facturaService.obtenerPorId(idFactura)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar facturas por RUT de usuario")
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @GetMapping("/usuario/{rut}")
    public ResponseEntity<List<Factura>> listarPorUsuario(@PathVariable Long rut) {
        return ResponseEntity.ok(facturaService.listarPorUsuario(rut));
    }

    @Operation(summary = "Obtener factura con datos del usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Datos obtenidos correctamente"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @GetMapping("/detalle/{idFactura}")
    public ResponseEntity<Map<String, Object>> obtenerConUsuario(@PathVariable Long idFactura) {
        return ResponseEntity.ok(facturaService.obtenerFacturaConUsuario(idFactura));
    }

    @Operation(summary = "Generar una nueva factura con sus detalles")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Factura generada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario no disponible")
    })
    @PostMapping("/generar")
    public ResponseEntity<Factura> generarFactura(@RequestBody GenerarFacturaRequest request) {
        List<DetalleFactura> detalles = request.getDetalles().stream().map(d -> {
            DetalleFactura det = new DetalleFactura();
            det.setDescripcion(d.getDescripcion());
            det.setCantidad(d.getCantidad());
            det.setPrecioUnitario(d.getPrecioUnitario());
            return det;
        }).toList();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facturaService.generarFactura(request.getUsuarioRut(), request.getPedidoId(), detalles));
    }

    @Operation(summary = "Calcular el total de una factura")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Total calculado correctamente"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @GetMapping("/total/{idFactura}")
    public ResponseEntity<Map<String, Double>> calcularTotal(@PathVariable Long idFactura) {
        return ResponseEntity.ok(Map.of("total", facturaService.calcularTotal(idFactura)));
    }

    @Operation(summary = "Anular una factura por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Factura anulada correctamente"),
        @ApiResponse(responseCode = "400", description = "La factura ya está anulada"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @PutMapping("/anular/{idFactura}")
    public ResponseEntity<Factura> anularFactura(@PathVariable Long idFactura) {
        return ResponseEntity.ok(facturaService.anularFactura(idFactura));
    }

    @Operation(summary = "Eliminar una factura por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Factura eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @DeleteMapping("/eliminar/{idFactura}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long idFactura) {
        facturaService.eliminarFactura(idFactura);
        return ResponseEntity.noContent().build();
    }
}
