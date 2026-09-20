package com.alucardstudio.rellenadorex17.extractor;

import com.alucardstudio.rellenadorex17.DatosPersona;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

public final class ExtractorEx25 {

    private static final Pattern PATRON_FECHA
            = Pattern.compile("\\b(\\d{1,2})\\s+(\\d{1,2})\\s+(\\d{4})\\b");

    private static final Pattern PATRON_EMAIL
            = Pattern.compile(
                    "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
            );

    private static final Pattern PATRON_TELEFONO
            = Pattern.compile("\\b[6789]\\d{8}\\b");

    private static final Pattern PATRON_CP
            = Pattern.compile("\\b\\d{5}\\b");

    private ExtractorEx25() {
    }

    public static DatosPersona extraer(PDDocument documento)
            throws IOException {

        DatosPersona datosAcroForm = extraerDesdeAcroForm(documento);

        if (datosAcroForm != null) {

            extraerSexoYEstadoCivil(
                    documento,
                    datosAcroForm
            );

            validar(datosAcroForm);
            return datosAcroForm;
        }

        // Fallback para EX-25 que no tengan campos AcroForm utilizables.
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(1);
        stripper.setSortByPosition(true);

        String contenido = normalizarEspacios(
                stripper.getText(documento)
        );

        if (contenido.isBlank()) {
            throw new IOException(
                    "El EX-25 no contiene datos personales legibles."
            );
        }

        DatosPersona datos = extraerCompacto(contenido);

        String piso = extraerPisoPorCoordenadas(documento);

        if (!piso.isBlank()) {
            datos.piso = piso;
        }

        extraerSexoYEstadoCivil(
                documento,
                datos
        );

        validar(datos);

        return datos;

    }

    private static DatosPersona extraerDesdeAcroForm(
            PDDocument documento) throws IOException {

        PDAcroForm formulario = documento
                .getDocumentCatalog()
                .getAcroForm();

        if (formulario == null) {
            return null;
        }

        /*
     * Estos nombres identifican los CAMPOS del modelo oficial EX-25,
     * no datos de una persona concreta.
         */
        String pasaporte = valorCampo(formulario, "Texto157");
        String primerApellido = valorCampo(formulario, "Texto161");
        String nombre = valorCampo(formulario, "Texto163");

        /*
     * Si estos campos principales no existen, probablemente estamos
     * ante otra versión del EX-25. Dejamos que actúe el fallback.
         */
        if (pasaporte.isBlank()
                || primerApellido.isBlank()
                || nombre.isBlank()) {

            return null;
        }

        DatosPersona datos = new DatosPersona();

        datos.pasaporte = pasaporte;
        datos.primerApellido = primerApellido;
        datos.segundoApellido = valorCampo(formulario, "Texto162");
        datos.nombre = nombre;

        datos.diaNacimiento = valorCampo(formulario, "Texto164");
        datos.mesNacimiento = valorCampo(formulario, "Texto165");
        datos.anioNacimiento = valorCampo(formulario, "Texto166");

        datos.lugarNacimiento = valorCampo(formulario, "Texto167");
        datos.paisNacimiento = valorCampo(formulario, "Texto168");
        datos.nacionalidad = valorCampo(formulario, "Texto169");

        datos.nombrePadre = valorCampo(formulario, "Texto170");
        datos.nombreMadre = valorCampo(formulario, "Texto171");

        datos.domicilio = valorCampo(formulario, "Texto172");
        datos.numeroDomicilio = valorCampo(formulario, "Texto173");
        datos.piso = valorCampo(formulario, "Texto174");

        datos.localidad = valorCampo(formulario, "Texto175");
        datos.telefono = valorCampo(formulario, "Texto176");
        datos.codigoPostal = valorCampo(formulario, "Texto177");
        datos.provincia = valorCampo(formulario, "Texto178");
        datos.email = valorCampo(formulario, "Texto179");

        return datos;
    }

