package es.obstetrics.obstetric.backend.utilities;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@Log4j2
public class BaseDirectoryPath {

    private Path URI = null;

    public static final String RELATIVE_PATH_PDF = "contentPdf"; //Subcarpeta donde se escriben/leen los pdfs
    public static final String RELATIVE_IMG_PDF = "contentImg"; //Subcarpeta donde se escriben/leen los pdfs
    public  static final String ABSOLUTE_PATH_IMG =System.getProperty("file.separator") + "var" + System.getProperty("file.separator") + "obstetric" + System.getProperty("file.separator") + RELATIVE_IMG_PDF;
    public static final String ABSOLUTE_PATH_PDF = System.getProperty("file.separator") + "var" + System.getProperty("file.separator") + "obstetric" + System.getProperty("file.separator") + RELATIVE_PATH_PDF;
    public String resourcePDF = null;

    public BaseDirectoryPath() {
        FilesUtilities.directoryExists(ABSOLUTE_PATH_PDF); //Comprobación de si existe la carpeta
        Path uri;
        try {
            uri = Paths.get(this.getClass().getResource("/").toURI());
            resourcePDF = Paths.get(uri.toAbsolutePath() + System.getProperty("file.separator") + RELATIVE_PATH_PDF).toString();
        } catch (URISyntaxException ex) {
            Logger.getLogger(BaseDirectoryPath.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Convierte un byte[] a base64
    public static String convertToBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }

}
