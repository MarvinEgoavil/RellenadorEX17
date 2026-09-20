package com.alucardstudio.rellenadorex17.extractor;

import com.alucardstudio.rellenadorex17.DatosPersona;
import java.io.IOException;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;

/**
 * Extractor independiente para formularios EX-31.
 *
 * Estrategia:
 * 1. AcroForm cuando el PDF conserva campos editables.
 * 2. Texto de la primera página para formularios aplanados.
 * 3. Detección posicional de sexo y estado civil como complemento.
 */
public final class ExtractorEx31 {

    private static final Pattern NIE =
            Pattern.compile("(?i)\\b[XYZ]\\s*(?:\\d\\s*){7}[A-Z]\\b");

    private static final Pattern EMAIL =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    private static final Pattern TELEFONO =
            Pattern.compile("\\b[6789]\\d{8}\\b");

    private ExtractorEx31() {
    }

    public static DatosPersona extraer(PDDocument documento)
            throws IOException {

        DatosPersona datos = extraerDesdeAcroForm(documento);

        if (datos == null) {
            datos = extraerDesdeTexto(documento);
        }

        // Complementa PDFs aplanados y aquellos cuyos checks no se
        // exponen claramente como campos AcroForm.
        detectarSexoYEstadoCivil(documento, datos);

        validar(datos);
        return datos;
    }

    private static DatosPersona extraerDesdeAcroForm(PDDocument documento) {

        PDAcroForm formulario = documento.getDocumentCatalog().getAcroForm();

        if (formulario == null) {
            return null;
        }

        String pasaporte = valorCampo(formulario, "Texto1");
        String apellido1 = valorCampo(formulario, "Texto5");
        String nombre = valorCampo(formulario, "Texto7");

        if (pasaporte.isBlank() || apellido1.isBlank() || nombre.isBlank()) {
            return null;
        }

        DatosPersona datos = new DatosPersona();

        datos.pasaporte = pasaporte;
        datos.primerApellido = apellido1;
        datos.segundoApellido = valorCampo(formulario, "Texto6");
        datos.nombre = nombre;

        datos.diaNacimiento = valorCampo(formulario, "Texto8");
        datos.mesNacimiento = valorCampo(formulario, "Texto9");
        datos.anioNacimiento = valorCampo(formulario, "Texto10");

        datos.lugarNacimiento = valorCampo(formulario, "Texto11");
        datos.paisNacimiento = valorCampo(formulario, "Texto12");
        datos.nacionalidad = valorCampo(formulario, "Texto13");

        // En el EX-31 oficial los campos Texto14/Texto15 corresponden
        // a padre y madre cuando están presentes.
        datos.nombrePadre = valorCampo(formulario, "Texto14");
        datos.nombreMadre = valorCampo(formulario, "Texto15");

        datos.domicilio = valorCampo(formulario, "Texto16");
        datos.numeroDomicilio = valorCampo(formulario, "Texto17");
        datos.piso = valorCampo(formulario, "Texto18");

        datos.localidad = valorCampo(formulario, "Texto19");
        datos.codigoPostal = valorCampo(formulario, "Texto20");
        datos.provincia = valorCampo(formulario, "Texto21");
        datos.telefono = valorCampo(formulario, "Texto22");
        datos.email = valorCampo(formulario, "Texto23");

        return datos;
    }

    private static String valorCampo(PDAcroForm formulario, String nombre) {

        PDField campo = formulario.getField(nombre);

        if (campo == null) {
            return "";
        }

        return normalizar(campo.getValueAsString());
    }

