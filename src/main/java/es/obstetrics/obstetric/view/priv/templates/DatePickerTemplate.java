package es.obstetrics.obstetric.view.priv.templates;

import com.vaadin.flow.component.datepicker.DatePicker;

import java.util.Arrays;

public class DatePickerTemplate extends DatePicker{

    public DatePickerTemplate(String label){

        // Configuración de DatePickerI18n para personalizar el componente
        DatePickerI18n datePickerI18n = new DatePickerI18n();
        datePickerI18n.setWeekdays(Arrays.asList("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"));
        datePickerI18n.setWeekdaysShort(Arrays.asList("dom", "lun", "mar", "mié", "jue", "vie", "sáb"));
        datePickerI18n.setMonthNames(Arrays.asList("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"));
        datePickerI18n.setToday("Hoy");
        datePickerI18n.setCancel("Cancelar");
        setLabel(label);
        setI18n(datePickerI18n);

    }

}
