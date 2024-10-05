package es.obstetrics.obstetric.view.priv.dialog;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;

public class MasterListingsDialog extends Dialog {

    public MasterListingsDialog(PdfViewer pdfViewer){
        setWidth("1000px");
        open();
        add(pdfViewer);
        Button cancelBtn = new Button(VaadinIcon.CLOSE.create(), e -> closeDialog());
        cancelBtn.addClassName("lumo-error-color-background-button");
        getHeader().add(cancelBtn);
        setModal(true);
        setResizable(true);
    }

    private void closeDialog() {
        close();
    }
}
