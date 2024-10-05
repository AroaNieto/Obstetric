package es.obstetrics.obstetric.view.priv.grid;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Clase abstracta que sirve de base para la creación de todos los grid que se registren en la aplicación.
 * @param <T> Parámetro genérico que configurara la clase a la que pertenece el grid creado.
 */
public abstract class MasterGrid<T> extends VerticalLayout {

    protected Grid<T> masterGrid;
    protected GridListDataView<T> gridListDataView;
    protected HorizontalLayout filterContainerHl;
    protected FlexLayout filterContainerFl;
    protected Div headerVl;
    protected Button addBtn;
    protected TextField searchTextField;
    protected  FormLayout filterContainer;

    /**
     * Constructor de la clase, añade:
     *  - Un vl dónde irán los datos de la cabecera.
     *  - Un hl que contendrá los diferentes filtros con los que se podrán buscar los datos del grid.
     *  - El grid.
     */
    public MasterGrid() {

        masterGrid = new Grid<>();
        addBtn = createButton(new Icon(VaadinIcon.PLUS));
        addBtn.addClickListener(event -> this.openDialog());

        filterContainerHl = new HorizontalLayout();
        filterContainerFl = new FlexLayout();
        filterContainer = new FormLayout();
        headerVl = new Div();

        searchTextField = new TextField(); //Barra de búsqueda
        searchTextField.setPlaceholder("Buscar");
        searchTextField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER);

        add(headerVl,filterContainerHl, filterContainerFl, masterGrid);
        setHorizontalComponentAlignment(Alignment.CENTER,headerVl);
        setSizeFull();
    }

    private Button createButton(Icon icon){
        Button button = new Button(icon);
        button.addClassName("lumo-primary-color-background-button");
        return button;
    }
    /**
     * Método abstracto que las clases usarán para abrir el cuadro de diálogo
     */
    public abstract void openDialog();

    /**
     * Método que las clases usarán para establecer el contenido de la cabecera
     */
    public void setHeader(H2 title){
        headerVl.add(title);

    }

    /**
     * Método abstracto que las clases usarán para  establecer los filtros
     */
    public abstract void setFilterContainer();

    /**
     * Método abstracto que las clases usarán para establecer los atributos del grid.
     */
    public abstract void setGrid();

    /**
     * Método abstracto que las clases usarán para actualizar los atributos del grid.
     */
    public abstract void updateGrid();

}
