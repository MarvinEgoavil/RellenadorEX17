package com.alucardstudio.rellenadorex17.extractor;

import com.alucardstudio.rellenadorex17.DatosPersona;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.pdfbox.text.TextPosition;

/**
 * Extractor independiente para EX-03.
 *
 * Lee los campos personales de la primera página por su zona física.
 * De esta forma las marcas X de Sexo/Estado civil no se mezclan con
 * Nombre, Nacionalidad, Padre o Madre.
 */
public final class ExtractorEx03 {

    private ExtractorEx03() {
    }

    public static DatosPersona extraer(PDDocument documento)
            throws IOException {

        if (documento == null || documento.getNumberOfPages() == 0) {
            throw new IOException("El EX-03 no contiene páginas.");
        }

        PDPage pagina = documento.getPage(0);
        DatosPersona datos = new DatosPersona();

        // ============================================================
        // SECCIÓN 1 - DATOS DE LA PERSONA EXTRANJERA
        // Coordenadas del modelo oficial EX-03 aportado.
        // ============================================================

        datos.pasaporte = leerZona(
                pagina, 118f, 158f, 115f, 22f
        );

        datos.primerApellido = leerZona(
                pagina, 116f, 175f, 220f, 21f
        );

        datos.segundoApellido = leerZona(
                pagina, 365f, 175f, 190f, 21f
        );

        datos.nombre = leerZona(
                pagina, 116f, 192f, 320f, 21f
        );

        datos.diaNacimiento = soloDigitos(leerZona(
                pagina, 148f, 208f, 25f, 21f
        ));

        datos.mesNacimiento = soloDigitos(leerZona(
                pagina, 174f, 208f, 25f, 21f
        ));

        datos.anioNacimiento = soloDigitos(leerZona(
                pagina, 199f, 208f, 38f, 21f
        ));

        datos.lugarNacimiento = leerZona(
                pagina, 250f, 208f, 165f, 21f
        );

        datos.paisNacimiento = leerZona(
                pagina, 423f, 208f, 135f, 21f
        );

        datos.nacionalidad = leerZona(
                pagina, 116f, 224f, 220f, 21f
        );

        /*
         * IMPORTANTE:
         * Padre y madre se leen como campos completos.
         * Ejemplo real comprobado:
         * padre = NERY RAMON
         * madre = ENOIS DEL VALLE
         */
        datos.nombrePadre = leerZona(
                pagina, 128f, 240f, 205f, 22f
        );

        datos.nombreMadre = leerZona(
                pagina, 355f, 240f, 205f, 22f
        );

        datos.domicilio = leerZona(
                pagina, 137f, 256f, 315f, 22f
        );

        datos.numeroDomicilio = leerZona(
                pagina, 462f, 256f, 36f, 22f
        );

        datos.piso = leerZona(
                pagina, 500f, 256f, 58f, 22f
        );

        datos.localidad = leerZona(
                pagina, 112f, 273f, 215f, 22f
        );

        datos.codigoPostal = soloDigitos(leerZona(
                pagina, 335f, 273f, 70f, 22f
        ));

        datos.provincia = leerZona(
                pagina, 430f, 273f, 128f, 22f
        );

        datos.telefono = soloDigitos(leerZona(
                pagina, 128f, 288f, 155f, 22f
        ));

        datos.email = leerZona(
                pagina, 287f, 288f, 270f, 22f
        ).replaceAll("\\s+", "");

        // Las casillas se leen aparte para que sus X nunca entren en texto.
        extraerSexoYEstadoCivil(documento, datos);

        limpiar(datos);
        validar(datos);

        return datos;
    }

    private static String leerZona(
            PDPage pagina,
            float x,
            float y,
            float ancho,
            float alto) throws IOException {

        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);

        Rectangle2D.Float zona =
                new Rectangle2D.Float(x, y, ancho, alto);

        stripper.addRegion("CAMPO", zona);
        stripper.extractRegions(pagina);

