Esta interfaz está diseñada para gestionar los mensajes enviados por la aplicación. A continuación se detallan las
diferentes funcionalidades y secciones de la interfaz.

#### Barra de Búsqueda y filtros

En la parte superior de la interfaz, se encuentran varios campos de búsqueda y filtros:

- **Canal**: Botones para filtrar por el canal de envío del mensaje.
- **Fecha de inicio**: Campo para buscar los mensajes por su fecha de inicio.
- **Estado**: Botones de opción para filtrar contenidos por su estado.

#### Acciones Disponibles

- **Imprimir**: Botón de impresión para generar un listado de todos los mensjaes.

#### Tabla de mensajes

La tabla principal muestra una lista de mensajes con la siguiente información:

- **Canal**: Canal por el que se ha enviado el mensaje.
- **Estado**: Estado del mensaje.
- **Fecha de envio**: Fecha en la que se envió el mensaje.
- **Destinario**: Nombre, apellidos y rol del destinatario.
- **Contenido**: Contenido del mensaje (PDF o URL).
- **Acciones**: Botones para realizar acciones sobre cada mensaje:
    - Vuelve en enviar el mensaje (solo aparecerá cuando el mensaje ya haya sido enviado).
    - Dar de baja el mensaje de la lista (solo aparecerá cuando el mensaje tenga estado activo)
    - Reactivar el mensaje dado de baja de la lista (solo aparecerá cuando el mensaje tenga estado inactivo).

#### Ejemplo de Uso

- Para buscar un mensaje especñifico, puedes ingresar su canal, fecha de envío o estado en los campos correspondientes en la parte superior y la lista se filtrará automáticamente para mostrar los resultados relevantes.
- Para da de baja un mensaje, haz clic en el ícono de eliminación (papelera) y confirma la acción.