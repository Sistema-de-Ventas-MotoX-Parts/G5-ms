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
  "contrasenia": "claveSegura123"
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