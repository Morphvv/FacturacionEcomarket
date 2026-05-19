package com.Facturacion.FacturacionM.feign.fallback;

import com.Facturacion.FacturacionM.feign.ClienteFeignClient;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ClienteFeignFallback implements ClienteFeignClient {

    @Override
    public Map<String, Object> obtenerUsuarioPorRut(Long rut) {
        return Map.of(
            "rut",           rut,
            "nombre",        "Usuario no disponible",
            "apellido",      "N/A",
            "email",         "N/A",
            "telefono",      "N/A",
            "estadoUsuario", "N/A"
        );
    }
}