        return normalizar(
                stripper.getTextForRegion("CAMPO")
        );
    }

    private static void extraerSexoYEstadoCivil(
            PDDocument documento,
            DatosPersona datos) throws IOException {

        List<TextPosition> posiciones = new ArrayList<>();

        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void processTextPosition(TextPosition text) {
                posiciones.add(text);
                super.processTextPosition(text);
            }
        };

        stripper.setSortByPosition(true);
        stripper.setStartPage(1);
        stripper.setEndPage(1);
        stripper.getText(documento);

        for (TextPosition p : posiciones) {

            String caracter = p.getUnicode();

            if (caracter == null || caracter.isBlank()) {
                continue;
            }

            String c = caracter.trim();

            // En los EX-03 aportados la marca visible es X.
            if (!"X".equalsIgnoreCase(c)
                    && !"✔".equals(c)
                    && !"✓".equals(c)) {
                continue;
            }

            float x = p.getXDirAdj();
            float y = p.getYDirAdj();

            // ========================================================
            // SEXO
            // Comprobado:
            // Frank     -> H (marca aprox. x=479)
            // Neriannys -> M (marca aprox. x=505)
            // ========================================================
            if (y >= 190f && y <= 212f) {

                if (x >= 445f && x < 470f) {
                    datos.sexo = "X";

                } else if (x >= 470f && x < 495f) {
                    datos.sexo = "H";

                } else if (x >= 495f && x <= 520f) {
                    datos.sexo = "M";
                }
            }

            // ========================================================
            // ESTADO CIVIL
            // Neriannys y Frank: S (marca aprox. x=397)
            // ========================================================
            if (y >= 224f && y <= 244f) {

                if (x >= 388f && x < 413f) {
                    datos.estadoCivil = "S";

                } else if (x >= 413f && x < 438f) {
                    datos.estadoCivil = "C";

                } else if (x >= 438f && x < 463f) {
                    datos.estadoCivil = "V";

                } else if (x >= 463f && x < 493f) {
                    datos.estadoCivil = "D";

                } else if (x >= 493f && x <= 530f) {
                    datos.estadoCivil = "Sp";
                }
            }
        }
    }

    private static void limpiar(DatosPersona datos) {

        datos.pasaporte = normalizar(datos.pasaporte);
        datos.primerApellido = normalizar(datos.primerApellido);
        datos.segundoApellido = normalizar(datos.segundoApellido);
        datos.nombre = normalizar(datos.nombre);

        datos.diaNacimiento = normalizar(datos.diaNacimiento);
        datos.mesNacimiento = normalizar(datos.mesNacimiento);
        datos.anioNacimiento = normalizar(datos.anioNacimiento);

        datos.lugarNacimiento = normalizar(datos.lugarNacimiento);
        datos.paisNacimiento = normalizar(datos.paisNacimiento);
        datos.nacionalidad = normalizar(datos.nacionalidad);

        datos.nombrePadre = normalizar(datos.nombrePadre);
        datos.nombreMadre = normalizar(datos.nombreMadre);

        datos.domicilio = normalizar(datos.domicilio);
        datos.numeroDomicilio = normalizar(datos.numeroDomicilio);
        datos.piso = normalizar(datos.piso);

        datos.localidad = normalizar(datos.localidad);
        datos.codigoPostal = normalizar(datos.codigoPostal);
        datos.provincia = normalizar(datos.provincia);

        datos.telefono = normalizar(datos.telefono);
        datos.email = normalizar(datos.email);

        /*
         * Seguridad adicional: las marcas de las casillas nunca deben
         * terminar almacenadas en un campo textual.
         */
        datos.nombre = quitarMarcaFinal(datos.nombre);
        datos.nacionalidad = quitarMarcaFinal(datos.nacionalidad);
        datos.nombrePadre = quitarMarcaFinal(datos.nombrePadre);
        datos.nombreMadre = quitarMarcaFinal(datos.nombreMadre);
    }

    private static String quitarMarcaFinal(String valor) {

        String v = normalizar(valor);

        return v
                .replaceAll("\\s+[✔✓]$", "")
                .trim();
    }

    private static String soloDigitos(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.replaceAll("\\D", "");
    }

    private static String normalizar(String valor) {

        if (valor == null) {
            return "";
        }

        return valor
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static void validar(DatosPersona datos)
            throws IOException {

        if (datos.pasaporte.isBlank()
                || datos.primerApellido.isBlank()
                || datos.nombre.isBlank()
                || datos.diaNacimiento.isBlank()
                || datos.mesNacimiento.isBlank()
                || datos.anioNacimiento.isBlank()) {

            throw new IOException(
                    "No se pudieron leer correctamente los datos "
                    + "de la sección 1 del EX-03."
            );
        }
    }
}
