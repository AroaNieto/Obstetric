Esta interfaz está diseñada para gestionar el mantenimiento de sus citas, horarios y agendas. A continuación se detallan las diferentes funcionalidades y secciones de la interfaz.

#### Barra de Búsqueda y Filtros

En la parte superior de la interfaz, se encuentran varios campos de búsqueda y filtros:

- **Buscar**: Campo de texto para buscar al paciente asociado a una cita por su nombre o apellidos.
- **Día**: Día por el que se quiere buscar las citas.
- **Centro**: Campo para buscar citas por el centro asociado.
- **Día que pasa consulta**: Campo para buscar citas por la agenda asociada.
- Por defecto, aparecen las citas del día actual. Si se quisiera ver las citas de días posteriores o anteriores, se tendría que volver a seleccionar el campo de día y se actualizaría la tabla con los datos del día.
- Si desea ver su agenda en un calendario con sus huecos libres y citas, debe realizar los siguientes pasos:
  - Una vez seleccionado, se habilitará el campo de centro con los centros asociados a las citas de ese día y se actualizará la tabla con los datos del día y centro asociado.
  - Una vez seleccionado, se habilitará el campo de agenda para buscar por un tipo de agenda específico y se actualizará la tabla con los datos del día, agenda y centro asociado.
  - Una vez seleccionado, aparecerá un botón oculto para poder consultar la agenda en ese centro a partir del día.

#### Tabla de Mi Agenda

La tabla principal muestra una lista de las citas asociadas a una agenda con la siguiente información:

- **Paciente**: Nombre completo del paciente junto con su DNI.
- **Semana de embarazo**: Semana de embarazo del paciente (si aplica).
- **Fecha**: Fecha de la cita.
- **Hora**: Hora de la cita.
- **Tipo de cita**: Tipo de cita.
- **Nombre de la agenda**: Nombre de la agenda asociada.
- **Atendido**: Estado de la cita (atendida o no).
- **Aseguradora**: Aseguradora asociada a la cita.
- **Póliza de seguro**: Póliza de seguro asociada.
- **Centro**: Centro asociado a la cita.
- **Observaciones**: Observaciones adicionales.
- **Acciones**: Botones para realizar acciones sobre cada cita:
  - Comenzar el proceso de citado. **SOLO APARECERÁ VISIBLE SI SE TRATA DEL DÍA ACTUAL**
  - Imprimir (ícono de la impresora): Imprimir la agenda que se está mostrando actualmente en el grid.

#### Ejemplo de Uso

- Busco mis citas para el día 25/06/2024:
  - Selecciono el día en el campo de día.
  - Se me habilita el campo de centro; selecciono el centro: hospital Reina Sofía.
  - Se me habilita el campo de agenda; selecciono la agenda: agenda de verano.
  - Se me muestran en el grid todas las citas para el día 25/06/2024, el hospital Reina Sofía y la agenda de verano. Además, se habilita el botón de consultar agenda con dichos datos.
