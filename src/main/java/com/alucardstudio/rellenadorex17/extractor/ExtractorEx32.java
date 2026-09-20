package com.alucardstudio.rellenadorex17.extractor;

import com.alucardstudio.rellenadorex17.DatosPersona;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Extractor independiente para formularios EX-32.
 *
 * Estrategia:
 * 1) AcroForm, cuando el formulario conserva sus campos editables.
 * 2) Regiones posicionales de la primera página como fallback para PDFs aplanados.
 *
 * No contiene datos personales hardcodeados.
 */
public final class ExtractorEx32 {

    private ExtractorEx32() {
    }

    public static DatosPersona extraer(PDDocument documento) throws IOException {

        if (documento == null || documento.getNumberOfPages() == 0) {
            throw new IOException("El EX-32 no contiene páginas.");
        }

        DatosPersona datos = new DatosPersona();

        boolean obtenidoPorAcroForm = extraerAcroForm(documento, datos);

        if (!obtenidoPorAcroForm) {
            extraerPorPosicion(documento, datos);
            detectarSexoYEstadoCivilPosicional(documento, datos);
        }

        validarMinimos(datos);
        return datos;
    }

    // -------------------------------------------------------------------------
    // ACROFORM
    // -------------------------------------------------------------------------

    private static boolean extraerAcroForm(PDDocument documento, DatosPersona datos) {

        PDAcroForm form = documento.getDocumentCatalog().getAcroForm();

        if (form == null) {
            return false;
        }

        // En el EX-32 oficial, la sección 1 usa Texto1 y Texto5..Texto23.
        // Texto2..Texto4 corresponden a las partes del NIE y pueden estar vacíos.
        String pasaporte = valor(form, "Texto1");
        String apellido1 = valor(form, "Texto5");
        String apellido2 = valor(form, "Texto6");
        String nombre = valor(form, "Texto7");

        if (vacio(pasaporte) && vacio(apellido1) && vacio(apellido2) && vacio(nombre)) {
            return false;
        }

        datos.pasaporte = limpiar(pasaporte);
        datos.primerApellido = limpiar(apellido1);
        datos.segundoApellido = limpiar(apellido2);
        datos.nombre = limpiar(nombre);

        datos.diaNacimiento = limpiarCompacto(valor(form, "Texto8"));
        datos.mesNacimiento = limpiarCompacto(valor(form, "Texto9"));
        datos.anioNacimiento = limpiarCompacto(valor(form, "Texto10"));

        datos.lugarNacimiento = limpiar(valor(form, "Texto11"));
        datos.paisNacimiento = limpiar(valor(form, "Texto12"));
        datos.nacionalidad = limpiar(valor(form, "Texto13"));

        datos.nombrePadre = limpiar(valor(form, "Texto14"));
        datos.nombreMadre = limpiar(valor(form, "Texto15"));

        datos.domicilio = limpiar(valor(form, "Texto16"));
        datos.numeroDomicilio = limpiarCompacto(valor(form, "Texto17"));
        datos.piso = limpiar(valor(form, "Texto18"));

        datos.localidad = limpiar(valor(form, "Texto19"));
        datos.codigoPostal = limpiarCompacto(valor(form, "Texto20"));
        datos.provincia = limpiar(valor(form, "Texto21"));

        datos.telefono = limpiarCompacto(valor(form, "Texto22"));
        datos.email = limpiar(valor(form, "Texto23"));

        detectarSexoAcroForm(form, datos);
        detectarEstadoCivilAcroForm(form, datos);

        return true;
    }

    private static void detectarSexoAcroForm(PDAcroForm form, DatosPersona datos) {

        // Orden visual del EX-32:
        // checkbox 1 = X, checkbox 2 = H, checkbox 3 = M.
        if (marcado(form, "Casilla de verificación1")) {
            datos.sexo = "X";
        } else if (marcado(form, "Casilla de verificación2")) {
            datos.sexo = "H";
        } else if (marcado(form, "Casilla de verificación3")) {
            datos.sexo = "M";
        }
    }

