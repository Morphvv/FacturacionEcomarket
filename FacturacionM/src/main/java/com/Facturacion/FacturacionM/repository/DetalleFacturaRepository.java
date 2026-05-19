package com.Facturacion.FacturacionM.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Facturacion.FacturacionM.model.DetalleFactura;
import com.Facturacion.FacturacionM.model.Factura;

@Repository
public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {

    List<DetalleFactura> findByFactura(Factura factura);

}
