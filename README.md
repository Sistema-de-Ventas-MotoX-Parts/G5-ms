# Sistema de Ventas MotoX Parts - Backend (G5-ms)

Este proyecto es una API REST desarrollada con **Java 21** y **Spring Boot 4.1.0** para gestionar productos, categorías, imágenes y autenticación de usuarios.

---

## 1. Estructura y Capas del Proyecto

El código está estructurado bajo una arquitectura de capas (MVC) que separa responsabilidades:

*   **`model`**: Entidades mapeadas directamente a las tablas de la base de datos MySQL (con validaciones de campos).
*   **`repository`**: Interfaces que extienden `JpaRepository` para interactuar con la base de datos (con Query Methods automáticos).
*   **`service`**: Contiene la lógica de negocio (validaciones, hashes con BCrypt, interacción con Cloudinary, etc.).
*   **`controller`**: Controladores que exponen los endpoints HTTP para el frontend.
*   **`config`**: Configuraciones globales (por ejemplo, Cloudinary).

---

## 2. Requisitos Previos y Configuración

### Base de Datos
La aplicación se conecta a una base de datos MySQL llamada `motosx`. Al iniciar el servidor, las tablas se crearán o actualizarán automáticamente debido a la configuración `spring.jpa.hibernate.ddl-auto=update` en `application.properties`.

### Variables de Entorno
Crea un archivo llamado `.env` en la raíz del proyecto (al mismo nivel que este `README.md` y `pom.xml`) y agrega tus credenciales de Cloudinary:

```env
CLOUDINARY_URL=cloudinary://TU_API_KEY:TU_API_SECRET@TU_CLOUD_NAME
```

---

## 3. Guía de Endpoints de la API (Colección Postman)

A continuación se detallan todas las solicitudes disponibles con sus respectivos métodos HTTP, URLs y cuerpos en formato JSON.

### A. Autenticación y Gestión de Usuarios

#### 1. Registrar un Usuario
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/auth/register`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "nombre": "Juan Pérez",
  "email": "juan.perez@example.com",
  "contrasenia": "claveSegura123",
  "direccion": "Av. Siempreviva 742"
}
```

#### 2. Iniciar Sesión (Login)
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/auth/login`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "email": "juan.perez@example.com",
  "contrasenia": "claveSegura123"
}
```

#### 3. Obtener Mi Perfil (Autenticado)
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/usuarios/perfil`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>` (o mediante cookie `token_jwt`)

#### 4. Editar Mi Perfil (Autenticado)
*   **Método**: `PUT`
*   **URL**: `http://localhost:8080/api/usuarios/perfil`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>` (o mediante cookie `token_jwt`)
*   **Body (JSON)**:
```json
{
  "nombre": "Juan Pérez Editado",
  "contraseniaActual": "claveSegura123",
  "nuevaContrasenia": "nuevaClave456",
  "direccion": "Av. Siempreviva 742 (Modificada)",
  "imagenUrl": "https://res.cloudinary.com/dummy/image/upload/v1/ejemplo.jpg"
}
```
*Nota: Solo el nombre, la dirección y la imagen son modificables sin requerir contraseñas. Si se envía `nuevaContrasenia`, `contraseniaActual` es obligatoria para validar el cambio.*

#### 5. Eliminar Mi Perfil (Autenticado)
*   **Método**: `DELETE`
*   **URL**: `http://localhost:8080/api/usuarios/perfil`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>` (o mediante cookie `token_jwt`)

#### 6. Obtener Historial de Productos Comprados (Autenticado)
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/usuarios/perfil/productos-comprados`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>` (o mediante cookie `token_jwt`)
*   **Respuesta**: Lista en formato JSON detallando los productos adquiridos por el usuario, sus cantidades, subtotales y fecha de compra.

#### 7. Listar todos los Mecánicos Activos (Admin)
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/auth/usuarios/mecanicos`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>` (o mediante cookie `token_jwt`)
*   **Respuesta**: Lista de todos los usuarios activos que poseen el rol `MECHANIC` (incluyendo su información laboral como sueldo y horarios).
*   **Crear mecánico (Ejemplo de payload para Admin en POST /api/auth/usuarios)**:
```json
{
  "nombre": "Juan Mecánico",
  "email": "juan@mecanico.com",
  "contrasenia": "mecanico123",
  "rol": {
    "nombreRol": "MECHANIC"
  },
  "empleado": {
    "sueldo": 250000.0,
    "diasTrabajo": "Lunes a Viernes",
    "horarioTrabajo": "08:00 a 17:00",
    "diasLibres": "Sábado y Domingo"
  }
}
```