    private static void detectarEstadoCivilAcroForm(PDAcroForm form, DatosPersona datos) {

        // Orden visual del EX-32:
        // 4=S, 5=C, 6=V, 7=D, 8=Sp.
        if (marcado(form, "Casilla de verificación4")) {
            datos.estadoCivil = "S";
        } else if (marcado(form, "Casilla de verificación5")) {
            datos.estadoCivil = "C";
        } else if (marcado(form, "Casilla de verificación6")) {
            datos.estadoCivil = "V";
        } else if (marcado(form, "Casilla de verificación7")) {
            datos.estadoCivil = "D";
        } else if (marcado(form, "Casilla de verificación8")) {
            datos.estadoCivil = "SP";
        }
    }

    // -------------------------------------------------------------------------
    // FALLBACK POSICIONAL - PRIMERA PÁGINA
    // -------------------------------------------------------------------------

    private static void extraerPorPosicion(PDDocument documento, DatosPersona datos)
            throws IOException {

        PDFTextStripperByArea area = new PDFTextStripperByArea();
        area.setSortByPosition(true);

        agregarRegion(area, "pasaporte",    100, 156, 205, 22);
        agregarRegion(area, "apellido1",     95, 176, 250, 22);
        agregarRegion(area, "apellido2",    389, 176, 178, 22);
        agregarRegion(area, "nombre",        86, 194, 260, 22);

        agregarRegion(area, "dia",          130, 214,  30, 20);
        agregarRegion(area, "mes",          159, 214,  30, 20);
        agregarRegion(area, "anio",         191, 214,  43, 20);
        agregarRegion(area, "lugar",        252, 214, 180, 22);
        agregarRegion(area, "pais",         450, 214, 116, 22);

        agregarRegion(area, "nacionalidad", 100, 232, 230, 22);
        agregarRegion(area, "padre",        115, 250, 190, 22);
        agregarRegion(area, "madre",        380, 250, 186, 22);

        agregarRegion(area, "domicilio",    123, 269, 355, 22);
        agregarRegion(area, "numero",       495, 269,  32, 22);
        agregarRegion(area, "piso",         537, 269,  30, 22);

        agregarRegion(area, "localidad",     97, 286, 235, 22);
        agregarRegion(area, "cp",           355, 286,  70, 22);
        agregarRegion(area, "provincia",    468, 286,  98, 22);

        agregarRegion(area, "telefono",     102, 303, 170, 22);
        agregarRegion(area, "email",        292, 303, 274, 22);

        area.extractRegions(documento.getPage(0));

        datos.pasaporte = limpiarCompacto(area.getTextForRegion("pasaporte"));
        datos.primerApellido = limpiar(area.getTextForRegion("apellido1"));
        datos.segundoApellido = limpiar(area.getTextForRegion("apellido2"));
        datos.nombre = limpiar(area.getTextForRegion("nombre"));

        datos.diaNacimiento = limpiarCompacto(area.getTextForRegion("dia"));
        datos.mesNacimiento = limpiarCompacto(area.getTextForRegion("mes"));
        datos.anioNacimiento = limpiarCompacto(area.getTextForRegion("anio"));

        datos.lugarNacimiento = limpiar(area.getTextForRegion("lugar"));
        datos.paisNacimiento = limpiar(area.getTextForRegion("pais"));
        datos.nacionalidad = limpiar(area.getTextForRegion("nacionalidad"));

        datos.nombrePadre = limpiar(area.getTextForRegion("padre"));
        datos.nombreMadre = limpiar(area.getTextForRegion("madre"));

        datos.domicilio = limpiar(area.getTextForRegion("domicilio"));
        datos.numeroDomicilio = limpiarCompacto(area.getTextForRegion("numero"));
        datos.piso = limpiar(area.getTextForRegion("piso"));

        datos.localidad = limpiar(area.getTextForRegion("localidad"));
        datos.codigoPostal = limpiarCompacto(area.getTextForRegion("cp"));
        datos.provincia = limpiar(area.getTextForRegion("provincia"));

        datos.telefono = limpiarCompacto(area.getTextForRegion("telefono"));
        datos.email = limpiar(area.getTextForRegion("email"));
    }

