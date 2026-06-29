package com.Facturacion.FacturacionM.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "factura")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFactura;

    @Column(nullable = false, unique = true, length = 50)
    private String numero;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private double total;

    @Column(nullable = false, length = 50)
    private String estado;

    @Column(name = "pedido_id", nullable = false)
    private int pedidoId;

    @Column(name = "usuario_rut", nullable = false)
    private Long usuarioRut;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL,
        orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @JsonManagedReference
    private List<DetalleFactura> detalles = new ArrayList<>();

}
