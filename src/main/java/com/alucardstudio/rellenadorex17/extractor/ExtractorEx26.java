package com.alucardstudio.rellenadorex17.extractor;

import com.alucardstudio.rellenadorex17.DatosPersona;
import com.alucardstudio.rellenadorex17.extractor.pdf.PaginaPdf;
import com.alucardstudio.rellenadorex17.extractor.pdf.PaginaPdf;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;

public final class ExtractorEx26 {

    private ExtractorEx26() {
    }

    public static DatosPersona extraer(PDDocument documento)
            throws IOException {

        PaginaPdf pagina = PaginaPdf.cargar(documento, 1);
        DatosPersona datos = new DatosPersona();

        datos.pasaporte = soloNumeros(
                pagina.textoEn(96f, 151f, 205f, 20f));

        datos.primerApellido =
                pagina.textoEn(91f, 170f, 245f, 21f);

        datos.segundoApellido =
                pagina.textoEn(382f, 170f, 170f, 21f);

        datos.nombre =
                pagina.textoEn(82f, 189f, 255f, 22f);

        datos.diaNacimiento = soloNumeros(
                pagina.textoEn(126f, 208f, 23f, 19f));

        datos.mesNacimiento = soloNumeros(
                pagina.textoEn(158f, 208f, 23f, 19f));

        datos.anioNacimiento = soloNumeros(
                pagina.textoEn(185f, 208f, 35f, 19f));

        datos.lugarNacimiento =
                pagina.textoEn(249f, 208f, 170f, 19f);

        datos.paisNacimiento =
                pagina.textoEn(445f, 208f, 112f, 19f);

        datos.nacionalidad =
                pagina.textoEn(95f, 226f, 225f, 20f);

        datos.nombrePadre =
                pagina.textoEn(110f, 244f, 180f, 20f);

        datos.nombreMadre =
                pagina.textoEn(367f, 244f, 185f, 20f);

        datos.domicilio =
                pagina.textoEn(119f, 262f, 350f, 20f);

        datos.numeroDomicilio = soloNumeros(
                pagina.textoEn(487f, 262f, 27f, 20f));

        datos.piso =
                pagina.textoEn(535f, 262f, 25f, 20f);

        datos.localidad =
                pagina.textoEn(84f, 280f, 238f, 20f);

        datos.codigoPostal = soloNumeros(
                pagina.textoEn(344f, 280f, 70f, 20f));

        datos.provincia =
                pagina.textoEn(452f, 280f, 105f, 20f);

        datos.telefono = soloNumeros(
                pagina.textoEn(100f, 298f, 160f, 20f));

        datos.email =
                pagina.textoEn(294f, 298f, 260f, 20f)
                        .replaceAll("\\s+", "");

        validar(datos);

        return datos;
    }

    private static String soloNumeros(String valor) {

        if (valor == null) {
            return "";
        }

        return valor.replaceAll("\\D", "");
    }

    private static void validar(DatosPersona datos)
            throws IOException {

        if (vacio(datos.pasaporte)) {
            throw new IOException(
                    "EX-26: no se pudo extraer el pasaporte.");
        }

        if (vacio(datos.primerApellido)) {
            throw new IOException(
                    "EX-26: no se pudo extraer el primer apellido.");
        }

        if (vacio(datos.nombre)) {
            throw new IOException(
                    "EX-26: no se pudo extraer el nombre.");
        }

        if (vacio(datos.diaNacimiento)
                || vacio(datos.mesNacimiento)
                || vacio(datos.anioNacimiento)) {

            throw new IOException(
                    "EX-26: no se pudo extraer correctamente "
                    + "la fecha de nacimiento.");
        }
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}