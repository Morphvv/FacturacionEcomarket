package com.Facturacion.FacturacionM.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void constructorConMensaje_guardaMensaje() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Factura no encontrada: 99");

        assertEquals("Factura no encontrada: 99", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void mensajeDetalle_sePropagaCorrectamente() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Detalle no encontrado: 1");

        assertEquals("Detalle no encontrado: 1", ex.getMessage());
    }
}
