package com.Facturacion.FacturacionM.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;

class BusinessExceptionTest {

    @Test
    void constructorConMensaje_guardaMensaje() {
        BusinessException ex = new BusinessException("El microservicio de usuarios no está disponible.");

        assertEquals("El microservicio de usuarios no está disponible.", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void mensajeVacio_sePropagaCorrectamente() {
        BusinessException ex = new BusinessException("");

        assertEquals("", ex.getMessage());
    }
}
