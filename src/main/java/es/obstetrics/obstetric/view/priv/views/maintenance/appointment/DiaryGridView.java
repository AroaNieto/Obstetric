package es.obstetrics.obstetric.view.priv.views.maintenance.appointment;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.DiaryEntity;
import es.obstetrics.obstetric.backend.entity.SanitaryEntity;
import es.obstetrics.obstetric.backend.entity.ScheduleEntity;
import es.obstetrics.obstetric.backend.service.*;
import es.obstetrics.obstetric.backend.utilities.BaseDirectoryPath;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.ConstantValues;
import es.obstetrics.obstetric.listings.pdf.DiaryGridPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.confirmDialog.appointment.FailReactivateDiaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.diary.DeleteDiaryConfirmDialog;
import es.obstetrics.obstetric.view.priv.confirmDialog.maintenance.diary.DeleteScheduleConfirmDialog;
import es.obstetrics.obstetric.view.priv.dialog.WindowHelp;
import es.obstetrics.obstetric.view.priv.dialog.MasterListingsDialog;
import es.obstetrics.obstetric.view.priv.dialog.maintenance.diary.DiaryDialog;
import es.obstetrics.obstetric.view.priv.dialog.maintenance.diary.ScheduleDialog;
import es.obstetrics.obstetric.view.priv.grid.MasterGrid;
import es.obstetrics.obstetric.view.priv.templates.DatePickerTemplate;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Route(value = "secretary/diaries", layout = PrincipalView.class)
@PageTitle("MotherBloom-Appointment")
@PermitAll
public class DiaryGridView extends MasterGrid<DiaryEntity> {
    final DiaryService diaryService;
    private final ConstantValues constantValues;
    private final SanitaryService sanitaryService;
    private final CenterService centerService;
    private Grid<ScheduleEntity> scheduleGrid;
    private final ScheduleService scheduleService;
    private Button deleteBtn;
    private Button reactivateBtn;
    private Button reactivateScheduleBtn;
    private Button deleteScheduleBtn;

    @Autowired
    public DiaryGridView(DiaryService diaryService,
                         SanitaryService sanitaryService,
                         CenterService centerService,
                         ConstantValues constantValues,
                         ScheduleService scheduleService) {
        this.diaryService = diaryService;
        this.scheduleService = scheduleService;
        this.constantValues = constantValues;
        this.sanitaryService = sanitaryService;
        this.centerService = centerService;

        setHeader(new H2("AGENDAS"));
        setFilterContainer();
        setGrid();
        setScheduleGrid();
        updateGrid();
        addBtn.setTooltipText("Añadir agenda");
    }

    /**
     * Actualización del grid de contenidos.
     */
    @Override
    public void updateGrid() {
        masterGrid.setDataProvider(createDataProvider());
    }