    private static DatosPersona extraerDesdeTexto(PDDocument documento)
            throws IOException {

        if (documento.getNumberOfPages() == 0) {
            throw new IOException("El EX-31 no contiene páginas.");
        }

        /*
         * Los EX-31 aplanados conservan el texto, pero PDFTextStripper
         * mezcla primero las etiquetas del formulario y después los valores.
         * Por eso no se deben interpretar por líneas.
         *
         * Leemos directamente las zonas físicas de la sección 1 de la
         * primera página. Las coordenadas corresponden al modelo oficial
         * EX-31 (A4) y no contienen ningún dato de una persona concreta.
         */
        PDFTextStripperByArea area = new PDFTextStripperByArea();
        area.setSortByPosition(true);

        agregarRegion(area, "pasaporte",     102, 140, 198, 22);
        agregarRegion(area, "apellido1",      98, 158, 240, 23);
        agregarRegion(area, "apellido2",     392, 158, 170, 23);
        agregarRegion(area, "nombre",         88, 178, 245, 22);

        agregarRegion(area, "dia",           134, 198,  25, 20);
        agregarRegion(area, "mes",           166, 198,  25, 20);
        agregarRegion(area, "anio",          192, 198,  38, 20);
        agregarRegion(area, "lugar",         256, 197, 170, 22);
        agregarRegion(area, "pais",          452, 197, 110, 22);

        agregarRegion(area, "nacionalidad",  102, 216, 220, 22);
        agregarRegion(area, "padre",         120, 234, 175, 22);
        agregarRegion(area, "madre",         377, 234, 185, 22);

        agregarRegion(area, "domicilio",     127, 252, 345, 22);
        agregarRegion(area, "numero",        496, 252,  27, 22);
        agregarRegion(area, "piso",          541, 252,  25, 22);

        agregarRegion(area, "localidad",     100, 270, 225, 22);
        agregarRegion(area, "cp",            353, 270,  66, 22);
        agregarRegion(area, "provincia",     471, 270,  92, 22);

        agregarRegion(area, "telefono",      120, 287, 145, 22);
        agregarRegion(area, "email",         300, 287, 260, 22);

        area.extractRegions(documento.getPage(0));

        DatosPersona datos = new DatosPersona();

        datos.pasaporte = limpiarCompacto(area.getTextForRegion("pasaporte"));
        datos.primerApellido = limpiarTexto(area.getTextForRegion("apellido1"));
        datos.segundoApellido = limpiarTexto(area.getTextForRegion("apellido2"));
        datos.nombre = limpiarTexto(area.getTextForRegion("nombre"));

        datos.diaNacimiento = limpiarCompacto(area.getTextForRegion("dia"));
        datos.mesNacimiento = limpiarCompacto(area.getTextForRegion("mes"));
        datos.anioNacimiento = limpiarCompacto(area.getTextForRegion("anio"));

        datos.lugarNacimiento = limpiarTexto(area.getTextForRegion("lugar"));
        datos.paisNacimiento = limpiarTexto(area.getTextForRegion("pais"));
        datos.nacionalidad = limpiarTexto(area.getTextForRegion("nacionalidad"));

        datos.nombrePadre = limpiarTexto(area.getTextForRegion("padre"));
        datos.nombreMadre = limpiarTexto(area.getTextForRegion("madre"));

        datos.domicilio = limpiarTexto(area.getTextForRegion("domicilio"));
        datos.numeroDomicilio = limpiarCompacto(area.getTextForRegion("numero"));
        datos.piso = limpiarCompacto(area.getTextForRegion("piso"));

        datos.localidad = limpiarTexto(area.getTextForRegion("localidad"));
        datos.codigoPostal = limpiarCompacto(area.getTextForRegion("cp"));
        datos.provincia = limpiarTexto(area.getTextForRegion("provincia"));

        datos.telefono = limpiarCompacto(area.getTextForRegion("telefono"));
        datos.email = limpiarCompacto(area.getTextForRegion("email"));

        if (datos.pasaporte.isBlank()
                || datos.primerApellido.isBlank()
                || datos.nombre.isBlank()) {

            throw new IOException(
                    "No se pudieron localizar los datos personales del EX-31."
            );
        }

        return datos;
    }

    private static void agregarRegion(
            PDFTextStripperByArea area,
            String nombre,
            float x,
            float y,
            float ancho,
            float alto) {

        area.addRegion(
                nombre,
                new Rectangle2D.Float(x, y, ancho, alto)
        );
    }