    private static String valorCampo(
            PDAcroForm formulario,
            String nombreCampo) {

        PDField campo = formulario.getField(nombreCampo);

        if (campo == null) {
            return "";
        }

        return normalizarEspacios(
                campo.getValueAsString()
        );
    }

    private static DatosPersona extraerCompacto(String bloque)
            throws IOException {

        DatosPersona datos = new DatosPersona();
        String contenido = normalizarEspacios(bloque);

        /*
         * El EX-25 puede contener después datos del representante.
         * Cortamos tras el primer email para trabajar únicamente con
         * los datos de la persona extranjera.
         */
        Matcher primerEmail = PATRON_EMAIL.matcher(contenido);

        if (primerEmail.find()) {
            contenido = contenido.substring(
                    0,
                    primerEmail.end()
            ).trim();
        }

        String[] tokens = contenido.split("\\s+");

        if (tokens.length == 0) {
            throw new IOException(
                    "El EX-25 no contiene datos personales legibles."
            );
        }

        datos.pasaporte = tokens[0];

        Matcher fecha = PATRON_FECHA.matcher(contenido);

        if (!fecha.find()) {
            throw new IOException(
                    "No se pudo localizar la fecha de nacimiento del EX-25."
            );
        }

        String antesFecha = contenido.substring(
                0,
                fecha.start()
        ).trim();

        String[] identidad = antesFecha.split("\\s+");

        if (identidad.length >= 2) {
            datos.primerApellido = identidad[1];
        }

        if (identidad.length >= 3) {
            datos.segundoApellido = identidad[2];
        }

        if (identidad.length >= 4) {
            StringBuilder nombre = new StringBuilder();

            for (int i = 3; i < identidad.length; i++) {
                if (nombre.length() > 0) {
                    nombre.append(' ');
                }
                nombre.append(identidad[i]);
            }

            datos.nombre = nombre.toString();
        }

        datos.diaNacimiento = fecha.group(1);
        datos.mesNacimiento = fecha.group(2);
        datos.anioNacimiento = fecha.group(3);

        String despuesFecha = contenido.substring(
                fecha.end()
        ).trim();

        extraerResto(
                despuesFecha,
                datos
        );

        completarContacto(
                contenido,
                datos
        );

        completarLocalidad(
                contenido,
                datos
        );

        return datos;
    }

    private static void extraerResto(
            String texto,
            DatosPersona datos) {

        String contenido = normalizarEspacios(texto);

        Matcher telefonoMatcher = PATRON_TELEFONO.matcher(contenido);
        Matcher cpMatcher = PATRON_CP.matcher(contenido);
        Matcher emailMatcher = PATRON_EMAIL.matcher(contenido);

        if (!telefonoMatcher.find()) {
            return;
        }

        datos.telefono = telefonoMatcher.group();

        int inicioTelefono = telefonoMatcher.start();

        if (cpMatcher.find(telefonoMatcher.end())) {
            datos.codigoPostal = cpMatcher.group();
        }

        if (emailMatcher.find()) {
            datos.email = emailMatcher.group();
        }

        String antesTelefono = contenido
                .substring(0, inicioTelefono)
                .trim();

        String[] partesAntesTelefono = antesTelefono.split("\\s+");

        if (partesAntesTelefono.length < 7) {
            return;
        }

        datos.localidad
                = partesAntesTelefono[partesAntesTelefono.length - 1];

        StringBuilder sinLocalidad = new StringBuilder();

        for (int i = 0;
                i < partesAntesTelefono.length - 1;
                i++) {

            if (sinLocalidad.length() > 0) {
                sinLocalidad.append(' ');
            }

            sinLocalidad.append(partesAntesTelefono[i]);
        }

        /*
         * Conservamos la estructura que ya utilizaba el EX-25 actual:
         * lugar, país, nacionalidad, padre, madre y después domicilio.
         * No contiene ningún valor personal fijo.
         */
        String[] partes = sinLocalidad.toString().split("\\s+");

        if (partes.length < 6) {
            return;
        }

        datos.lugarNacimiento = partes[0];
        datos.paisNacimiento = partes[1];
        datos.nacionalidad = partes[2];
        datos.nombrePadre = partes[3];
        datos.nombreMadre = partes[4];

        StringBuilder domicilioCompleto = new StringBuilder();

        for (int i = 5; i < partes.length; i++) {
            if (domicilioCompleto.length() > 0) {
                domicilioCompleto.append(' ');
            }

            domicilioCompleto.append(partes[i]);
        }

        separarDomicilio(
                domicilioCompleto.toString(),
                datos
        );

        if (datos.codigoPostal != null) {
            int indiceCp = contenido.indexOf(
                    datos.codigoPostal,
                    telefonoMatcher.end()
            );

            if (indiceCp >= 0) {
                String despuesCp = contenido.substring(
                        indiceCp + datos.codigoPostal.length()
                ).trim();

                Matcher emailProvincia
                        = PATRON_EMAIL.matcher(despuesCp);

                if (emailProvincia.find()) {
                    String provincia = despuesCp.substring(
                            0,
                            emailProvincia.start()
                    ).trim();

                    if (!provincia.isBlank()) {
                        datos.provincia = provincia;
                    }
                }
            }
        }
    }

