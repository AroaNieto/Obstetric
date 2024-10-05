package es.obstetrics.obstetric.view.priv.dialog;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import es.obstetrics.obstetric.backend.utilities.ConstantUtilities;
import es.obstetrics.obstetric.backend.utilities.FilesUtilities;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class WindowHelp extends Dialog {

    public WindowHelp(String file, String path, String tittle) {

        setModal(true);
        setResizable(true);
        setMinWidth("700px");
        setMaxWidth("700px");
        setMinHeight("600px");
        setMaxHeight("600px");

        Button cancelBtn = new Button(VaadinIcon.CLOSE.create(), e -> close());
        cancelBtn.addClassName("lumo-error-color-background-button");
        getHeader().add(cancelBtn);

        if (tittle != null) {
            Div div = new Div(tittle);
            div.getStyle().set("font-family", "Courier New");
            div.getStyle().set("font-size", "18");
            div.getStyle().set("font-weight", "bold");
            setHeaderTitle(tittle);
        }

        if (file != null && !file.isEmpty() && path != null) {
            // Recupera el fichero .md con el contenido de la ayuda
            String stringMdFile = new FilesUtilities().getFileHtml(path + "/" + file + ConstantUtilities.EXTENSION_MARKDOWN);

            if (stringMdFile != null && !stringMdFile.isEmpty()) {
                Parser parser = Parser.builder().build();
                Node document = parser.parse(stringMdFile);

                HtmlRenderer renderer = HtmlRenderer.builder().build();
                String html = renderer.render(document);

                Div div = new Div(new Html("<span>" + html + " </span>"));
                div.getStyle().set("font-family", "Courier New");
                div.getStyle().set("font-size", "10");
                add(div);
            }
            open();
        }
    }
}