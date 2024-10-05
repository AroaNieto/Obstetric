package es.obstetrics.obstetric.backend.utilities;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FilesUtilities {
    /**
     * Comprueba si un directorio existe en la ruta pasada,
     *  si no existe, se crea.
     * @param fileName Ruta del fichero
     * @return Verdadero o falso si el fichero existe.
     */
    public static boolean directoryExists(String fileName) {
        boolean exists = false;
        try {
            File dir = new File(fileName);
            if (!dir.exists()) {
                exists = dir.mkdirs(); //El directorio existe
            } else {
                exists = true;
            }
        } catch (Exception ex) {
        }
        return exists;
    }

    public static boolean fileExists(String fileName) {
        return new File(fileName).exists(); //Devuelve verdadero si existe el fichero sino falso
    }

    /**
     * Convierte un archivo en un array de bytes. Lee todos los bytes del archivo
     *  y los devuelve como un array de bytes.
     * @param fileName Nombre del archivo
     * @return Array de bytes
     */
    public static byte[] fileToByte(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytesArray = new byte[(int) file.length()];
        fileInputStream.read(bytesArray); // Lee los bytes del archivo en el array
        fileInputStream.close();

        return bytesArray;
    }

    /**
     * Recoge un array de bits y los escribe en un archivo del sistema de ficheros.
     * @param buffer Array de bytes que se desea escribir en el archivo.
     * @param pathname Ruta completa del archivo en el que se desea escribir los bytes
     * @return El fichero
     */
    public static File bytestoFile(byte[] buffer, String pathname) {
        File file = null;
        try {
            FileOutputStream fileOutputStream;
            file = new File(pathname);
            if (!file.isFile()) {
                fileOutputStream = new FileOutputStream(file);
                for (byte b : buffer) {
                    fileOutputStream.write(b);
                }
                fileOutputStream.close();
                log.debug("Fichero grabado " + pathname);
            } else {
                log.debug("Fichero ya existe " + pathname);
            }
        } catch (Exception e) {
           // log.error(Utilidades.getStackTrace(e));
        }
        return file;
    }

    /**
     * Escribe los datos en el archivo especificado.
     *
     * @param inputStream Fluo de entrada de donde se leerán los datos para escribir en el archivo.
     * @param fileName Nombre del archivo en el que se escribiran lso datos
     * @return El fichero
     */
    public static File iStoFile(InputStream inputStream, String fileName) {
        OutputStream outputStream = null;
        File fichero = null;
        try {
            fichero = new File(fileName);
            if (fichero.exists() && fichero.isFile()) {
                fichero.delete();
            }

            fichero = new File(fileName);
            outputStream = new FileOutputStream(fichero);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            log.debug("Fichero creado :{}", fileName);
        } catch (FileNotFoundException e) {
            log.error("Fichero no encontrado:{}", fileName);
        } catch (IOException e) {
            log.error("Fallo al leer o escribir{}", fileName);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (java.io.IOException e) {
                log.error("Fallo IO cerrando outputStream" );
            }
        }
        return fichero;
    }

    /**
     * Coje los datos de un archivo PDF y crea una miniatura de la primera págna del PDF
     *  como una imágen JPEG y devuelve la ruta de la miniatura creada.
     * @return
     */
    public static String setThumbnailOfPdfInByte(String name, byte[] content) {
        String output = null;
        try {
            String filename = BaseDirectoryPath.ABSOLUTE_PATH_PDF + System.getProperty("file.separator") + name + ".pdf"; //Ruta del fichero
            File file = bytestoFile(content, filename);

            output = BaseDirectoryPath.ABSOLUTE_PATH_IMG + System.getProperty("file.separator") + name + ".jpg";

            PDDocument document = Loader.loadPDF(file);
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImage(0);

            ImageIO.write(image, "JPEG", new File(output));
            document.close();

        } catch (IOException ex) {
            //log.equals("Fichero:" + nombre + "\n" + Utilidades.getStackTrace(ex));
        }
        return output;
    }

    public String getFileHtml(String path){
        StringBuilder content = new StringBuilder();
        File file = new File(path);
        if (!file.exists()) {
            log.error("El archivo no existe: " + path);
            return null;
        }
        try (InputStream inputStream = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            log.error("Error leyendo el archivo: " + path, e);
            return null;
        }

        // Convertir el contenido Markdown a HTML
        Parser parser = Parser.builder().build();
        Node document = parser.parse(content.toString());
        HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();

        return renderer.render(document);
    }
}