    private static String extraerPisoPorCoordenadas(
            PDDocument documento)
            throws IOException {

        PDFTextStripperByArea stripper
                = new PDFTextStripperByArea();

        stripper.setSortByPosition(true);

        Rectangle2D.Float zonaPiso
                = new Rectangle2D.Float(
                        522f,
                        258f,
                        34f,
                        27f
                );

        stripper.addRegion(
                "PISO_EX25",
                zonaPiso
        );

        stripper.extractRegions(
                documento.getPage(0)
        );

        return normalizarEspacios(
                stripper.getTextForRegion("PISO_EX25")
        );
    }

    private static void completarContacto(
            String texto,
            DatosPersona datos) {

        Matcher email = PATRON_EMAIL.matcher(texto);

        if (email.find()) {
            datos.email = email.group();
        }

        Matcher telefono = PATRON_TELEFONO.matcher(texto);

        if (telefono.find()) {
            datos.telefono = telefono.group();
        }
    }

    private static void completarLocalidad(
            String texto,
            DatosPersona datos) {

        Matcher cp = PATRON_CP.matcher(texto);

        if (!cp.find()) {
            return;
        }

        datos.codigoPostal = cp.group();

        String antes = texto.substring(
                0,
                cp.start()
        ).trim();

        String despues = texto.substring(
                cp.end()
        ).trim();

        String[] partesAntes = antes.split("\\s+");

        if (partesAntes.length > 0
                && datos.localidad == null) {

            datos.localidad
                    = partesAntes[partesAntes.length - 1];
        }

        String[] partesDespues = despues.split("\\s+");

        if (partesDespues.length > 0
                && datos.provincia == null) {

            String posibleProvincia
                    = partesDespues[0];

            if (!PATRON_EMAIL.matcher(
                    posibleProvincia
            ).matches()) {

                datos.provincia
                        = posibleProvincia;
            }
        }
    }

    private static void separarDomicilio(
            String linea,
            DatosPersona datos) {

        if (linea == null || linea.isBlank()) {
            return;
        }

        String[] partes = linea.trim().split("\\s+");

        if (partes.length < 2) {
            datos.domicilio = linea.trim();
            return;
        }

        int posicionNumero = -1;

        for (int i = 1; i < partes.length; i++) {
            if (partes[i].matches("\\d+[A-Za-z]?")) {
                posicionNumero = i;
                break;
            }
        }

        if (posicionNumero == -1) {
            datos.domicilio = linea.trim();
            return;
        }

        StringBuilder calle = new StringBuilder();

        for (int i = 0; i < posicionNumero; i++) {
            if (calle.length() > 0) {
                calle.append(' ');
            }

            calle.append(partes[i]);
        }

        datos.domicilio = calle.toString();
        datos.numeroDomicilio = partes[posicionNumero];

        if (posicionNumero + 1 < partes.length) {
            StringBuilder piso = new StringBuilder();

            for (int i = posicionNumero + 1;
                    i < partes.length;
                    i++) {

                if (piso.length() > 0) {
                    piso.append(' ');
                }

                piso.append(partes[i]);
            }

            datos.piso = piso.toString();
        }
    }