---

### B. Gestión de Categorías

#### 1. Crear una Categoría
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/categorias`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "nombre": "Accesorios y Cascos"
}
```

#### 2. Listar todas las Categorías
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/categorias`

#### 3. Obtener Categoría por ID
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/categorias/1`

#### 4. Editar Categoría
*   **Método**: `PUT`
*   **URL**: `http://localhost:8080/api/categorias/1`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "nombre": "Cascos Modificados"
}
```

#### 5. Eliminar Categoría
*   **Método**: `DELETE`
*   **URL**: `http://localhost:8080/api/categorias/1`

---

### C. Gestión de Productos

#### 1. Crear un Producto
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/productos`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "codigoSku": "SKU-PROD-001",
  "nombre": "Casco Integral Hawk",
  "descripcion": "Casco homologado con doble visor y color negro mate.",
  "precio": 89500.00,
  "stock": 10,
  "imagenUrl": "https://res.cloudinary.com/dummy/image/upload/v1/ejemplo.jpg",
  "categoria": {
    "id": 1
  },
  "activo": true
}
```

#### 2. Listar todos los Productos
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/productos`

#### 3. Obtener Producto por ID
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/productos/1`

#### 4. Editar Producto
*   **Método**: `PUT`
*   **URL**: `http://localhost:8080/api/productos/1`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "codigoSku": "SKU-PROD-001",
  "nombre": "Casco Integral Hawk - Edición Especial",
  "descripcion": "Casco homologado con doble visor antiempañante.",
  "precio": 95000.00,
  "stock": 8,
  "imagenUrl": "https://res.cloudinary.com/dummy/image/upload/v1/ejemplo.jpg",
  "categoria": {
    "id": 1
  },
  "activo": true
}
```

#### 5. Eliminar Producto
*   **Método**: `DELETE`
*   **URL**: `http://localhost:8080/api/productos/1`

---

### D. Subir Imágenes (Cloudinary)

#### 1. Subir Imagen
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/imagenes/upload`
*   **Headers**: *Dejar que Postman detecte el Content-Type automáticamente al usar form-data.*
*   **Body (form-data)**:
    *   **Key**: `file` (seleccionar tipo "File" en el desplegable de Postman).
    *   **Value**: *Selecciona una imagen local.*

---

### E. Gestión de Métodos de Pago

#### 1. Crear un Método de Pago
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/metodos-pago`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "nombre": "Tarjeta de Crédito"
}
```

#### 2. Listar todos los Métodos de Pago
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/metodos-pago`

#### 3. Obtener Método de Pago por ID
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/metodos-pago/1`

---

### F. Gestión de Facturación

#### 1. Crear una Factura
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/facturas`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "idUsuario": 1,
  "idMetodoPago": 1,
  "idOrden": null,
  "detalles": [
    {
      "idProducto": 1,
      "cantidad": 2
    }
  ],
  "detallesServicios": [
    {
      "idServicio": 1
    }
  ]
}
```
*Nota: El sistema calculará automáticamente los precios, descontará el stock y generará la fecha y estado. Si envías un `idOrden` y dejas `detalles` y `detallesServicios` vacíos (`[]`), el sistema heredará automáticamente las cantidades exactas y los servicios que usaste en la Orden.*

#### 2. Listar todas las Facturas
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/facturas`

#### 3. Obtener Factura por ID (Incluyendo Detalles)
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/facturas/1`

#### 4. Obtener Mis Facturas (Usuario Autenticado)
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/facturas/mis-facturas`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>` (o mediante cookie `token_jwt`)

#### 5. Actualizar Estado de Factura (Admin)
*   **Método**: `PATCH`
*   **URL**: `http://localhost:8080/api/facturas/1/estado?estado=CANCELADA`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>` (debe ser un Administrador)
*   **Parámetro (URL)**: `estado` (ej. `CANCELADA`, `PAGADA`).
*Nota: Si cancelas una factura, el sistema reabastece automáticamente el stock de los productos involucrados.*