    /**
     * Configura un DataProvider para cargar datos de agendas de manera diferida y eficiente,
     * optimizando el rendimiento de la aplicación al minimizar la carga anticipada de datos.
     */
    private DataProvider<DiaryEntity, String> createDataProvider() {
        return DataProvider.fromFilteringCallbacks(
                query -> {
                    int offset = query.getOffset(); //Indice de inicio
                    int limit = query.getLimit(); //Cantidad de elementos a recuprar
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return diaryService.findAll(
                                offset / limit, limit).get().toList().stream(); //Devuelve la lista de horarios segun el offset y el límite y la convierte a stream.
                    } else {
                        return diaryService.findBySanitaryEntityNameContaining(filter, offset / limit, limit).get().toList().stream(); //Devuelve una página de horarios que coinciden con el filtro y la convierte a stream.
                    }
                },
                query -> { //Obtiene el tamaño total de los datos después de aplicar el filtro.
                    String filter = query.getFilter().orElse("");
                    if (filter.isEmpty()) {
                        return (int) diaryService.findAll(0, Integer.MAX_VALUE).getTotalElements(); //Devuelve el número total de horarios en el sistema.
                    } else {
                        return (int) diaryService.findBySanitaryEntityNameContaining(filter, 0, Integer.MAX_VALUE).getTotalElements(); //Devuelve el número total de horarios que coinciden con el filtro.
                    }
                }
        );
    }

    /**
     * Abre el cuadro de diálogo dónde se editará el contenido, se le
     * pasa todas las categorias y sucategorias.
     */
    @Override
    public void openDialog() {
        List<SanitaryEntity> sanitaries = sanitaryService.findByRole(ConstantUtilities.ROLE_GYNECOLOGIST);
        sanitaries.addAll(sanitaryService.findByRole(ConstantUtilities.ROLE_MATRONE));
        DiaryDialog diaryDialog = new DiaryDialog(null, diaryService, sanitaries,
                centerService.findAll(0, 900).getContent(), centerService, sanitaryService);
        diaryDialog.setHeaderTitle("AÑADIR AGENDA");
        diaryDialog.addListener(DiaryDialog.SaveEvent.class, this::saveDiary);
        diaryDialog.open();
    }

    private void saveDiary(DiaryDialog.SaveEvent saveEvent) {
        diaryService.save(saveEvent.getDiaryEntity());
        updateGrid();
    }

    private void saveSchedule(ScheduleDialog.SaveEvent saveEvent) {
        scheduleService.save(saveEvent.getScheduleEntity());
        updateScheduledGrid(saveEvent.getScheduleEntity().getDiaryEntity());
        updateGrid();
    }

    /**
     * Crea los filtros que se utilizarán para hacer las búsquedas en el grid
     */
    @Override
    public void setFilterContainer() {

        searchTextField.setPlaceholder("Buscar sanitario");
        searchTextField.setValueChangeMode(ValueChangeMode.LAZY); // Cambiado a ValueChangeMode.LAZY
        searchTextField.setTitle("Sanitario");
        searchTextField.setPrefixComponent(new Icon(VaadinIcon.DOCTOR));
        searchTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query ->
                    diaryService.findBySanitaryEntityNameContaining(filter, query.getOffset() / query.getLimit(), query.getLimit()).stream());
        });

        masterGrid.setDataProvider(createDataProvider());
        Button helpButton = createButton(VaadinIcon.QUESTION_CIRCLE.create(), "help-button");
        helpButton.setTooltipText("Ayuda");
        helpButton.addClickListener(event -> {
            WindowHelp windowHelp = new WindowHelp(getClass().getSimpleName(),
                    ConstantUtilities.ROUTE_HELP + "/" + ConstantUtilities.ROUTE_HELP_MAINTENANCE
                            + ConstantUtilities.ROUTE_HELP_APPOINTMENT,
                    "Guía gestión de agendas");
            windowHelp.open();
        });

        addBtn.setTooltipText("Añadir agenda");
        Button printButton = createButton(new Icon(VaadinIcon.PRINT),"help-button");
        printButton.addClickListener(event-> printButton());
        printButton.setTooltipText("Imprimir listado");
        filterContainerHl.add(searchTextField,
                createCenterTextField(),
                createStartDate(),
                createDaysCombobox(),
                addBtn, printButton,helpButton);

        filterContainerHl.setFlexGrow(1, searchTextField, createCenterTextField());
        filterContainerHl.setDefaultVerticalComponentAlignment(Alignment.END);
        filterContainerHl.setWidthFull();
    }

    /**
     * Método ejecutado cuando el usuario pulsa sobre el botón de imprimir.
     * Abre el cuadro de diálogo con el listado en PDF para que el usuario pueda imprimirlo.
     * Las agendas es pasan mediante carga diferencia, solo cuando el usuario solicita
     *  la visualización del listado.
     */
    private void printButton() {
        StreamResource resource = new StreamResource("agendas.pdf", () -> {
            List<DiaryEntity> diaryEntities = getDiaryData();
            return new DiaryGridPdf((ArrayList<DiaryEntity>) diaryEntities).generatePdf();
        });

        PdfViewer pdfViewer = new PdfViewer();
        pdfViewer.setSrc(resource);

        MasterListingsDialog dialog = new MasterListingsDialog(pdfViewer);
        dialog.setHeaderTitle("Listado de agendas");
        dialog.open();
    }
    /**
     * Obtiene los datos de las agendas a través del DataProvider (carga diferida)
     * @return La lista de las agendas.
     */
    private List<DiaryEntity> getDiaryData() {
        DataProvider<DiaryEntity, String> dataProvider = createDataProvider();
        Query<DiaryEntity, String> query = new Query<>();
        return dataProvider.fetch(query).collect(Collectors.toList());
    }


    private Component createCenterTextField() {
        TextField centerTextField = new TextField("Centro");
        centerTextField.addClassName("text-field-1300");
        centerTextField.setTooltipText("Escriba el centroo que desea buscar.");
        centerTextField.setValueChangeMode(ValueChangeMode.EAGER); //El evento se dispara inmediatamente después de cada cambio de texto
        centerTextField.setPrefixComponent(new Icon(VaadinIcon.HOSPITAL));
        centerTextField.addValueChangeListener(event -> {
            String filter = event.getValue();
            masterGrid.setItems(query -> diaryService.findByCenterEntityContaining(filter, query.getOffset(), query.getLimit()).stream());
        });
        return centerTextField;
    }


    private Button createButton(Icon icon, String className) {
        Button button = new Button(icon);
        button.addClassName(className);
        return button;
    }

    /**
     * Creación de los dos comboBox: categorías y subcategorias para
     * la aplicación de filtros ne le grid.
     */

    private ComboBox<String> createDaysCombobox() {
        ComboBox<String> days = new ComboBox<>();
        days.setLabel("Día que pasa consulta");
        days.setItems(constantValues.getWeek());
        days.addClassName("text-field-1300");
        days.addValueChangeListener(event -> {
            boolean filter = Boolean.parseBoolean(event.getValue());
            if(event.getValue() == null){

                masterGrid.setItems(query -> diaryService.findAll(
                        query.getOffset() / query.getLimit(), query.getLimit()).get().toList().stream()); //Devuelve la lista de horarios segun el offset y el límite y la convierte a stream.
            }else if (event.getValue().equals("Lunes")) {
                masterGrid.setItems(query -> diaryService.findByMonday(filter, query.getOffset(), query.getLimit()).stream());
            } else if (event.getValue().equals("Martes")) {
                masterGrid.setItems(query -> diaryService.findByTuesday(filter, query.getOffset(), query.getLimit()).stream());
            } else if (event.getValue().equals("Miercoles")) {
                masterGrid.setItems(query -> diaryService.findByWednesday(filter, query.getOffset(), query.getLimit()).stream());
            } else if (event.getValue().equals("Jueves")) {
                masterGrid.setItems(query -> diaryService.findByThursday(filter, query.getOffset(), query.getLimit()).stream());
            } else if (event.getValue().equals("Viernes")) {
                masterGrid.setItems(query -> diaryService.findByFriday(filter, query.getOffset(), query.getLimit()).stream());
            } else if (event.getValue().equals("Sabado")) {
                masterGrid.setItems(query -> diaryService.findBySaturday(filter, query.getOffset(), query.getLimit()).stream());
            } else if (event.getValue().equals("Domingo")) {
                masterGrid.setItems(query -> diaryService.findBySunday(filter, query.getOffset(), query.getLimit()).stream());
            } else {
                masterGrid.setItems(query -> diaryService.findAll(
                        query.getOffset() / query.getLimit(), query.getLimit()).get().toList().stream()); //Devuelve la lista de horarios segun el offset y el límite y la convierte a stream.
            }
        });

        return days;
    }


    /**
     * Creación del DatePicker para filtrar por la fecha de inicio.
     *
     * @return La fecha de inicio
     */
    private DatePicker createStartDate() {
        DatePicker startDate = new DatePickerTemplate("Fecha de inicio");
        startDate.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                masterGrid.setItems(query -> diaryService.findByStartTime(event.getValue(), query.getOffset(), query.getLimit()).stream());
            } else {
                masterGrid.setItems(query -> diaryService.findAll(
                        query.getOffset() / query.getLimit(), query.getLimit()).get().toList().stream()); //Devuelve la lista de horarios segun el offset y el límite y la convierte a stream.
            }

        });
        return startDate;
    }


    /**
     * Creación del grid de contenidos con sus columnas correspondientes.
     */
    @Override
    public void setGrid() {
        masterGrid.addColumn(new ComponentRenderer<>(categoryEntity -> {
            Button angleButton = createButton(new Icon(VaadinIcon.ANGLE_RIGHT), "angle-right-button");
            return new HorizontalLayout(angleButton);

        })).setAutoWidth(true);
        masterGrid.addColumn(createSanitaryRenderer()).setHeader("Sanitario").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(DiaryEntity::getName).setHeader("Nombre").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(diaryEntity -> diaryEntity.getStartTime().getDayOfMonth()+"/"+diaryEntity.getStartTime().getMonthValue()+"/"+diaryEntity.getStartTime().getYear()).setHeader("Fecha de inicio").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(diaryEntity -> {
            if(diaryEntity.getEndTime() != null){
                return diaryEntity.getEndTime().getDayOfMonth()+"/"+diaryEntity.getEndTime().getMonthValue()+"/"+diaryEntity.getEndTime().getYear();
            }
            return null;
        }).setHeader("Fecha de finalización").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(diaryEntity -> diaryEntity.getCenterEntity().getCenterName()
        ).setHeader("Centro asociado").setAutoWidth(true).setSortable(true);

        masterGrid.addColumn(diaryEntity ->
                makeAndAppointment(diaryEntity.isMonday())).setHeader("Lunes").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(diaryEntity ->
                makeAndAppointment(diaryEntity.isTuesday())).setHeader("Martes").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(diaryEntity ->
                makeAndAppointment(diaryEntity.isWednesday())).setHeader("Miercoles").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(diaryEntity ->
                makeAndAppointment(diaryEntity.isThursday())).setHeader("Jueves").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(diaryEntity ->
                makeAndAppointment(diaryEntity.isFriday())).setHeader("Viernes").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(diaryEntity ->
                makeAndAppointment(diaryEntity.isSaturday())).setHeader("Sábado").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(diaryEntity ->
                makeAndAppointment(diaryEntity.isSunday())).setHeader("Domingo").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(DiaryEntity::getState).setHeader("Estado").setAutoWidth(true).setSortable(true);
        masterGrid.addColumn(new ComponentRenderer<>(diaryEntity -> {

            Button addBtn = createButton(new Icon(VaadinIcon.PLUS), "lumo-primary-color-background-button");
            addBtn.addClickListener(event -> openScheduleDialog(diaryEntity));
            addBtn.setTooltipText("Añadir horario");
            Button editBtn = createButton(new Icon(VaadinIcon.EDIT), "dark-green-background-button");
            editBtn.addClickListener(event -> openEditDiaryDialog(diaryEntity));
            editBtn.setTooltipText("Editar agenda");
            reactivateBtn = createButton(new Icon(VaadinIcon.REFRESH), "yellow-color-button");
            reactivateBtn.addClickListener(event -> openReactivateDiary(diaryEntity));
            reactivateBtn.setTooltipText("Reactivar agenda");
            if (diaryEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-disable-background-button");
                deleteBtn.setVisible(false);
                deleteBtn.getElement().setAttribute("disabled", true);
                return new HorizontalLayout(addBtn, editBtn, deleteBtn, reactivateBtn);
            }
            reactivateBtn.setVisible(false);
            deleteBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-background-button");
            deleteBtn.addClickListener(event -> openUnsubscribeDiary(diaryEntity));
            deleteBtn.setTooltipText("Dar de baja agenda");
            return new HorizontalLayout(addBtn, editBtn, deleteBtn, reactivateBtn);
        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);

        masterGrid.setItemDetailsRenderer(createScheduleGrid());
         /*
            Evento encargado de ocultar el panel de detalles en el caso que no haya
                detalles que mostrar.
         */
        masterGrid.addItemClickListener(event -> {
            if (event.getItem() != null) {
                if (event.getItem().getSchedules().isEmpty()) {
                    masterGrid.setDetailsVisible(event.getItem(), false);
                }
            }
        });
    }

    private static Renderer<DiaryEntity> createSanitaryRenderer() {
        return LitRenderer.<DiaryEntity> of(
                        "<vaadin-horizontal-layout style=\"align-items: center;\" theme=\"spacing\">"
                                + "  <vaadin-avatar img=\"${item.pictureUrl}\" name=\"${item.fullName}\"></vaadin-avatar>"
                                + "  <vaadin-vertical-layout style=\"line-height: var(--lumo-line-height-m);\">"
                                + "    <span> ${item.fullName} </span>"
                                + "    <span style=\"font-size: var(--lumo-font-size-s); color: var(--lumo-secondary-text-color);\">"
                                + "      ${item.email}" + "    </span>"
                                + "  </vaadin-vertical-layout>"
                                + "</vaadin-horizontal-layout>")
                .withProperty("pictureUrl", diaryEntity -> {
                    if(diaryEntity.getSanitaryEntity() != null){
                        byte[] profilePhoto = diaryEntity.getSanitaryEntity().getProfilePhoto();
                        if (profilePhoto != null) {
                            return "data:image/png;base64," + BaseDirectoryPath.convertToBase64(profilePhoto);
                            //Si tiene foto de perfil la muestra
                        } else {
                            return "";
                        }
                    }else{
                        return "";
                    }

                })
                .withProperty("fullName", diaryEntity -> {
                    if(diaryEntity.getSanitaryEntity() == null){
                        return null;
                    }else{
                        return diaryEntity.getSanitaryEntity().getName() + " " + diaryEntity.getSanitaryEntity().getLastName();
                    }
                })
                .withProperty("email", diaryEntity -> {
                    if(diaryEntity.getSanitaryEntity() == null){
                        return null;
                    }else{
                        return diaryEntity.getSanitaryEntity().getDni();
                    }
                });
    }

    /**
     * Comprueba si existe solapamiento, si no existe reactiva el horario
     * Si existe, le muestra un mensaje para indicar que no se puede realizar la acción.
     * @param diaryEntity Horario que se quiere reactivar.
     */
    private void openReactivateDiary(DiaryEntity diaryEntity) {
        if(checkDiaries(diaryEntity)){
            diaryEntity.setState(ConstantUtilities.STATE_ACTIVE);
            diaryEntity.setEndTime(null);
            for(int i = 0; i<diaryEntity.getSchedules().size();i++){
                diaryEntity.getSchedules().get(i).setState(ConstantUtilities.STATE_ACTIVE);
                diaryEntity.getSchedules().get(i).setEndTime(null);
            }
            diaryEntity.setEndTime(null);
            diaryService.save(diaryEntity);
            deleteBtn.setVisible(true);
            reactivateBtn.setVisible(false);
            updateGrid();
        }else{ //Le muestra un cuadro de diálogo indicandole que no puede reactivar la agenda.
            FailReactivateDiaryConfirmDialog failReactivateDiaryConfirmDialog = new FailReactivateDiaryConfirmDialog();
            failReactivateDiaryConfirmDialog.open();
        }

    }

    /**
     * Coomprueba que no exista solapamietno entre el horario que se va a reactivar y los horarios existentes.
     * @param diaryEntity El horario que se va a reactivar.
     * @return Verdadero is no existe solapamiento, falso si sí.
     */
    private boolean checkDiaries(DiaryEntity diaryEntity) {
        List<DiaryEntity> diaries = diaryService.findBySanitaryEntityAndState(diaryEntity.getSanitaryEntity(), ConstantUtilities.STATE_ACTIVE);
        for (DiaryEntity oneDiary : diaries) {
            if(makeCheck(oneDiary, diaryEntity)){ //Si hay superposición se muestra un mensaje de error y se sale
                return false;
            }
        }
        return true;
    }

    /**
     * Verificación de superposición de agendas, para que se pueda reactivar la nueva agenda se verifica:
     *       1.La agenda existente empieza antes o al mismo tiempo que diaryEntity y termina después de que diaryEntity haya empezado.
     *       2. La agenda existente empieza antes o al mismo tiempo que diaryEntity termina y termina después o al mismo tiempo que diaryEntity termina.
     *       3. DiaryEntity empieza antes o al mismo tiempo que oneDiary y termina después de que la agenda existente haya terminado.
     *       4. DiaryEntity empieza antes o al mismo tiempo que el diario existente y el diario existente termina después de diaryEntity haya terminado.
     *       5. Ambos terminan al mismo tiempo
     * @param oneDiary Agenda existente
     * @param diaryEntity Agenda que se quiere reactivar
     * @return Verdadero si puede reactivarse, falso si no.
     */
    private boolean makeCheck(DiaryEntity oneDiary, DiaryEntity diaryEntity) {
        LocalDate oneStart = oneDiary.getStartTime();
        LocalDate oneEnd = oneDiary.getEndTime();
        LocalDate otherStart = diaryEntity.getStartTime();
        LocalDate otherEnd = diaryEntity.getEndTime();

        // Caso 1: La agenda existente empieza antes o al mismo tiempo que diaryEntity y termina después de que diaryEntity haya empezado
        boolean case1 = (oneStart.isBefore(otherStart) || oneStart.isEqual(otherStart))
                && (oneEnd == null || otherStart.isBefore(oneEnd) || otherStart.isEqual(oneEnd));

        // Caso 2: La agenda existente empieza antes o al mismo tiempo que diaryEntity termina y termina después o al mismo tiempo que diaryEntity termina
        boolean case2 = (oneStart.isBefore(otherEnd) || oneStart.isEqual(otherEnd))
                && (otherEnd.isBefore(oneEnd) || otherEnd.isEqual(Objects.requireNonNull(oneEnd)));

        // Caso 3: diaryEntity empieza antes o al mismo tiempo que oneDiary y termina después de que la agenda existente haya terminado
        boolean case3 = (otherStart.isBefore(oneStart) || otherStart.isEqual(oneStart)) && (otherEnd.isAfter(oneEnd) || otherEnd.isEqual(Objects.requireNonNull(oneEnd)));

        // Caso 4: diaryEntity empieza antes o al mismo tiempo que el diario existente y el diario existente termina después de diaryEntity haya terminado
        boolean case4 = (otherStart.isBefore(oneStart) || otherStart.isEqual(oneStart))
                && (oneEnd == null || oneEnd.isAfter(otherEnd));

        // Caso 5: ambos terminan al mismo tiempo
        boolean case5 = oneEnd != null && oneEnd.isEqual(otherEnd);

        // Si cualquiera de los casos anteriores se cumple, hay un solapamiento
        return case1 || case2 || case3 || case4 || case5;
    }

    /**
     * Crea un renderer que actualiza y muestra el grid de hotarios dentro de un HL.
     *
     * @return El renderer con el grid.
     */
    private ComponentRenderer<Component, DiaryEntity> createScheduleGrid() {
        return new ComponentRenderer<>(diaryEntity -> {
            updateScheduledGrid(diaryEntity);
            return new HorizontalLayout(scheduleGrid);
        });
    }

    private void updateScheduledGrid(DiaryEntity diaryEntity) {
        scheduleGrid.setItems(scheduleService.findByDiaryEntity(diaryEntity));
    }

    /**
     * Creación del grid de subcategorias con las columnas correspondientes.
     */
    private void setScheduleGrid() {

        scheduleGrid = new Grid<>();

        scheduleGrid.addColumn(scheduleEntity -> "Fecha de inicio: " + scheduleEntity.getStartDate().getDayOfMonth()+"/"+scheduleEntity.getStartDate().getMonthValue()+"/"+scheduleEntity.getStartDate().getYear()).setAutoWidth(true);
        scheduleGrid.addColumn(scheduleEntity -> {
            if(scheduleEntity.getEndingDate() != null){
                return "Fecha de fin: " + scheduleEntity.getEndingDate().getDayOfMonth()+"/"+scheduleEntity.getEndingDate().getMonthValue()+"/"+scheduleEntity.getEndingDate().getYear();
            }
            return "";
        }).setAutoWidth(true);

        scheduleGrid.addColumn(scheduleEntity -> "Hora de inicio: " + scheduleEntity.getStartTime()).setAutoWidth(true);
        scheduleGrid.addColumn(scheduleEntity -> "Hora de finalización: " + scheduleEntity.getEndTime()).setAutoWidth(true);
        scheduleGrid.addColumn(scheduleEntity -> "Máximo de pacientes: " + scheduleEntity.getMaxPatients()).setAutoWidth(true);

        scheduleGrid.addColumn(new ComponentRenderer<>(scheduleEntity -> {
            Button editBtn = createButton(new Icon(VaadinIcon.EDIT), "dark-green-background-button");
            editBtn.addClickListener(event -> openEditScheduleDialog(scheduleEntity.getDiaryEntity(), scheduleEntity));
            editBtn.setTooltipText("Editar horario");
            reactivateScheduleBtn = createButton(new Icon(VaadinIcon.REFRESH), "yellow-color-button");
            reactivateScheduleBtn.addClickListener(event -> openReactivateSchedule(scheduleEntity));
            reactivateScheduleBtn.setTooltipText("Reactivar horario");
            if (scheduleEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                deleteScheduleBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-disable-background-button");
                deleteScheduleBtn.setVisible(false);
                deleteScheduleBtn.getElement().setAttribute("disabled", true);
                return new HorizontalLayout(editBtn, deleteScheduleBtn, reactivateScheduleBtn);
            }
            reactivateScheduleBtn.setVisible(false);
            deleteScheduleBtn = createButton(new Icon(VaadinIcon.TRASH), "lumo-error-color-background-button");
            deleteScheduleBtn.addClickListener(event -> openUnsubscribeScheduleDialog(scheduleEntity));
            deleteScheduleBtn.setTooltipText("Dar de baja horario");
            return new HorizontalLayout(editBtn, deleteScheduleBtn, reactivateScheduleBtn);

        })).setAutoWidth(true).setFrozenToEnd(true).setFlexGrow(0);

        scheduleGrid.setAllRowsVisible(true); //Ajuste del grid dependiendo de las filas
        scheduleGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER); //Quitar el borde del grid

    }

    /**
     * Reactiva un horario si no existen solapamientos entre horas entre otros horarios de la misma agenda.
     * @param scheduleEntity Horario a reactivar.
     */
    private void openReactivateSchedule(ScheduleEntity scheduleEntity) {
        if(makeCheckSchedules(scheduleEntity)){
            scheduleEntity.setState(ConstantUtilities.STATE_ACTIVE);
            scheduleEntity.setEndTime(null);
            scheduleService.save(scheduleEntity);
            deleteScheduleBtn.setVisible(true);
            reactivateScheduleBtn.setVisible(false);
            updateGrid();
        }
    }

    /**
     * Verifica si el horario a reactivar se solapa con alguno de los horarios existentes.
     * @param scheduleEntity Horario a reactivar.
     */
    private boolean makeCheckSchedules(ScheduleEntity scheduleEntity) {
        for (ScheduleEntity oneSchedule : scheduleEntity.getDiaryEntity().getSchedules()) {
            if (makeCheckDates(oneSchedule, scheduleEntity)) { //Si hay superposición se muestra un mensaje de error y se sale
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica si hay solapamiento de fechas, si es así verifica si existe solapamiento de hroas.
     * @param oneSchedule Horario existente.
     * @param scheduleEntity Horario a reactivar.
     * @return Falso si no existe solapameinto y verdadero si sí.
     */
    private boolean makeCheckDates(ScheduleEntity oneSchedule, ScheduleEntity scheduleEntity) {
        if (checkDateOverlap(oneSchedule, scheduleEntity)) {
            // Verificar el solapamiento de horas solo si las fechas se solapan
            return checkTimeOverlap(oneSchedule, scheduleEntity);
        }
        return false;
    }
    /**
     * Verifica si hay solapamiento de horas entre el horario existente y el nuevo.
     *
     * @param existingSchedule Horario existente a comprobar.
     * @param scheduleEntity Horario que se va a reactivar
     * @return true si hay solapamiento de horas, false en caso contrario.
     */
    private boolean checkTimeOverlap(ScheduleEntity existingSchedule, ScheduleEntity scheduleEntity) {
        // Verificar si el rango de horas se solapa
        return (existingSchedule.getStartTime().isBefore(scheduleEntity.getEndTime()) || scheduleEntity.getEndTime() == null) &&
                (existingSchedule.getEndTime() == null || existingSchedule.getEndTime().isAfter(scheduleEntity.getStartTime()));
    }
    /**
     * Verifica si hay solapamiento de fechas entre el horario existente y el nuevo.
     *
     * @param existingSchedule Horario existente a comprobar.
     * @param scheduleEntity Horario que se va a reactivar
     * @return true si hay solapamiento de fechas, false en caso contrario.
     */
    private boolean checkDateOverlap(ScheduleEntity existingSchedule, ScheduleEntity scheduleEntity) {

        // Verificar si el rango de fechas se solapa
        if (((scheduleEntity.getEndingDate()) == null || existingSchedule.getStartDate().isBefore(scheduleEntity.getEndingDate())) &&
                (existingSchedule.getEndingDate() == null || existingSchedule.getEndingDate().isAfter(scheduleEntity.getStartDate()))) {
            return true;
        }else return (existingSchedule.getEndingDate() == null || scheduleEntity.getStartDate().isBefore(existingSchedule.getEndingDate())) &&
                (scheduleEntity.getEndingDate() == null || scheduleEntity.getStartDate().isAfter(existingSchedule.getStartDate()));
    }

    /**
     * Abre el cuadro de diálogo para editar un horario.
     * @param diaryEntity Agenda asociada al horario.
     * @param scheduleEntity Horario a editar.
     */
    private void openEditScheduleDialog(DiaryEntity diaryEntity, ScheduleEntity scheduleEntity) {
        ScheduleDialog scheduleDialog = new ScheduleDialog(diaryEntity, scheduleEntity);
        scheduleDialog.setHeaderTitle("MODIFICAR HORARIO");
        scheduleDialog.addListener(ScheduleDialog.SaveEvent.class, this::saveSchedule);
        scheduleDialog.open();
    }

    /**
     * Abre el cuadro de diálogo para añadir un horario.
     * @param diaryEntity Agenda asociada al horario.
     */
    private void openScheduleDialog(DiaryEntity diaryEntity) {
        ScheduleDialog scheduleDialog = new ScheduleDialog(diaryEntity, null);
        scheduleDialog.setHeaderTitle("AÑADIR HORARIO");
        scheduleDialog.addListener(ScheduleDialog.SaveEvent.class, this::saveSchedule);
        scheduleDialog.open();
    }

    /**
     * Abre el cuadro de diálogo para editar una agenda.
     * @param diaryEntity Agenda a para editar.
     */
    private void openEditDiaryDialog(DiaryEntity diaryEntity) {

        DiaryDialog diaryDialog = new DiaryDialog(diaryEntity, diaryService, sanitaryService.findAll(),
                centerService.findAll(0, 900).getContent(), centerService, sanitaryService);
        diaryDialog.setHeaderTitle("MODIFICAR AGENDA");
        diaryDialog.addListener(DiaryDialog.SaveEvent.class, this::saveDiary);
        diaryDialog.open();

    }

    /**
     * Se abre el cuadro de diálogo en el que se pregunta
     * si está seguro de dar de baja la agenda.
     *
     * @param diaryEntity Agenda que se va a eliminar.
     */
    private void openUnsubscribeDiary(DiaryEntity diaryEntity) {
        DeleteDiaryConfirmDialog deleteDiaryConfirmDialog = new DeleteDiaryConfirmDialog(diaryEntity);
        deleteDiaryConfirmDialog.open();
        deleteDiaryConfirmDialog.addListener(DeleteDiaryConfirmDialog.DeleteEvent.class, this::unsubscribeDiary);
    }

    /**
     * Da de baja el horario
     * @param deleteEvent Evento
     */
    private void unsubscribeDiary(DeleteDiaryConfirmDialog.DeleteEvent deleteEvent) {
        deleteEvent.getDiaryEntity().setEndTime(LocalDate.now()); //Se le aplica una fecha de fin para darla de baja
        deleteEvent.getDiaryEntity().setState(ConstantUtilities.STATE_INACTIVE);
        for(int i = 0 ; i< deleteEvent.getDiaryEntity().getSchedules().size(); i++){
            deleteEvent.getDiaryEntity().getSchedules().get(i).setState(ConstantUtilities.STATE_INACTIVE); //Se dan de baja sus horarios asociados
        }
        deleteBtn.setVisible(false);
        reactivateBtn.setVisible(true);
        diaryService.save(deleteEvent.getDiaryEntity());
        updateGrid();
    }

    private void openUnsubscribeScheduleDialog(ScheduleEntity scheduleEntity) {
        DeleteScheduleConfirmDialog deleteScheduleConfirmDialog = new DeleteScheduleConfirmDialog(scheduleEntity);
        deleteScheduleConfirmDialog.open();
        deleteScheduleConfirmDialog.addListener(DeleteScheduleConfirmDialog.DeleteEvent.class, this::unsubscribeSchedule);

    }

    private void unsubscribeSchedule(DeleteScheduleConfirmDialog.DeleteEvent deleteEvent) {
        deleteEvent.getScheduleEntity().setEndingDate(LocalDate.now()); //Se le aplica una fecha de fin para darla de baja
        deleteEvent.getScheduleEntity().setState(ConstantUtilities.STATE_INACTIVE);
        scheduleService.save(deleteEvent.getScheduleEntity());
    }

    private String makeAndAppointment(boolean value) {
        if (value) {
            return "Si";
        } else {
            return "No";
        }
    }

}