    private static void extraerSexoYEstadoCivil(
            PDDocument documento,
            DatosPersona datos) throws IOException {

        // 1. Intentamos leer las casillas AcroForm del EX-25.
        PDAcroForm formulario = documento
                .getDocumentCatalog()
                .getAcroForm();

        if (formulario != null) {

            if (casillaMarcada(formulario, "Casilla de verificación235")) {
                datos.sexo = "X";
            } else if (casillaMarcada(formulario, "Casilla de verificación236")) {
                datos.sexo = "H";
            } else if (casillaMarcada(formulario, "Casilla de verificación237")) {
                datos.sexo = "M";
            }
        }

        // 2. Fallback para EX-25 aplanados: lectura posicional.
        PDFTextStripper stripper = new PDFTextStripper() {

            @Override
            protected void processTextPosition(
                    org.apache.pdfbox.text.TextPosition text) {

                String valor = text.getUnicode();

                if (valor == null || valor.isBlank()) {
                    super.processTextPosition(text);
                    return;
                }

                float x = text.getXDirAdj();
                float y = text.getYDirAdj();

                // SEXO: X / H / M
                if (y >= 190 && y <= 212) {

                    if (x >= 455 && x < 480) {
                        datos.sexo = "X";
                    } else if (x >= 480 && x < 510) {
                        datos.sexo = "H";
                    } else if (x >= 510 && x <= 540) {
                        datos.sexo = "M";
                    }
                }

                // ESTADO CIVIL: S / C / V / D / Sp
                if (y >= 235 && y <= 265) {

                    if (x >= 455 && x < 480) {
                        datos.estadoCivil = "S";
                    } else if (x >= 480 && x < 510) {
                        datos.estadoCivil = "C";
                    } else if (x >= 510 && x < 540) {
                        datos.estadoCivil = "V";
                    } else if (x >= 540 && x < 570) {
                        datos.estadoCivil = "D";
                    } else if (x >= 570 && x <= 610) {
                        datos.estadoCivil = "Sp";
                    }
                }

                super.processTextPosition(text);
            }
        };

        stripper.setSortByPosition(true);
        stripper.setStartPage(1);
        stripper.setEndPage(1);
        stripper.getText(documento);
    }

    private static boolean casillaMarcada(
            PDAcroForm formulario,
            String nombreCampo) {

        PDField campo = formulario.getField(nombreCampo);

        if (campo == null) {
            return false;
        }

        String valor = campo.getValueAsString();

        return valor != null
                && !valor.isBlank()
                && !"Off".equalsIgnoreCase(valor);
    }

    private static void validar(DatosPersona datos)
            throws IOException {

        if (vacio(datos.pasaporte)) {
            throw new IOException(
                    "EX-25: no se pudo extraer el pasaporte."
            );
        }

        if (vacio(datos.primerApellido)) {
            throw new IOException(
                    "EX-25: no se pudo extraer el primer apellido."
            );
        }

        if (vacio(datos.nombre)) {
            throw new IOException(
                    "EX-25: no se pudo extraer el nombre."
            );
        }

        if (vacio(datos.diaNacimiento)
                || vacio(datos.mesNacimiento)
                || vacio(datos.anioNacimiento)) {

            throw new IOException(
                    "EX-25: no se pudo extraer correctamente "
                    + "la fecha de nacimiento."
            );
        }
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String normalizarEspacios(String texto) {
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
}
