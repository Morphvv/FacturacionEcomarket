# Facturación EcoMarket

Microservicio de facturación del ecosistema **EcoMarket**. Su función es generar facturas a partir de un pedido y un usuario, registrar el detalle de los productos, calcular subtotales y totales, consultar facturas emitidas y permitir su anulación.

Antes de emitir una factura, el servicio consulta el microservicio de **Usuarios** mediante OpenFeign para verificar que el RUT se encuentre disponible. Si el servicio externo no responde, Resilience4j activa un fallback y la operación termina con un error de negocio controlado.

**Autor:** Juan Pablo Jofre

---

## Funcionalidades principales

- Generar facturas asociadas a un pedido y a un usuario.
- Crear automáticamente el número de factura con formato `FCT-XXXXXXXX`.
- Registrar la fecha de emisión y el estado inicial `EMITIDA`.
- Calcular el subtotal de cada producto.
- Calcular el total general de la factura.
- Consultar facturas por ID o por RUT del usuario.
- Obtener una factura junto con los datos del usuario.
- Agregar nuevos detalles a una factura existente.
- Anular y eliminar facturas.
- Manejar fallos del microservicio de Usuarios mediante circuit breaker y fallback.
- Documentar la API con Swagger/OpenAPI.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.4.5
- Spring Web
- Spring Data JPA
- MySQL
- OpenFeign
- Resilience4j
- Lombok
- Swagger / OpenAPI
- Maven
- JUnit
- H2 para pruebas
- JaCoCo para cobertura

---

## Dominio

### Factura

Representa el documento principal de facturación.

| Campo | Descripción |
|---|---|
| `idFactura` | Identificador autogenerado |
| `numero` | Número único generado con formato `FCT-XXXXXXXX` |
| `fechaEmision` | Fecha de creación de la factura |
| `total` | Suma de los subtotales |
| `estado` | Estado de la factura: `EMITIDA` o `ANULADA` |
| `pedidoId` | Identificador del pedido asociado |
| `usuarioRut` | RUT del usuario asociado |
| `detalles` | Productos o conceptos incluidos en la factura |

### DetalleFactura

Representa cada producto o concepto facturado.

| Campo | Descripción |
|---|---|
| `idDetalleFactura` | Identificador autogenerado |
| `descripcion` | Nombre o descripción del producto |
| `cantidad` | Cantidad facturada |
| `precioUnitario` | Precio por unidad |
| `subtotal` | Resultado de `cantidad × precioUnitario` |
| `factura` | Factura a la que pertenece el detalle |

---

## Requisitos

Antes de iniciar el proyecto se necesita:

- JDK 21
- MySQL
- Maven o Maven Wrapper
- Microservicio de Usuarios disponible para generar facturas

---

## Configuración

El archivo principal de configuración se encuentra en:

```text
FacturacionM/src/main/resources/application.properties
```

Configuración actual del repositorio:

```properties
spring.application.name=FacturacionM

server.port=8888

spring.datasource.url=jdbc:mysql://localhost:3306/facturacionDB?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

spring.cloud.openfeign.circuitbreaker.enabled=true
usuario.service.url=http://localhost:9090

springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/doc/swagger-ui.html
springdoc.packages-to-scan=com.Facturacion.FacturacionM.controller
```

> El repositorio configura actualmente el puerto `8888`. Si el ecosistema EcoMarket utiliza el puerto `8086` para Facturación, cambia `server.port=8888` por `server.port=8086` y ajusta las URLs de los ejemplos.

### Base de datos

Crear la base de datos antes de ejecutar:

```sql
CREATE DATABASE facturacionDB;
```

Las tablas se crean o actualizan automáticamente mediante:

```properties
spring.jpa.hibernate.ddl-auto=update
```

### Microservicio de Usuarios

Facturación consulta el siguiente endpoint:

```text
GET http://localhost:9090/api/v1/usuarios/buscar/{rut}
```

La dirección puede modificarse mediante:

```properties
usuario.service.url=http://localhost:9090
```

---

## Ejecución

Clonar el repositorio:

```bash
git clone https://github.com/Morphvv/FacturacionEcomarket.git
cd FacturacionEcomarket/FacturacionM
```

### Windows

```powershell
.\mvnw.cmd clean spring-boot:run
```

### Linux o macOS

```bash
./mvnw clean spring-boot:run
```

También se puede compilar y ejecutar el archivo JAR:

```bash
./mvnw clean package
java -jar target/FacturacionM-0.0.1-SNAPSHOT.jar
```

---

## Documentación Swagger

Con el puerto configurado actualmente:

```text
http://localhost:8888/doc/swagger-ui.html
```

Documentación OpenAPI en formato JSON:

```text
http://localhost:8888/v3/api-docs
```

---

# Endpoints

URL base utilizada en los ejemplos:

```text
http://localhost:8888
```

## Facturas

### Generar una factura

```http
POST /api/v1/factura/generar
```

Body:

```json
{
  "usuarioRut": 18765432,
  "pedidoId": 31,
  "detalles": [
    {
      "descripcion": "Arroz 1 kg",
      "cantidad": 2,
      "precioUnitario": 2490
    },
    {
      "descripcion": "Aceite vegetal 1 L",
      "cantidad": 2,
      "precioUnitario": 8500
    }
  ]
}
```

Al generar la factura, el servicio:

1. Consulta el RUT en el microservicio de Usuarios.
2. Verifica que exista al menos un detalle.
3. Genera automáticamente el número de factura.
4. Asigna la fecha actual.
5. Establece el estado inicial como `EMITIDA`.
6. Calcula cada subtotal.
7. Calcula y guarda el total general.