---

### G. Gestión de Servicios

#### 1. Crear un Servicio
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/servicios`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "nombre": "Cambio de Aceite",
  "descripcion": "Cambio de aceite sintético y filtro.",
  "precioBase": 15000.0
}
```

#### 2. Listar todos los Servicios
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/servicios`

#### 3. Obtener Servicio por ID
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/servicios/1`

#### 4. Editar Servicio
*   **Método**: `PUT`
*   **URL**: `http://localhost:8080/api/servicios/1`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "nombre": "Cambio de Aceite Premium",
  "descripcion": "Aceite importado alta duración.",
  "precioBase": 18000.0
}
```

#### 5. Eliminar Servicio
*   **Método**: `DELETE`
*   **URL**: `http://localhost:8080/api/servicios/1`

---

### H. Gestión de Motocicletas

#### 1. Crear una Motocicleta
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/motocicletas`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "marca": "Honda",
  "modelo": "Titan 150",
  "patente": "AB123CD",
  "idUsuario": 1
}
```
*Nota: El campo `idUsuario` es opcional para poder registrar motos de clientes no fidelizados.*

#### 2. Listar todas las Motocicletas
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/motocicletas`

#### 3. Obtener Motocicleta por ID
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/motocicletas/1`

#### 4. Editar Motocicleta
*   **Método**: `PUT`
*   **URL**: `http://localhost:8080/api/motocicletas/1`
*   **Headers**: `Content-Type: application/json`
*   **Body (JSON)**:
```json
{
  "marca": "Honda",
  "modelo": "Titan 150 (Modificado)",
  "patente": "AB123CD",
  "idUsuario": null
}
```

#### 5. Eliminar Motocicleta
*   **Método**: `DELETE`
*   **URL**: `http://localhost:8080/api/motocicletas/1`

#### 6. Obtener Motocicletas por Usuario
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/motocicletas/usuario/1`

#### 7. Obtener Motocicleta por Patente
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/motocicletas/patente/AB123CD`

---

### I. Gestión de Órdenes de Taller (Requiere Admin)

#### 1. Crear una Orden
*   **Método**: `POST`
*   **URL**: `http://localhost:8080/api/ordenes`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>`
*   **Body (JSON)**:
```json
{
  "idMoto": 1,
  "telefonoContacto": "1122334455",
  "estado": "MOTO_INGRESADA",
  "notas": "Mantenimiento general",
  "idMecanico": 3,
  "idUsuario": 1,
  "idMetodoPago": 1,
  "servicios": [
    { "idServicio": 1 }
  ],
  "productos": [
    { "idProducto": 1, "cantidad": 3 }
  ]
}
```
*Nota: Al crear una orden, el sistema descuenta el stock de los productos para reservarlos. La factura se generará de manera automática y diferida en estado PENDIENTE cuando el estado de la orden sea cambiado a `SERVICE_TERMINADO`.*

#### 2. Listar todas las Órdenes
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/ordenes` (o filtrar por mecánico: `http://localhost:8080/api/ordenes?idMecanico=3`)
*   **Headers**: `Authorization: Bearer <TU_TOKEN>`

#### 3. Obtener Orden por ID
*   **Método**: `GET`
*   **URL**: `http://localhost:8080/api/ordenes/1`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>`

#### 4. Editar Orden (Completa)
*   **Método**: `PUT`
*   **URL**: `http://localhost:8080/api/ordenes/1`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>`
*   **Body (JSON)**: Igual que al crear.

#### 5. Actualizar Estado de Orden
*   **Método**: `PATCH`
*   **URL**: `http://localhost:8080/api/ordenes/1/estado?estado=SERVICE_TERMINADO`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>`
*   **Parámetro (URL)**: `estado` (ej. `MOTO_INGRESADA`, `REALIZANDOSE_SERVICE`, `SERVICE_TERMINADO`).
*Nota: Al cambiar el estado a `SERVICE_TERMINADO`, si aún no se había generado la factura de esta orden, el sistema la creará automáticamente en estado PENDIENTE recopilando los productos y servicios finales.*

#### 6. Eliminar Orden
*   **Método**: `DELETE`
*   **URL**: `http://localhost:8080/api/ordenes/1`
*   **Headers**: `Authorization: Bearer <TU_TOKEN>`