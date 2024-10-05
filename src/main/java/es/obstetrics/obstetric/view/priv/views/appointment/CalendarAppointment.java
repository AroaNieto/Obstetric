package es.obstetrics.obstetric.view.priv.views.appointment;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.AppointmentService;
import es.obstetrics.obstetric.backend.service.InsuranceService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.view.priv.dialog.appointment.AppointmentDialog;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarAppointment extends VerticalLayout {
    private final DiaryEntity diary;
    private YearMonth currentMonth;
    private LocalDate dateTemp;
    private final Span monthLabel;
    private Div calendarContainer;
    private final PatientEntity patientEntity;
    private final InsuranceService insuranceService;
    private final List<AppointmentTypeEntity> appointmentTypeEntities;
    private final AppointmentService appointmentService;

    public CalendarAppointment(LocalDate localDate, DiaryEntity diary,
                               PatientEntity patientEntity,
                               AppointmentService appointmentService,
                               InsuranceService insuranceService,
                               List<AppointmentTypeEntity> appointmentTypeEntities) {

        this.appointmentService = appointmentService;
        this.appointmentTypeEntities = appointmentTypeEntities;
        this.diary = diary;
        this.insuranceService = insuranceService;
        this.patientEntity = patientEntity;
        dateTemp = localDate;
        monthLabel = new Span();
        currentMonth = YearMonth.from(dateTemp);
        calendarContainer = new Div(updateCalendar());
        calendarContainer.addClassName("calendar-container");
        add(createHeader(), calendarContainer);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        setSizeFull();
    }

    private Div updateCalendar() {
        Div calendarDiv = new Div();
        calendarDiv.setWidthFull();
        calendarDiv.add(createDayHeaders());

        LocalDate firstDayOfMonth = currentMonth.atDay(1); //Se obtiene el primer día del mes
        LocalDate firstDisplayedDate = firstDayOfMonth.minusDays(firstDayOfMonth.getDayOfWeek().getValue() - 1); //Cuantos días hay que retroceder desde el primer día del mes para llegar al lunes

        /*
         * Se itera sobre el calendario, constará de 6 filas para representar las semanas
         *  y 8 columnas para representar los días de la semana.
         */
        for (int week = 0; week < 6; week++) {
            HorizontalLayout weekHl = new HorizontalLayout();
            weekHl.setSpacing(false);
            weekHl.addClassName("week");
            for (int day = 0; day < 7; day++) {
                LocalDate localDate = firstDisplayedDate.plusDays(week * 7 + day);
                Div dayCell = createDayCell(localDate);
                weekHl.add(dayCell);
            }
            calendarDiv.add(weekHl);
        }

        return calendarDiv;
    }

    private void updateCalendarView() {
        updateMonth();
        remove(calendarContainer);
        calendarContainer = new Div(updateCalendar());
        calendarContainer.addClassName("calendar-container");
        add(calendarContainer);
    }

    private void updateMonth() {
        currentMonth = YearMonth.from(dateTemp);
        monthLabel.setText(dateTemp.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + currentMonth.getYear());
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("calendar-header");
        Button angleLeftIcon = createIcon(VaadinIcon.ANGLE_LEFT.create());
        angleLeftIcon.addClickListener(event -> {
            dateTemp = dateTemp.minusMonths(1);
            updateCalendarView();
        });
        Button angleRightIcon = createIcon(VaadinIcon.ANGLE_RIGHT.create());
        angleRightIcon.addClickListener(event -> {
            dateTemp = dateTemp.plusMonths(1);
            updateCalendarView();
        });
        header.add(angleLeftIcon);
        monthLabel.addClassName("month-label");
        updateMonth();
        header.add(monthLabel);
        header.setJustifyContentMode(JustifyContentMode.CENTER);
        header.setWidthFull();
        header.add(angleRightIcon);

        return header;
    }

    private Button createIcon(Icon icon) {
        Button button = new Button(icon);
        button.addClassName("dark-green-color-button");
        return button;
    }

    private HorizontalLayout createDayHeaders() {
        HorizontalLayout dayHeader = new HorizontalLayout();
        dayHeader.setSpacing(false);
        dayHeader.setWidthFull();
        dayHeader.addClassName("day-header");
        ArrayList<String> days = new ArrayList<>();
        days.add(ConstantUtilities.MONDAY);
        days.add(ConstantUtilities.TUESDAY);
        days.add(ConstantUtilities.WEDNESDAY);
        days.add(ConstantUtilities.THURSDAY);
        days.add(ConstantUtilities.FRIDAY);
        days.add(ConstantUtilities.SATURDAY);
        days.add(ConstantUtilities.SUNDAY);

        for (String day : days) {
            Span dayLabel = new Span(day);
            dayLabel.addClassName("day-label");
            dayHeader.add(dayLabel);
        }
        return dayHeader;
    }

    private Div createDayCell(LocalDate date) {
        Div dayCell = new Div();
        dayCell.addClassName("day-cell");
        dayCell.setText(String.valueOf(date.getDayOfMonth()));

        String[] colors = {"var(--yellow-color-60pct)","var(--pink-color-50pct)","var(--light-purple-color-30pct)","var(--lumo-primary-color-20pct)", "var(--dark-green-color-20pct)"};

        int colorIndex = 0;
        if(date.isAfter(LocalDate.now())){ //El mismo día no se podrá pedir cita
            //Se comprueba que el horario este vigente y que pasa cita ese día
            if ((date.isAfter(diary.getStartTime()) && (diary.getEndTime() == null || diary.getEndTime().isAfter(date)))
                    && datesCheck(date.getDayOfWeek())) {
                // Añadir información de ScheduleEntity si la fecha coincide
                for (ScheduleEntity schedule : diary.getSchedules()) {
                    if(schedule.getState().equals(ConstantUtilities.STATE_ACTIVE)){
                        if ((schedule.getStartDate().isEqual(date) || schedule.getStartDate().isBefore(date))&&
                                (schedule.getEndingDate() == null || schedule.getEndingDate().isAfter(date))) {
                            Div scheduleInfo = new Div();
                            int busyDating = 0;
                            for(AppointmentEntity oneAppointment: schedule.getAppointmentEntities()){
                                if(oneAppointment.getDate().equals(date) && oneAppointment.getState().equals(ConstantUtilities.STATE_ACTIVE)){
                                    busyDating++;
                                }
                            }
                            int freeSpaces = (int) (Long.parseLong(schedule.getMaxPatients()) - busyDating); //Espacios disponibles
                            scheduleInfo.add(new Span(schedule.getStartTime() + "-" + schedule.getEndTime() + " " + freeSpaces + " libres"));
                            scheduleInfo.addClassName("schedule-info");
                            //Cambia el color dinámicamente
                            String color = colors[colorIndex % colors.length];
                            colorIndex++;
                            scheduleInfo.getStyle().set("background-color", color);
                            if(patientEntity == null){
                                scheduleInfo.addClickListener(event ->openScheduleDialog(schedule, date));
                            }else{
                                scheduleInfo.addClickListener(event ->openScheduleDialog(schedule, date));
                            }
                            dayCell.add(scheduleInfo);
                        }
                    }
                }
            }
            if (date.getMonth().equals(currentMonth.getMonth())){ //Se comprueba si el día pertenece a este mes o no
                dayCell.addClassName("current-month");
            }else{
                dayCell.addClassName("other-month");
            }

        }

        return dayCell;
    }

    private boolean datesCheck(DayOfWeek dayOfWeek) {
        return (diary.isMonday() && dayOfWeek == DayOfWeek.MONDAY) ||
                (diary.isThursday() && dayOfWeek == DayOfWeek.THURSDAY) ||
                (diary.isWednesday() && dayOfWeek == DayOfWeek.WEDNESDAY) ||
                (diary.isTuesday() && dayOfWeek == DayOfWeek.TUESDAY) ||
                (diary.isFriday() && dayOfWeek == DayOfWeek.FRIDAY) ||
                (diary.isSunday() && dayOfWeek == DayOfWeek.SUNDAY) ||
                (diary.isSaturday() && dayOfWeek == DayOfWeek.SATURDAY);
    }

    private void openScheduleDialog(ScheduleEntity schedule, LocalDate date) {
        if(patientEntity == null){
            AppointmentDialog appointmentConfirmDialog = new AppointmentDialog(schedule, insuranceService,
                    null, date, appointmentTypeEntities,appointmentService);
            appointmentConfirmDialog.addListener(AppointmentDialog.ConfirmEvent.class, this::saveAppointment);
            appointmentConfirmDialog.open();
            appointmentConfirmDialog.setHeaderTitle("Ver citas día "+ date.getDayOfMonth() + "/" + date.getMonthValue() + "/" + date.getYear());
        }else{
            AppointmentDialog appointmentConfirmDialog = new AppointmentDialog(schedule, insuranceService,
                    patientEntity, date, appointmentTypeEntities,appointmentService);
            appointmentConfirmDialog.addListener(AppointmentDialog.ConfirmEvent.class, this::saveAppointment);
            appointmentConfirmDialog.open();
            appointmentConfirmDialog.setHeaderTitle("PEDIR CITA");
        }

    }

    private void saveAppointment(AppointmentDialog.ConfirmEvent confirmEvent) {
        fireEvent(new ConfirmEvent(this, confirmEvent.getAppointment()));
    }

    /**
     * Clase abstracta que extiende de {@link CalendarAppointment}, evento ocurrido en dicha clase.
     */
    @Getter
    public static abstract class AppointmentCalendarDialogFormEvent extends ComponentEvent<CalendarAppointment> {
        private final AppointmentEntity appointmentEntity; //Comunidad con la que trabajamos

        protected AppointmentCalendarDialogFormEvent(CalendarAppointment source, AppointmentEntity appointmentEntity) {
            super(source, false);
            this.appointmentEntity = appointmentEntity;
        }
    }

    /**
     * Clase heredada de Calendar, representa un evento de cerrar que ocurre en el diálogo.
     */
    public static class ConfirmEvent extends AppointmentCalendarDialogFormEvent {
        ConfirmEvent(CalendarAppointment source, AppointmentEntity appointmentEntity) {
            super(source, appointmentEntity);
        }
    }

    /**
     * Método que permite registrar un listener para un tipo específico de evento.
     *
     * @param eventType Tipo de evento al que se desea registrar un listener.
     * @param listener  El listener que manejará el evento.
     * @return Un objeto Registration que permite anular el registro del listener cuando sea necesario.
     */
    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType, ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}