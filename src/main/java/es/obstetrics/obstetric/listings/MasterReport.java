package es.obstetrics.obstetric.listings;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

@Getter
public abstract class MasterReport {

    protected static final Logger log = LogManager.getLogger(MasterReport.class);
    protected Document document;

    @Setter
    protected String fileName;

    @Setter
    private int fontSize = 12;  // Default font size

    public MasterReport(String fileName) {
        this.fileName = fileName;
        this.document = new Document(PageSize.A4);
    }

    /**
     * Method to be implemented by subclasses to create the PDF.
     */
    public abstract InputStream generatePdf();

}
