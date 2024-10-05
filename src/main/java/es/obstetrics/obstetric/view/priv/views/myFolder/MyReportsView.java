package es.obstetrics.obstetric.view.priv.views.myFolder;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import es.obstetrics.obstetric.backend.entity.*;
import es.obstetrics.obstetric.backend.service.ReportService;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.listings.pdf.GynecologistReportPdf;
import es.obstetrics.obstetric.listings.pdf.MatronReportPdf;
import es.obstetrics.obstetric.view.priv.PrincipalView;
import es.obstetrics.obstetric.view.priv.home.HomeView;
import es.obstetrics.obstetric.view.priv.templates.UserHeaderTemplate;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value="patients/my-my-reports", layout = PrincipalView.class)
public class MyReportsView extends Div {
    private final Grid<ReportEntity> reportsGrid;
    private final ReportService reportService;
    private final UserCurrent current;

    public MyReportsView(ReportService reportService, UserCurrent current){
        this.reportService = reportService;
        this.current = current;
        UserHeaderTemplate header = new UserHeaderTemplate(VaadinIcon.HOME.create(), new UserEntity(), new H3("Mis informes"));
        header.getButton().addClickListener(buttonClickEvent ->
                UI.getCurrent().navigate(HomeView.class)//Se dirige a la ventana anterior
        );
        reportsGrid = new Grid<>(ReportEntity.class, false);
        add(header, createReportBox());
    }

    /**
     * Añade los FL responsivos de datos de informes a otro FL
     * @return El HL con los FL responsivos.
     */
    private Component createReportBox() {
        HorizontalLayout allDatesHl = new HorizontalLayout(createReportsDataGrid());
        allDatesHl.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        allDatesHl.setSizeFull();
        allDatesHl.setPadding(true);
        return allDatesHl;
    }

    /**
     * Crea el FormLayout con los informes del paciente.
     * @return El FormLayout con los informes.
     */
    private FormLayout createReportsDataGrid() {
        reportsGrid.addColumn(reportEntity -> {
            if (reportEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                return reportEntity.getDate().getDayOfMonth() + "-" + reportEntity.getDate().getMonthValue() + "-" + reportEntity.getDate().getYear();
            }
            return null;
        }).setHeader("Día").setAutoWidth(true);

        reportsGrid.addColumn(reportEntity -> {
            if (reportEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                return reportEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity();
            }
            return null;
        }).setHeader("Sanitario").setAutoWidth(true);

        reportsGrid.addColumn(reportEntity -> {
            if (reportEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {
                return reportEntity.getAppointmentEntity().getAppointmentTypeEntity().getDescription();
            }
            return null;
        }).setHeader("Tipo de cita").setAutoWidth(true);

        reportsGrid.addColumn(new ComponentRenderer<>(reportEntity -> {
            if (reportEntity.getState().equals(ConstantUtilities.STATE_INACTIVE)) {

                Button printButton = createButton(VaadinIcon.DOWNLOAD.create());
                printButton.setTooltipText("Descargar informe");
                printButton.addClickListener(event -> {
                    Anchor downloadLink = getAnchor(reportEntity);
                    downloadLink.getElement().setAttribute("download", true);
                    downloadLink.getElement().setAttribute("hidden", true);
                    add(downloadLink);
                    downloadLink.getElement().callJsFunction("click");

                    new Thread(() -> {
                        try {
                            Thread.sleep(1000); // Esperar 1 segundo antes de eliminar el Anchor
                        } catch (InterruptedException e) {
                            e.getMessage();
                        }
                        getUI().ifPresent(ui -> ui.access(() -> {
                            remove(downloadLink);
                        }));
                    }).start();
                });
                return new HorizontalLayout(printButton);
            }
            return null;
        })).setHeader("PDF").setAutoWidth(true).setFrozenToEnd(true);

        reportsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        refreshReportsGrid();

        Button refreshIcon = createIconButton();
        refreshIcon.addClickListener(event -> refreshReportsGrid()); // Carga de nuevo los informes

        HorizontalLayout title = createTitleH4(createIcon(), refreshIcon);

        FormLayout box = createFormLayout();
        box.add(title, reportsGrid);
        box.setColspan(title, 2);
        box.setColspan(reportsGrid, 2);
        box.setSizeFull();
        box.addClassName("square-box");
        return box;
    }

    private Anchor getAnchor(ReportEntity reportEntity) {
        StreamResource resource;
        if (reportEntity.getAppointmentEntity().getScheduleEntity().getDiaryEntity().getSanitaryEntity().getRole().equals(ConstantUtilities.ROLE_GYNECOLOGIST)) {
            resource = new StreamResource("informe.pdf", () -> new GynecologistReportPdf((GynecologistReportEntity) reportEntity).generatePdf());

        } else {
            resource = new StreamResource("informe.pdf", () -> new MatronReportPdf((MatronReportEntity) reportEntity).generatePdf());
        }
        return new Anchor(resource, "");
    }

    private void refreshReportsGrid() {
        if (current.getCurrentUser() != null) {
            reportsGrid.setItems(reportService.findByPatientEntityAndState((PatientEntity) current.getCurrentUser(), ConstantUtilities.STATE_INACTIVE));
        }
    }

    private Button createButton(Icon icon) {
        Button button = new Button(icon);
        button.setTooltipText("Descargar");
        button.addClassName("dark-green-background-button");
        return button;
    }

    /**
     * Crea un botón y le da el estilo de color verde.
     *
     * @return El botón.
     */
    private Button createIconButton() {
        Button button = new Button(new Icon(VaadinIcon.REFRESH));
        button.setTooltipText("Refrescar");
        button.addClassName("interactive-button");
        return button;
    }

    /**
     * Crea un icono con mediante un VaadinIcon y le da el estilo de color verde.
     *
     * @return El icono.
     */
    private Icon createIcon() {
        Icon icon = new Icon(VaadinIcon.CLIPBOARD_HEART);
        icon.setColor("var(--dark-green-color)");
        return icon;
    }

    /**
     * Crea un FormLayout.
     * @return El FormLayout responsivo.
     */
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        return formLayout;
    }

    /**
     * Crea el título de cabecera de cada una de las box.
     *
     * @param icon         El icono relacionado con el título.
     * @param iconRefresh  El icono de refrescar (no será nulo cuando el box contenga un grid)
     * @return El HorizontalLayout con todos los datos.
     */
    private HorizontalLayout createTitleH4(Icon icon, Button iconRefresh) {
        H4 title = new H4("Informes");
        title.addClassNames("title-dark-green", "margin-title-dark-green");
        HorizontalLayout horizontalLayoutTitleAndIcon = new HorizontalLayout(title, icon);
        horizontalLayoutTitleAndIcon.setSizeFull();
        horizontalLayoutTitleAndIcon.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

        HorizontalLayout horizontalLayoutIconRight = new HorizontalLayout(iconRefresh);
        horizontalLayoutIconRight.setSizeFull();
        horizontalLayoutIconRight.setSizeFull();
        horizontalLayoutIconRight.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        return new HorizontalLayout(horizontalLayoutTitleAndIcon, horizontalLayoutIconRight);
    }

}
