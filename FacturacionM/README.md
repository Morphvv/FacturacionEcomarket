// Java 21 | Puerto 8086 | Juan Pablo Jofre

Swagger: http://localhost:8086/doc/swagger-ui.html

---

## Generar Factura
**POST** `http://localhost:8086/api/v1/factura/generar`

```json
{
  "usuarioRut": 12345678,
  "pedidoId": 1,
  "detalles": [
    {
      "descripcion": "Producto Eco A",
      "cantidad": 2,
      "precioUnitario": 1500.0
    },
    {
      "descripcion": "Producto Eco B",
      "cantidad": 1,
      "precioUnitario": 3000.0
    }
  ]
}
```

---

## Agregar Detalle a Factura existente
**POST** `http://localhost:8086/api/v1/detalleFactura/agregarDetalle/{idFactura}`

```json
{
  "descripcion": "Producto Extra",
  "cantidad": 3,
  "precioUnitario": 800.0
}
```

---

## Otros endpoints (sin body)

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/v1/factura/listarTodos` | Listar todas las facturas |
| GET | `/api/v1/factura/listarPorIdFactura/{id}` | Obtener factura por ID |
| GET | `/api/v1/factura/usuario/{rut}` | Facturas por RUT de usuario |
| GET | `/api/v1/factura/detalle/{id}` | Factura con datos del usuario |
| GET | `/api/v1/factura/total/{id}` | Calcular total de factura |
| PUT | `/api/v1/factura/anular/{id}` | Anular factura |
| DELETE | `/api/v1/factura/eliminar/{id}` | Eliminar factura |
| GET | `/api/v1/detalleFactura/listarTodos` | Listar todos los detalles |
| GET | `/api/v1/detalleFactura/listarPorIdDetalleF/{id}` | Detalle por ID |
| GET | `/api/v1/detalleFactura/listarPorFactura/{idFactura}` | Detalles por factura |
| PUT | `/api/v1/detalleFactura/subtotal/{id}` | Recalcular subtotal |
| DELETE | `/api/v1/detalleFactura/eliminarDetalleFactura/{id}` | Eliminar detalle |
