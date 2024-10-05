package es.obstetrics.obstetric.view.allUsers.dialog;

import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;

public class PdfDialog extends MasterPublicConfirmDialog {
    private final PdfViewer pdfViewer;
    private final String title;

    public PdfDialog(byte[] bytes, String title){
        pdfViewer = new PdfViewer();
        StreamResource pdf = new StreamResource("document.pdf", () -> new ByteArrayInputStream(bytes));
        pdfViewer.setAddPrintButton(true); //Botón de imprimir activo
        pdfViewer.setSrc(pdf);
        this.title = title;
        createHeaderAndTextDialog();
    }
    /**
     * Creación de la cabecera y el texto del cuadro de diálogo.
     *  El texto estará compuesto por el pdf correspondiente y la cabecerá
     *      será el título del PDF (puesto por el sanitario anteriormente).
     */
    @Override
    public void createHeaderAndTextDialog() {
        setHeader(title);
        setText(pdfViewer);
    }

    /**
     * Evento de cierre del cuadro de diálogo
     */
    @Override
    public void clickButton() {
        close();
    }
}