Ejemplo de respuesta:

```json
{
  "idFactura": 1,
  "numero": "FCT-A1B2C3D4",
  "fechaEmision": "2026-07-15",
  "total": 21980,
  "estado": "EMITIDA",
  "pedidoId": 31,
  "usuarioRut": 18765432,
  "detalles": [
    {
      "idDetalleFactura": 1,
      "descripcion": "Arroz 1 kg",
      "cantidad": 2,
      "precioUnitario": 2490,
      "subtotal": 4980
    },
    {
      "idDetalleFactura": 2,
      "descripcion": "Aceite vegetal 1 L",
      "cantidad": 2,
      "precioUnitario": 8500,
      "subtotal": 17000
    }
  ]
}
```

No es necesario enviar `idFactura`, `numero`, `fechaEmision`, `total`, `estado`, `idDetalleFactura` ni `subtotal`, porque el servicio los genera o calcula.

---

### Listar todas las facturas

```http
GET /api/v1/factura/listarTodos
```

---

### Buscar una factura por ID

```http
GET /api/v1/factura/listarPorIdFactura/{idFactura}
```

Ejemplo:

```text
GET /api/v1/factura/listarPorIdFactura/1
```

---

### Listar facturas por RUT

```http
GET /api/v1/factura/usuario/{rut}
```

Ejemplo:

```text
GET /api/v1/factura/usuario/18765432
```

---

### Obtener factura con datos del usuario

```http
GET /api/v1/factura/detalle/{idFactura}
```

La respuesta contiene dos elementos:

```json
{
  "factura": {
    "idFactura": 1,
    "numero": "FCT-A1B2C3D4",
    "estado": "EMITIDA"
  },
  "usuario": {
    "rut": 18765432,
    "nombre": "Valentina"
  }
}
```

---

### Calcular el total de una factura

```http
GET /api/v1/factura/total/{idFactura}
```

Ejemplo de respuesta:

```json
{
  "total": 21980
}
```

---

### Anular una factura

```http
PUT /api/v1/factura/anular/{idFactura}
```

El estado cambia de:

```text
EMITIDA → ANULADA
```

Si la factura ya está anulada, el servicio devuelve un error de negocio.

---

### Eliminar una factura

```http
DELETE /api/v1/factura/eliminar/{idFactura}
```

Respuesta esperada:

```text
204 No Content
```

---

## Detalles de factura

### Listar todos los detalles

```http
GET /api/v1/detalleFactura/listarTodos
```

---

### Buscar un detalle por ID

```http
GET /api/v1/detalleFactura/listarPorIdDetalleF/{idDetalleFactura}
```

---

### Listar detalles de una factura

```http
GET /api/v1/detalleFactura/listarPorFactura/{idFactura}
```

---

### Agregar un detalle a una factura existente

```http
POST /api/v1/detalleFactura/agregarDetalle/{idFactura}
```

Body:

```json
{
  "descripcion": "Producto adicional",
  "cantidad": 3,
  "precioUnitario": 800
}
```

El subtotal se calcula automáticamente:

```text
3 × 800 = 2400
```

> Este endpoint guarda el nuevo detalle y calcula su subtotal. En la implementación actual no actualiza automáticamente el campo `total` almacenado en la factura.

---

### Recalcular el subtotal de un detalle

```http
PUT /api/v1/detalleFactura/subtotal/{idDetalleFactura}
```

---

### Eliminar un detalle

```http
DELETE /api/v1/detalleFactura/eliminarDetalleFactura/{idDetalleFactura}
```

Respuesta esperada:

```text
204 No Content
```

---

## Respuestas HTTP principales

| Código | Significado |
|---|---|
| `200 OK` | Consulta o actualización realizada correctamente |
| `201 Created` | Factura o detalle creado correctamente |
| `204 No Content` | Recurso eliminado correctamente |
| `400 Bad Request` | Error de negocio o datos inválidos |
| `404 Not Found` | Factura o detalle no encontrado |
| `500 Internal Server Error` | Error interno no controlado |

---

## Circuit breaker y fallback

El cliente Feign consulta el microservicio de Usuarios:

```java
@GetMapping("/api/v1/usuarios/buscar/{rut}")
Map<String, Object> obtenerUsuarioPorRut(@PathVariable("rut") Long rut);
```

Cuando el servicio no está disponible, el fallback devuelve datos controlados con el nombre:

```text
Usuario no disponible
```

Al detectar esa respuesta durante la generación de una factura, el servicio evita continuar y genera el mensaje:

```text
El microservicio de usuarios no está disponible.
```

Esto impide emitir una factura sin validar previamente al usuario.

---

## Pruebas

Ejecutar todas las pruebas:

### Windows

```powershell
.\mvnw.cmd test
```

### Linux o macOS

```bash
./mvnw test
```

Generar el reporte de cobertura JaCoCo:

```bash
./mvnw clean test
```

El reporte HTML queda disponible en:

```text
target/site/jacoco/index.html
```

---

## Estructura principal

```text
FacturacionM/
├── src/
│   ├── main/
│   │   ├── java/com/Facturacion/FacturacionM/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── feign/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── FacturacionMApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## Flujo recomendado para probar

1. Iniciar MySQL.
2. Crear la base de datos `facturacionDB`.
3. Iniciar el microservicio de Usuarios en el puerto configurado.
4. Iniciar Facturación.
5. Abrir Swagger.
6. Verificar que el RUT exista en Usuarios.
7. Ejecutar `POST /api/v1/factura/generar`.
8. Consultar la factura mediante `GET /api/v1/factura/listarTodos`.
9. Probar el cálculo del total y la anulación.
