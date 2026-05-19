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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/detalleFactura")
@RequiredArgsConstructor
public class DetalleFacturaController {

    private final DetalleFacturaService detalleFacturaService;

    @GetMapping("/listarTodos")
    public ResponseEntity<List<DetalleFactura>> listarTodosDetallesF() {
        return ResponseEntity.ok(detalleFacturaService.listarTodosDetallesF());
    }

    @GetMapping("/listarPorIdDetalleF/{idDetalleFactura}")
    public ResponseEntity<DetalleFactura> obtenerPorIdDetalleF(@PathVariable Long idDetalleFactura) {
        return detalleFacturaService.obtenerPorId(idDetalleFactura)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/listarPorFactura/{idFactura}")
    public ResponseEntity<List<DetalleFactura>> listarPorFactura(@PathVariable Long idFactura) {
        return ResponseEntity.ok(detalleFacturaService.listarPorFactura(idFactura));
    }

    @PostMapping("/agregarDetalle/{idFactura}")
    public ResponseEntity<DetalleFactura> agregarDetalle(@PathVariable Long idFactura,
            @RequestBody DetalleFactura detalleFactura) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(detalleFacturaService.agregarDetalle(idFactura, detalleFactura));
    }

    @PutMapping("/subtotal/{idDetalleFactura}")
    public ResponseEntity<DetalleFactura> calcularSubTotal(@PathVariable Long idDetalleFactura) {
        return ResponseEntity.ok(detalleFacturaService.calcularSubTotal(idDetalleFactura));
    }

    @DeleteMapping("/eliminarDetalleFactura/{idDetalleFactura}")
    public ResponseEntity<Void> eliminarDetalleFactura(@PathVariable Long idDetalleFactura) {
        detalleFacturaService.eliminarDetalleFactura(idDetalleFactura);
        return ResponseEntity.noContent().build();
    }
}
