package com.Facturacion.FacturacionM.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Facturacion.FacturacionM.model.Factura;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByUsuarioRut(Long usuarioRut);
    Optional<Factura> findByNumero(String numero);
    List<Factura> findByEstado(String estado);

}
