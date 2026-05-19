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

import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.model.Factura;
import com.Facturacion.FacturacionM.service.FacturaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/factura")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @GetMapping("/listarTodos")
    public ResponseEntity<List<Factura>> listarTodasFacturas() {
        return ResponseEntity.ok(facturaService.listarTodasFactura());
    }

    @GetMapping("/listarPorIdFactura/{idFactura}")
    public ResponseEntity<Factura> obtenerPorIdFactura(@PathVariable Long idFactura) {
        return facturaService.obtenerPorId(idFactura)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{rut}")
    public ResponseEntity<List<Factura>> listarPorUsuario(@PathVariable Long rut) {
        return ResponseEntity.ok(facturaService.listarPorUsuario(rut));
    }

    @GetMapping("/detalle/{idFactura}")
    public ResponseEntity<Map<String, Object>> obtenerConUsuario(@PathVariable Long idFactura) {
        return ResponseEntity.ok(facturaService.obtenerFacturaConUsuario(idFactura));
    }

    @PostMapping("/generar")
    public ResponseEntity<Factura> generarFactura(@RequestBody Map<String, Object> body) {
        Long usuarioRut = ((Number) body.get("usuarioRut")).longValue();
        int pedidoId = ((Number) body.get("pedidoId")).intValue();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("detalles");

        List<DetalleFactura> detalles = raw.stream().map(d -> {
            DetalleFactura det = new DetalleFactura();
            det.setDescripcion((String) d.get("descripcion"));
            det.setCantidad(((Number) d.get("cantidad")).intValue());
            det.setPrecioUnitario(((Number) d.get("precioUnitario")).doubleValue());
            return det;
        }).toList();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facturaService.generarFactura(usuarioRut, pedidoId, detalles));
    }

    @GetMapping("/total/{idFactura}")
    public ResponseEntity<Map<String, Double>> calcularTotal(@PathVariable Long idFactura) {
        return ResponseEntity.ok(Map.of("total", facturaService.calcularTotal(idFactura)));
    }

    @PutMapping("/anular/{idFactura}")
    public ResponseEntity<Factura> anularFactura(@PathVariable Long idFactura) {
        return ResponseEntity.ok(facturaService.anularFactura(idFactura));
    }

    @DeleteMapping("/eliminar/{idFactura}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long idFactura) {
        facturaService.eliminarFactura(idFactura);
        return ResponseEntity.noContent().build();
    }
}
