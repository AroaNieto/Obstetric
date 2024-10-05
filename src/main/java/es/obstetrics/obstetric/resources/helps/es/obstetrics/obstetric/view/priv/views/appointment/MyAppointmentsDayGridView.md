Esta interfaz est� dise�ada para gestionar el mantenimiento de sus citas, horarios y agendas. A continuaci�n se detallan las diferentes funcionalidades y secciones de la interfaz.

#### Barra de B�squeda y Filtros

En la parte superior de la interfaz, se encuentran varios campos de b�squeda y filtros:

- **Buscar**: Campo de texto para buscar al paciente asociado a una cita por su nombre o apellidos.
- **D�a**: D�a por el que se quiere buscar las citas.
- **Centro**: Campo para buscar citas por el centro asociado.
- **D�a que pasa consulta**: Campo para buscar citas por la agenda asociada.
- Por defecto, aparecen las citas del d�a actual. Si se quisiera ver las citas de d�as posteriores o anteriores, se tendr�a que volver a seleccionar el campo de d�a y se actualizar�a la tabla con los datos del d�a.
- Si desea ver su agenda en un calendario con sus huecos libres y citas, debe realizar los siguientes pasos:
  - Una vez seleccionado, se habilitar� el campo de centro con los centros asociados a las citas de ese d�a y se actualizar� la tabla con los datos del d�a y centro asociado.
  - Una vez seleccionado, se habilitar� el campo de agenda para buscar por un tipo de agenda espec�fico y se actualizar� la tabla con los datos del d�a, agenda y centro asociado.
  - Una vez seleccionado, aparecer� un bot�n oculto para poder consultar la agenda en ese centro a partir del d�a.

#### Tabla de Mi Agenda

La tabla principal muestra una lista de las citas asociadas a una agenda con la siguiente informaci�n:

- **Paciente**: Nombre completo del paciente junto con su DNI.
- **Semana de embarazo**: Semana de embarazo del paciente (si aplica).
- **Fecha**: Fecha de la cita.
- **Hora**: Hora de la cita.
- **Tipo de cita**: Tipo de cita.
- **Nombre de la agenda**: Nombre de la agenda asociada.
- **Atendido**: Estado de la cita (atendida o no).
- **Aseguradora**: Aseguradora asociada a la cita.
- **P�liza de seguro**: P�liza de seguro asociada.
- **Centro**: Centro asociado a la cita.
- **Observaciones**: Observaciones adicionales.
- **Acciones**: Botones para realizar acciones sobre cada cita:
  - Comenzar el proceso de citado. **SOLO APARECER� VISIBLE SI SE TRATA DEL D�A ACTUAL**
  - Imprimir (�cono de la impresora): Imprimir la agenda que se est� mostrando actualmente en el grid.

#### Ejemplo de Uso

- Busco mis citas para el d�a 25/06/2024:
  - Selecciono el d�a en el campo de d�a.
  - Se me habilita el campo de centro; selecciono el centro: hospital Reina Sof�a.
  - Se me habilita el campo de agenda; selecciono la agenda: agenda de verano.
  - Se me muestran en el grid todas las citas para el d�a 25/06/2024, el hospital Reina Sof�a y la agenda de verano. Adem�s, se habilita el bot�n de consultar agenda con dichos datos.
