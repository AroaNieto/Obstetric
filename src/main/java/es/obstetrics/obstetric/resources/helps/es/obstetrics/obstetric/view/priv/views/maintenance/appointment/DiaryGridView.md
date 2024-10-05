Esta interfaz est� dise�ada para gestionar el mantenimiento de las agendas. A continuaci�n se detallan las diferentes funcionalidades y secciones de la interfaz.

#### Barra de B�squeda y Filtros

En la parte superior de la interfaz, se encuentran varios campos de b�squeda y filtros:

- **Buscar**: Campo de texto para buscar sanitarios por su nombre o apellidos.
- **Centro**: Campo para buscar agendas por su centro.
- **Fecha de inicio**: Campo para buscar agendas por su fecha de inicio.
- **D�a que pasa consulta**: Campo para buscar agendas por uno de los d�as en los que pasa consulta.

#### Tabla de Agendas

La tabla principal muestra una lista de agendas con la siguiente informaci�n:

- **Sanitario**: Nombre completo del sanitario junto con su DNI.
- **Fecha de inicio**: Fecha de inicio de la agenda.
- **Fecha de fin**: Fecha de finalizaci�n de la agenda.
- **Centro**: Centro asociado a la agenda.
- **Lunes, martes, mi�rcoles, jueves, viernes, s�bado, domingo**: D�as que pasa consulta el sanitario en esa agenda.
- **Estado**: Estado de la agenda.
- **Acciones**: Botones para realizar acciones sobre cada agenda:
    - A�adir un horario para esa agenda.
    - Editar la agenda.
    - Dar de baja la agenda de la lista (solo aparecer� cuando la agenda est� en estado activa y su fecha de finalizaci�n a�n no ha pasado).
    - Reactivar la agenda dada de baja de la lista (solo aparecer� cuando la agenda est� en estado inactivo).
- **Cuando un sanitario tiene una agenda activa para un centro, no se podr�n a�adir agendas solapando dicha fecha.**

#### Tabla de Horarios

Tabla que se muestra al pulsar sobre una de las filas de agenda, muestra una lista de horarios para esa agenda con la siguiente informaci�n:

- **Fecha de inicio**: Fecha de inicio del horario.
- **Fecha de fin**: Fecha de finalizaci�n del horario.
- **Hora de inicio**: Hora de inicio del horario.
- **M�ximo de pacientes**: M�ximo de pacientes para ese horario.
- **Acciones**: Botones para realizar acciones sobre cada horario:
    - Editar el horario.
    - Dar de baja el horario de la lista (solo aparecer� cuando el horario est� en estado activo y su fecha de finalizaci�n a�n no ha pasado).
    - Reactivar el horario dado de baja de la lista (solo aparecer� cuando el horario est� en estado inactivo).
- **Cuando uno o varios horarios dentro de una agenda, se podr�n solapar las fechas de inicio y de fin siempre y cuando NO haya solapamiento entre las horas.**

#### Ejemplo de Uso

- A�ado un horario con fecha de inicio el d�a 17-05-2024 y sin fecha de fin. No podr� a�adir m�s horarios hasta que se modifique este y se ponga fecha de fin y finalice o se d� de baja.
- A�ado un horario con fecha de inicio el d�a 17-05-2024 y con fecha de fin el d�a 17-03-2024; podr� a�adir m�s horarios a partir de la fecha de fin.
- A�ado una agenda con fecha de inicio el d�a 17-05-2024, sin fecha de fin, con horas 9:00-14:00:
    - Podr� a�adir un horario con fecha de inicio 19-05-2024, sin fecha de fin y con horas de 16:00-17:00.
    - NO podr� a�adir un horario con fecha de inicio 19-05-2024, sin fecha de fin y con horas de 13:00-17:00 porque existe solapamiento entre las horas.
- A�ado un horario con fecha de inicio el d�a 17-05-2024 y con fecha de fin el d�a 17-03-2024; podr� a�adir m�s horarios a partir de la fecha de fin.