    private static String limpiarRegion(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String limpiarCompacto(String texto) {
        return limpiarRegion(texto).replaceAll("\\s+", "");
    }

    private static String limpiarTexto(String texto) {

        String valor = limpiarRegion(texto);

        /*
         * Repara fragmentaciones artificiales introducidas por PDFBox
         * dentro de una misma palabra.
         *
         * Ejemplos:
         * G UTIERREZ -> GUTIERREZ
         * J UAN      -> JUAN
         * M ADRID    -> MADRID
         * NI LDA     -> NILDA
         *
         * No afecta a separaciones normales como:
         * JUAN ALFONZO
         * CALLE HIGUERAS
         */
        String[] partes = valor.split("\\s+");

        if (partes.length == 2
                && partes[0].matches("[\\p{L}]{1,2}")
                && partes[1].matches("[\\p{L}]{3,}")) {
            return partes[0] + partes[1];
        }

        String anterior;
        do {
            anterior = valor;
            valor = valor.replaceAll(
                    "(^|\\s)([\\p{L}])\\s+([\\p{L}]{2,})(?=\\s|$)",
                    "$1$2$3"
            );
        } while (!valor.equals(anterior));

        return valor;
    }

    private static int localizarLineaNie(String[] lineas) {

        for (int i = 0; i < lineas.length; i++) {

            String linea = normalizar(lineas[i]).toUpperCase();

            if (NIE.matcher(linea).find()) {
                return i;
            }

            // Algunos EX-31 aplanados separan el NIE en varios fragmentos.
            // Comprobamos también una versión compacta, sin espacios.
            String compacta = linea.replaceAll("\\s+", "");

            if (compacta.matches(".*[XYZ]\\d{7}[A-Z].*")) {
                return i;
            }
        }

        return -1;
    }

    private static String obtenerLinea(String[] lineas, int posicion)
            throws IOException {

        if (posicion < 0 || posicion >= lineas.length) {
            throw new IOException(
                    "El EX-31 no contiene todos los datos personales esperados."
            );
        }

        return normalizar(lineas[posicion]);
    }

    private static void separarDomicilio(
            String linea,
            DatosPersona datos) {

        String[] partes = normalizar(linea).split("\\s+");

        if (partes.length == 0) {
            return;
        }

        int posicionNumero = -1;

        for (int i = 1; i < partes.length; i++) {
            if (partes[i].matches("\\d+[A-Za-z]?")) {
                posicionNumero = i;
                break;
            }
        }

        if (posicionNumero < 0) {
            datos.domicilio = normalizar(linea);
            return;
        }

        datos.domicilio = unir(partes, 0, posicionNumero);
        datos.numeroDomicilio = partes[posicionNumero];

        if (posicionNumero + 1 < partes.length) {
            datos.piso = unir(partes, posicionNumero + 1, partes.length);
        }
    }

    private static void extraerLocalidadCpProvincia(
            String linea,
            DatosPersona datos) {

        Matcher m = Pattern.compile(
                "(.+?)\\s+(\\d{5})\\s+(.+)"
        ).matcher(normalizar(linea));

        if (m.matches()) {
            datos.localidad = m.group(1).trim();
            datos.codigoPostal = m.group(2).trim();
            datos.provincia = m.group(3).trim();
        }
    }

    private static void extraerTelefonoEmail(
            String linea,
            DatosPersona datos) {

        Matcher telefono = TELEFONO.matcher(linea);
        Matcher email = EMAIL.matcher(linea);

        if (telefono.find()) {
            datos.telefono = telefono.group();
        }

        if (email.find()) {
            datos.email = email.group();
        }
    }

    private static void detectarSexoYEstadoCivil(
            PDDocument documento,
            DatosPersona datos)
            throws IOException {

        List<MarcaPdf> marcas = new ArrayList<>();

        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void processTextPosition(TextPosition text) {

                String caracter = text.getUnicode();

                if ("?".equals(caracter)
                        || "✔".equals(caracter)
                        || "✓".equals(caracter)) {

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

            if (cerca(y, 193.56f, 5f)) {

                if (cerca(x, 470f, 15f)) {
                    datos.sexo = "X";
                } else if (cerca(x, 508.15f, 15f)) {
                    datos.sexo = "H";
                } else if (cerca(x, 540f, 15f)) {
                    datos.sexo = "M";
                }
            }

            if (cerca(y, 230.64f, 5f)) {

                if (cerca(x, 415.76f, 12f)) {
                    datos.estadoCivil = "S";
                } else if (cerca(x, 445f, 12f)) {
                    datos.estadoCivil = "C";
                } else if (cerca(x, 475f, 12f)) {
                    datos.estadoCivil = "V";
                } else if (cerca(x, 505f, 12f)) {
                    datos.estadoCivil = "D";
                } else if (cerca(x, 545f, 15f)) {
                    datos.estadoCivil = "SP";
                }
            }
        }
    }

    private static boolean cerca(float valor, float objetivo, float tolerancia) {
        return Math.abs(valor - objetivo) <= tolerancia;
    }

    private static String unir(String[] partes, int inicio, int fin) {

        StringBuilder resultado = new StringBuilder();

        for (int i = inicio; i < fin; i++) {
            if (resultado.length() > 0) {
                resultado.append(" ");
            }
            resultado.append(partes[i]);
        }

        return resultado.toString().trim();
    }

    private static String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace('\u00A0', ' ')
                .replace("✔", " ")
                .replace("✓", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static void validar(DatosPersona datos)
            throws IOException {

        if (datos.pasaporte == null || datos.pasaporte.isBlank()
                || datos.primerApellido == null || datos.primerApellido.isBlank()
                || datos.nombre == null || datos.nombre.isBlank()) {

            throw new IOException(
                    "No se pudieron leer correctamente los datos personales del EX-31."
            );
        }
    }

    private static final class MarcaPdf {

        final float x;
        final float y;

        MarcaPdf(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
