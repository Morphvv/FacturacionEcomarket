package com.Facturacion.FacturacionM.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.Facturacion.FacturacionM.feign.fallback.ClienteFeignFallback;

@FeignClient(
    name = "usuario-service",
    url = "${usuario.service.url}",
    fallback = ClienteFeignFallback.class
)
public interface ClienteFeignClient {

    @GetMapping("/api/v1/usuarios/buscar/{rut}")
    Map<String, Object> obtenerUsuarioPorRut(@PathVariable("rut") Long rut);

}
