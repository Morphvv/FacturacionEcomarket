package com.Facturacion.FacturacionM.dto;

import java.util.List;

import lombok.Data;

@Data
public class GenerarFacturaRequest {

    private Long usuarioRut;
    private int pedidoId;
    private List<DetalleRequest> detalles;

    @Data
    public static class DetalleRequest {
        private String descripcion;
        private int cantidad;
        private double precioUnitario;
    }
}
