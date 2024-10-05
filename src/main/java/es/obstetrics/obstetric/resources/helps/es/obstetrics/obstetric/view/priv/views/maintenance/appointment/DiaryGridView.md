Esta interfaz está diseñada para gestionar el mantenimiento de las agendas. A continuación se detallan las diferentes funcionalidades y secciones de la interfaz.

#### Barra de Búsqueda y Filtros

En la parte superior de la interfaz, se encuentran varios campos de búsqueda y filtros:

- **Buscar**: Campo de texto para buscar sanitarios por su nombre o apellidos.
- **Centro**: Campo para buscar agendas por su centro.
- **Fecha de inicio**: Campo para buscar agendas por su fecha de inicio.
- **Día que pasa consulta**: Campo para buscar agendas por uno de los días en los que pasa consulta.

#### Tabla de Agendas

La tabla principal muestra una lista de agendas con la siguiente información:

- **Sanitario**: Nombre completo del sanitario junto con su DNI.
- **Fecha de inicio**: Fecha de inicio de la agenda.
- **Fecha de fin**: Fecha de finalización de la agenda.
- **Centro**: Centro asociado a la agenda.
- **Lunes, martes, miércoles, jueves, viernes, sábado, domingo**: Días que pasa consulta el sanitario en esa agenda.
- **Estado**: Estado de la agenda.
- **Acciones**: Botones para realizar acciones sobre cada agenda:
    - Añadir un horario para esa agenda.
    - Editar la agenda.
    - Dar de baja la agenda de la lista (solo aparecerá cuando la agenda esté en estado activa y su fecha de finalización aún no ha pasado).
    - Reactivar la agenda dada de baja de la lista (solo aparecerá cuando la agenda esté en estado inactivo).
- **Cuando un sanitario tiene una agenda activa para un centro, no se podrán añadir agendas solapando dicha fecha.**

#### Tabla de Horarios

Tabla que se muestra al pulsar sobre una de las filas de agenda, muestra una lista de horarios para esa agenda con la siguiente información:

- **Fecha de inicio**: Fecha de inicio del horario.
- **Fecha de fin**: Fecha de finalización del horario.
- **Hora de inicio**: Hora de inicio del horario.
- **Máximo de pacientes**: Máximo de pacientes para ese horario.
- **Acciones**: Botones para realizar acciones sobre cada horario:
    - Editar el horario.
    - Dar de baja el horario de la lista (solo aparecerá cuando el horario esté en estado activo y su fecha de finalización aún no ha pasado).
    - Reactivar el horario dado de baja de la lista (solo aparecerá cuando el horario esté en estado inactivo).
- **Cuando uno o varios horarios dentro de una agenda, se podrán solapar las fechas de inicio y de fin siempre y cuando NO haya solapamiento entre las horas.**

#### Ejemplo de Uso

- Añado un horario con fecha de inicio el día 17-05-2024 y sin fecha de fin. No podré añadir más horarios hasta que se modifique este y se ponga fecha de fin y finalice o se dé de baja.
- Añado un horario con fecha de inicio el día 17-05-2024 y con fecha de fin el día 17-03-2024; podré añadir más horarios a partir de la fecha de fin.
- Añado una agenda con fecha de inicio el día 17-05-2024, sin fecha de fin, con horas 9:00-14:00:
    - Podré añadir un horario con fecha de inicio 19-05-2024, sin fecha de fin y con horas de 16:00-17:00.
    - NO podré añadir un horario con fecha de inicio 19-05-2024, sin fecha de fin y con horas de 13:00-17:00 porque existe solapamiento entre las horas.
- Añado un horario con fecha de inicio el día 17-05-2024 y con fecha de fin el día 17-03-2024; podré añadir más horarios a partir de la fecha de fin.
