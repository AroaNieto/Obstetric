package es.obstetrics.obstetric.backend.utilities;

import es.obstetrics.obstetric.backend.entity.PregnanceEntity;

import java.time.LocalDate;
import java.util.List;

public class Utilities {

    /**
     * Calculo de la semana de embarazo a partir de la fecha del último día de regla del paciente.
     * @param dayLastPeriod Fecha en días del último día de regla de l apaciente.
     * @return La semana de embarazo en la que se encuentra la paciente.
     */
    public static int quarterCalculator(long dayLastPeriod){
        long days = LocalDate.now().toEpochDay() - dayLastPeriod; //Días transcurridos
        return Integer.parseInt(String.valueOf(days/7));
    }

    /**
     * Comprobación de si el paciente tiene embarazos activos si la fecha de finalización es nula.
     * @param pregnancies Lista de embarazos del usuario
     * @return Devuelve verdadero si el embarazo está activo y falso si no.
     */
    public static PregnanceEntity isPregnantActive(List<PregnanceEntity> pregnancies){
        if (pregnancies != null) { //Se comprueba si el usuario no tiene embarazos activos
            for (PregnanceEntity p : pregnancies) {
                if (p.getEndingDate() == null) { //El embarazo estaría activo
                    return p;
                }
            }

        }
        return null;
    }

}
