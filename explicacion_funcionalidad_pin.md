# Guía de Integración: PIN de Seguridad, Estado ENTREGADO y WhatsApp (Frontend)

Este documento detalla el funcionamiento del flujo de seguridad para la entrega de motocicletas en el taller. El backend se encarga de la generación, expiración y validación del PIN de seguridad, además de proveer una URL pre-formateada de WhatsApp para que el frontend pueda notificar al cliente con un solo clic.

---

## 1. Ciclo de Vida del PIN

1. **Generación**: Cuando una orden pasa al estado `SERVICE_TERMINADO` (ya sea al crear la orden o al actualizarla), el backend autogenera un **PIN aleatorio de 6 dígitos** y establece una validez de **15 minutos**.
2. **Exposición**: El PIN, su fecha de expiración y un enlace pre-formateado para WhatsApp se envían al frontend en el DTO de respuesta.
3. **Notificación (WhatsApp)**: El frontend utiliza el campo `whatsappUrl` para abrir la ventana de chat del cliente con el mensaje ya escrito.
4. **Validación**: Para cambiar el estado de la orden a `ENTREGADO`, el administrador debe proveer el PIN ingresado por el cliente. Si coincide y no ha expirado, el cambio de estado se aprueba.
5. **Limpieza**: Tras una entrega exitosa (`ENTREGADO`), el backend limpia de la base de datos los campos `pin` y `fechaExpiracionPin` (se guardan como `null`) por motivos de seguridad.

---

## 2. Estructura de Datos en la Respuesta (`OrdenResponseDTO`)

Al consultar u operar sobre una orden que tiene un PIN activo (estado `SERVICE_TERMINADO`), recibirás campos adicionales en el JSON de respuesta:

```json
{
  "id": 12,
  "idMoto": 3,
  "marcaModeloMoto": "Honda CB 250 Twister",
  "patenteMoto": "A123BCD",
  "telefonoContacto": "3704093764",
  "estado": "SERVICE_TERMINADO",
  "notas": "Frenos reparados y service de transmisión completo.",
  "pin": "748392",
  "fechaExpiracionPin": "2026-06-23T22:15:30",
  "whatsappUrl": "https://wa.me/3704093764?text=%C2%A1Hola%21+Tu+motocicleta+est%C3%A1+lista+para+retirar+en+el+taller.+Tu+PIN+de+seguridad+es%3A+748392.+Recuerda+que+este+PIN+expira+en+15+minutos."
}
```

### Campos Clave:
* **`pin`**: Código de 6 dígitos que se le solicita al cliente al retirar la moto.
* **`fechaExpiracionPin`**: Fecha y hora local en que expira el PIN.
* **`whatsappUrl`**: Enlace listo para usar en el frontend con el número del cliente sanitizado y el mensaje codificado.

---

## 3. Endpoints del Backend

### 3.1. Cambiar Estado de la Orden
Para realizar transiciones de estado básicas.
* **Método**: `PATCH`
* **Ruta**: `/api/ordenes/{id}/estado`
* **Parámetros de Query**:
  * `estado` (String, Obligatorio): Nuevo estado del enum (`MOTO_INGRESADA`, `REALIZANDOSE_SERVICE`, `SERVICE_TERMINADO`, `ENTREGADO`).
  * `pin` (String, Condicional): Obligatorio **solo** si el nuevo estado es `ENTREGADO`.

**Ejemplo de llamada para entregar moto:**
`PATCH http://localhost:8080/api/ordenes/12/estado?estado=ENTREGADO&pin=748392`

---

### 3.2. Regenerar / Reenviar PIN
Si el PIN expira o el cliente no lo recibe, el administrador puede solicitar un PIN nuevo. Esto generará un código nuevo y refrescará el temporizador por otros 15 minutos.
* **Método**: `POST`
* **Ruta**: `/api/ordenes/{id}/reenviar-pin`
* **Respuesta**: Retorna la orden con el nuevo `pin`, nueva `fechaExpiracionPin` y la nueva `whatsappUrl` correspondiente.

---

## 4. Implementación Sugerida en el Frontend

### A. Notificar al Cliente (Enviar WhatsApp)
El botón "Notificar Cliente" o "Enviar PIN" solo necesita abrir el link provisto por el backend en una nueva pestaña:

```typescript
const enviarNotificacionPin = (orden: { whatsappUrl?: string }) => {
  if (orden.whatsappUrl) {
    // Abre WhatsApp Web / Desktop / Móvil con el chat prellenado
    window.open(orden.whatsappUrl, '_blank');
  } else {
    console.warn("La orden no cuenta con un PIN activo para notificar.");
  }
};
```

### B. Validación de Retiro (Pasar a ENTREGADO)
En tu formulario o modal de entrega:

```typescript
const confirmarEntrega = async (ordenId: number, pinIngresado: string) => {
  try {
    const response = await axios.patch(
      `http://localhost:8080/api/ordenes/${ordenId}/estado`,
      null, // sin body
      {
        params: {
          estado: 'ENTREGADO',
          pin: pinIngresado
        },
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );
    alert("¡Entrega confirmada y registrada con éxito!");
  } catch (error) {
    // El backend retorna 400 Bad Request si el PIN es incorrecto o si ya expiró
    alert(error.response?.data?.message || "Error al validar el PIN.");
  }
};
```
