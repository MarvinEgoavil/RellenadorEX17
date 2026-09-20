package com.alucardstudio.rellenadorex17;

import com.alucardstudio.rellenadorex17.extractor.ExtractorEx03;
import com.alucardstudio.rellenadorex17.extractor.ExtractorEx25;
import com.alucardstudio.rellenadorex17.extractor.ExtractorEx26;
import com.alucardstudio.rellenadorex17.extractor.ExtractorEx31;
import com.alucardstudio.rellenadorex17.extractor.ExtractorEx32;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Coordinador de extracción.
 *
 * Detecta EX-03 / EX-25 / EX-26 / EX-31 / EX-32 y delega el trabajo
 * al extractor independiente correspondiente.
 */
public final class ExtractorDatosPdf {

    private static String ultimoTipoDetectado;

    private ExtractorDatosPdf() {
    }

    public static DatosPersona extraer(File archivoPdf) throws IOException {

        if (archivoPdf == null || !archivoPdf.isFile()) {
            throw new IOException("El documento origen no existe o no es válido.");
        }

        ultimoTipoDetectado = null;

        try (PDDocument documento = Loader.loadPDF(archivoPdf)) {

            String texto = leerPrimeraPagina(documento);
            String tipoFormulario = detectarTipoFormulario(documento, texto, archivoPdf.getName());
            ultimoTipoDetectado = tipoFormulario;

            switch (tipoFormulario) {
                case "EX-03":
                    return ExtractorEx03.extraer(documento);
                case "EX-25":
                    return ExtractorEx25.extraer(documento);
                case "EX-26":
                    return ExtractorEx26.extraer(documento);
                case "EX-31":
                    return ExtractorEx31.extraer(documento);
                case "EX-32":
                    return ExtractorEx32.extraer(documento);
                default:
                    throw new IOException("Tipo de formulario no compatible.");
            }

        } catch (IOException e) {
            AppLogger.error("EXTRACCION_DATOS", archivoPdf, ultimoTipoDetectado, e);
            throw e;

        } catch (RuntimeException e) {
            AppLogger.error("EXTRACCION_DATOS", archivoPdf, ultimoTipoDetectado, e);
            throw new IOException(
                    "Se produjo un error inesperado al procesar el formulario.",
                    e
            );
        }
    }

    public static String getUltimoTipoDetectado() {
        return ultimoTipoDetectado;
    }

    private static String leerPrimeraPagina(PDDocument documento)
            throws IOException {

        if (documento.getNumberOfPages() == 0) {
            return "";
        }

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(1);
        stripper.setSortByPosition(true);

        return stripper.getText(documento);
    }

    private static String detectarTipoFormulario(
            PDDocument documento,
            String texto,
            String nombreArchivo) throws IOException {

        String normalizado = normalizar(texto);

        String directo = detectarPorTexto(normalizado);
        if (directo != null) {
            return directo;
        }

        String porAcroForm = detectarPorAcroForm(documento);
        if (porAcroForm != null) {
            return porAcroForm;
        }

        String porNombre = detectarPorNombreArchivo(nombreArchivo);
        if (porNombre != null) {
            return porNombre;
        }

        if (normalizado.contains("MENOR")
                || normalizado.contains("MENORES")
                || normalizado.contains("TUTELA")
                || normalizado.contains("DESPLAZAMIENTO")) {
            return "EX-25";
        }

        throw new IOException(
                "No se reconoce el formulario. Utilice un EX-03, EX-25, "
                + "EX-26, EX-31 o EX-32 compatible."
        );
    }

    private static String detectarPorTexto(String texto) {

        if (contieneFormulario(texto, "03")) {
            return "EX-03";
        }
        if (contieneFormulario(texto, "25")) {
            return "EX-25";
        }
        if (contieneFormulario(texto, "26")) {
            return "EX-26";
        }
        if (contieneFormulario(texto, "31")) {
            return "EX-31";
        }
        if (contieneFormulario(texto, "32")) {
            return "EX-32";
        }

        return null;
    }

    private static String detectarPorNombreArchivo(String nombreArchivo) {

        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            return null;
        }

        String nombre = nombreArchivo
                .toUpperCase()
                .replace('\u00A0', ' ');

        /*
         * No usamos \\b después del número porque "_" cuenta como carácter
         * de palabra en regex. Así se reconocen, por ejemplo:
         * EX03_SOLICITUD.pdf y Formulario_EX31_E08202600351347.pdf.
         */
        if (nombre.matches("(?s).*(?<![A-Z0-9])EX\\s*[-_ ]?\\s*0?3(?!\\d).*")) {
            return "EX-03";
        }
        if (nombre.matches("(?s).*(?<![A-Z0-9])EX\\s*[-_ ]?\\s*25(?!\\d).*")) {
            return "EX-25";
        }
        if (nombre.matches("(?s).*(?<![A-Z0-9])EX\\s*[-_ ]?\\s*26(?!\\d).*")) {
            return "EX-26";
        }
        if (nombre.matches("(?s).*(?<![A-Z0-9])EX\\s*[-_ ]?\\s*31(?!\\d).*")) {
            return "EX-31";
        }
        if (nombre.matches("(?s).*(?<![A-Z0-9])EX\\s*[-_ ]?\\s*32(?!\\d).*")) {
            return "EX-32";
        }

        return null;
    }

    private static String detectarPorAcroForm(PDDocument documento) {

        PDAcroForm form = documento.getDocumentCatalog().getAcroForm();

        if (form == null) {
            return null;
        }

        StringBuilder huella = new StringBuilder();

        for (PDField campo : form.getFieldTree()) {
            agregar(huella, campo.getFullyQualifiedName());
            agregar(huella, campo.getPartialName());
            agregar(huella, campo.getAlternateFieldName());

            try {
                agregar(huella, campo.getValueAsString());
            } catch (Exception ignored) {
                // Un campo problemático no invalida el resto del formulario.
            }
        }

        return detectarPorTexto(normalizar(huella.toString()));
    }

    private static void agregar(StringBuilder destino, String valor) {
        if (valor != null && !valor.isBlank()) {
            destino.append(' ').append(valor);
        }
    }

    private static boolean contieneFormulario(String texto, String numero) {

        return texto.matches(
                "(?s).*\\bEX\\s*[-–—]?\\s*0?" + numero + "\\b.*"
        );
    }

    private static String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .toUpperCase()
                .replace('\u00A0', ' ')
                .replace('–', '-')
                .replace('—', '-')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
