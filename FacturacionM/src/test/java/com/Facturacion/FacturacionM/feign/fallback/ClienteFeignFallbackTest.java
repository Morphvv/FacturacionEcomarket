package com.Facturacion.FacturacionM.feign.fallback;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class ClienteFeignFallbackTest {

    private final ClienteFeignFallback fallback = new ClienteFeignFallback();

    @Test
    void obtenerUsuarioPorRut_retornaRutOriginal() {
        Map<String, Object> resultado = fallback.obtenerUsuarioPorRut(12345678L);

        assertNotNull(resultado);
        assertEquals(12345678L, resultado.get("rut"));
    }

    @Test
    void obtenerUsuarioPorRut_retornaNombreNoDisponible() {
        Map<String, Object> resultado = fallback.obtenerUsuarioPorRut(12345678L);

        assertEquals("Usuario no disponible", resultado.get("nombre"));
        assertEquals("N/A", resultado.get("apellido"));
        assertEquals("N/A", resultado.get("email"));
        assertEquals("N/A", resultado.get("telefono"));
        assertEquals("N/A", resultado.get("estadoUsuario"));
    }

    @Test
    void obtenerUsuarioPorRut_rutDistinto_retornaRutCorrecto() {
        Map<String, Object> resultado = fallback.obtenerUsuarioPorRut(99L);

        assertEquals(99L, resultado.get("rut"));
        assertEquals("Usuario no disponible", resultado.get("nombre"));
    }
}
