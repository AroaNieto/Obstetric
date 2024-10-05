
Esta interfaz está diseñada para gestionar la búsqueda de citas. A continuaci?n se detallan las diferentes funcionalidades y secciones de la interfaz.

#### Barra de Búsqueda y filtros

En la parte superior de la interfaz, se encuentran varios campos de b?squeda y filtros:

- **Buscar**: Campo de texto para buscar pacientes de una cita por su nombre o apellidos
- **D?a**: Campo para buscar citas por el d?a de esta.
- **Tipo de cita**: Campo para buscar citas por su tipo de cita.
- **Imprimir**: Se abre un cuadro de diálogo para poder imprimir un listado con todas las citas existentes.
- **Citas por aseguradora**: Se abre un cuadro de diálogo para escoger un intervalo de fechas y mostrar todas las citas existentes con aseguradoras.

#### Tabla de citas

La tabla principal muestra una lista de citas con la siguiente informaci?n:

- **Sanitario**: Nombre completo del sanitario junto con su DNI asociado a la cita.
- **Paciente**: Nombre y apellidos del paciente asociado a la cita.
- **D?a**: D?a de la cita
- **Hora de inicio**: Hora de inicio de la cita.
- **Hora de inicio**: Hora de fin de la cita.
- **Notificar**: Indica si el paciente ha pedido que se le notifique la cita por correo electr?nico.
- **Recordar**: Indica si el paciente ha pedido que se le recuerde la cita 2 d?as antes de su comienzo.
- **Atendido**: Si el paciente se ha presentado a la cita.
- **Aseguradora**: Aseguradora asociada a la cita.
- **Observaciones**: Observaciones de la cita.
- **Tipo de cita**: Tipo de cita asociado a la cita
- **Acciones**: Botones para realizar acciones sobre cada cita:
    -  Dar de baja la cita de la lista (solo aparecer? cuando la cita est? en estado activa y su fecha a?n no ha pasado.).
    -  Reactivar la cita dado de baja de la lista (solo aparecer? cuando la cita est? en estado inactivo y su fecha  a?n no ha pasado.).

#### Ejemplo de Uso

- Para buscar una cita espec?fica, puedes ingresar su paciente, d?a o tipo de cita en los campos correspondientes en la parte superior y la lista se filtrar? autom?ticamente para mostrar los resultados relevantes.
- Para da de baja una cita, haz clic en el ?cono de eliminaci?n (papelera) y confirma la acci?n.