    private static void agregarRegion(
            PDFTextStripperByArea area,
            String nombre,
            double x,
            double y,
            double ancho,
            double alto) {

        area.addRegion(nombre, new Rectangle2D.Double(x, y, ancho, alto));
    }

    // -------------------------------------------------------------------------
    // SEXO / ESTADO CIVIL PARA PDF APLANADO
    // -------------------------------------------------------------------------

    private static void detectarSexoYEstadoCivilPosicional(
            PDDocument documento,
            DatosPersona datos) throws IOException {

        List<MarcaPdf> marcas = new ArrayList<>();

        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void processTextPosition(TextPosition text) {

                String c = text.getUnicode();

                if ("?".equals(c)
                        || "✔".equals(c)
                        || "✓".equals(c)
                        || "X".equalsIgnoreCase(c)) {

                    marcas.add(new MarcaPdf(
                            text.getXDirAdj(),
                            text.getYDirAdj()
                    ));
                }

                super.processTextPosition(text);
            }
        };

        stripper.setStartPage(1);
        stripper.setEndPage(1);
        stripper.getText(documento);

        for (MarcaPdf marca : marcas) {

            float x = marca.x;
            float y = marca.y;

            // Sexo: X / H / M
            if (cerca(y, 205f, 12f)) {
                if (cerca(x, 480f, 18f)) {
                    datos.sexo = "X";
                } else if (cerca(x, 514f, 18f)) {
                    datos.sexo = "H";
                } else if (cerca(x, 544f, 18f)) {
                    datos.sexo = "M";
                }
            }

            // Estado civil: S / C / V / D / Sp
            if (cerca(y, 241f, 12f)) {
                if (cerca(x, 421f, 18f)) {
                    datos.estadoCivil = "S";
                } else if (cerca(x, 450f, 18f)) {
                    datos.estadoCivil = "C";
                } else if (cerca(x, 479f, 18f)) {
                    datos.estadoCivil = "V";
                } else if (cerca(x, 509f, 18f)) {
                    datos.estadoCivil = "D";
                } else if (cerca(x, 536f, 18f)) {
                    datos.estadoCivil = "SP";
                }
            }
        }
    }

    private static boolean cerca(float valor, float objetivo, float tolerancia) {
        return Math.abs(valor - objetivo) <= tolerancia;
    }

    private static final class MarcaPdf {

        private final float x;
        private final float y;

        private MarcaPdf(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    // -------------------------------------------------------------------------
    // UTILIDADES
    // -------------------------------------------------------------------------

    private static String valor(PDAcroForm form, String nombreCampo) {

        try {
            PDField field = form.getField(nombreCampo);

            if (field == null) {
                return "";
            }

            String valor = field.getValueAsString();
            return valor == null ? "" : valor;

        } catch (Exception ex) {
            return "";
        }
    }

    private static boolean marcado(PDAcroForm form, String nombreCampo) {

        String v = valor(form, nombreCampo);

        if (v == null) {
            return false;
        }

        v = v.trim();

        return !v.isEmpty()
                && !"Off".equalsIgnoreCase(v)
                && !"0".equals(v)
                && !"No".equalsIgnoreCase(v);
    }

    private static String limpiar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace('\u00A0', ' ')
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private static String limpiarCompacto(String texto) {
        return limpiar(texto).replaceAll("\\s+", "");
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static void validarMinimos(DatosPersona datos) throws IOException {

        if (vacio(datos.primerApellido)
                && vacio(datos.segundoApellido)
                && vacio(datos.nombre)
                && vacio(datos.pasaporte)) {

            throw new IOException(
                    "No se pudieron extraer los datos personales del EX-32."
            );
        }
    }
}
