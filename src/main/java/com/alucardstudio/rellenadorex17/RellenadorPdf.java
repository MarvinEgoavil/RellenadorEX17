package com.alucardstudio.rellenadorex17;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

/**
 * Gestiona la lectura y escritura de los formularios PDF utilizados por
 * Rellenador EX-17.
 *
 * La clase recibe los datos ya extraídos y normalizados en DatosPersona,
 * los escribe sobre una plantilla limpia de EX-17 y completa automáticamente
 * los valores correspondientes a la configuración clásica.
 *
 * @author Marvin Egoavil
 * @version 1.2
 */
public final class RellenadorPdf {

    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    /**
     * Clase de utilidades estáticas. No necesita ser instanciada.
     */
    private RellenadorPdf() {
    }

    // ============================================================
    // SECCIÓN 1 - DATOS DE LA PERSONA EXTRANJERA
    // ============================================================
    // NIE: el EX-17 lo divide en letra inicial, números y letra final.
    private static final String NIE_LETRA_INICIAL = "Texto2";
    private static final String NIE_NUMEROS = "Texto3";
    private static final String NIE_LETRA_FINAL = "Texto4";

    // Datos personales.
    private static final String PASAPORTE = "Texto1";
    private static final String PRIMER_APELLIDO = "Texto5";
    private static final String SEGUNDO_APELLIDO = "Texto6";
    private static final String NOMBRE = "Texto7";

    // Fecha de nacimiento.
    private static final String DIA_NACIMIENTO = "Texto8";
    private static final String MES_NACIMIENTO = "Texto9";
    private static final String ANIO_NACIMIENTO = "Texto10";

    // Lugar de nacimiento y nacionalidad.
    private static final String LUGAR_NACIMIENTO = "Texto11";
    private static final String PAIS_NACIMIENTO = "Texto12";
    private static final String NACIONALIDAD = "Texto13";

    // Datos familiares.
    private static final String NOMBRE_PADRE = "Texto14";
    private static final String NOMBRE_MADRE = "Texto15";

    // Domicilio.
    private static final String DOMICILIO = "Texto16";
    private static final String NUMERO_DOMICILIO = "Texto17";
    private static final String PISO = "Texto18";
    private static final String LOCALIDAD = "Texto19";
    private static final String CODIGO_POSTAL = "Texto20";
    private static final String PROVINCIA = "Texto21";

    // Contacto.
    private static final String TELEFONO = "Texto22";
    private static final String EMAIL = "Texto23";


    // ============================================================
    // SEXO Y ESTADO CIVIL - EX-17 DESTINO
    // ============================================================
    private static final String SEXO_X_DESTINO
            = "Casilla de verificación1";

    private static final String SEXO_H_DESTINO
            = "Casilla de verificación2";

    private static final String SEXO_M_DESTINO
            = "Casilla de verificación3";

    private static final String ESTADO_S_DESTINO
            = "Casilla de verificación4";

    private static final String ESTADO_C_DESTINO
            = "Casilla de verificación5";

    private static final String ESTADO_V_DESTINO
            = "Casilla de verificación6";

    private static final String ESTADO_D_DESTINO
            = "Casilla de verificación7";

    private static final String ESTADO_SP_DESTINO
            = "Casilla de verificación8";

    // ============================================================
    // SECCIÓN 3 - DOMICILIO A EFECTOS DE NOTIFICACIONES
    // ============================================================
    private static final String NOTIF_NOMBRE = "Texto40";
    private static final String NOTIF_NIE = "Texto41";

    private static final String NOTIF_DOMICILIO = "Texto42";
    private static final String NOTIF_NUMERO = "Texto43";
    private static final String NOTIF_PISO = "Texto44";

    private static final String NOTIF_LOCALIDAD = "Texto45";
    private static final String NOTIF_CP = "Texto46";
    private static final String NOTIF_PROVINCIA = "Texto47";

    private static final String NOTIF_TELEFONO = "Texto48";
    private static final String NOTIF_EMAIL = "Texto49";

    // ============================================================
    // LUGAR Y FECHA
    // ============================================================
    private static final String LUGAR_FIRMA = "Texto50";
    private static final String DIA_FIRMA = "Texto51";
    private static final String MES_FIRMA = "Texto52";
    private static final String ANIO_FIRMA = "Texto53";

    // ============================================================
    // CONFIGURACIÓN CLÁSICA
    // ============================================================
    private static final String CHECK_CONSIENTO
            = "Casilla de verificación9";

    private static final String CHECK_TARJETA_INICIAL
            = "Casilla de verificación10";

    private static final String CHECK_RENOVACION
            = "Casilla de verificación11";

    private static final String CHECK_DUPLICADO
            = "Casilla de verificación12";

    // ============================================================
    // GENERACIÓN PRINCIPAL DEL EX-17
    // ============================================================
    /**
     * Rellena automáticamente un EX-17 utilizando los datos del documento
     * origen y el NIE obtenido desde la resolución.
     *
     * En configuración clásica:
     *
     * - copia los campos personales compatibles; - copia sexo y estado civil; -
     * utiliza el NIE obtenido desde la resolución; - rellena la sección 3; -
     * utiliza la localidad de la persona como lugar de firma; - utiliza la fecha actual; - marca
     * consentimiento; - marca Tarjeta inicial; - desmarca Renovación y
     * Duplicado.
     *
     * @param archivoPdf plantilla EX-17 previamente copiada al destino.
     * @param datos datos ya extraídos del documento origen.
     * @param nie NIE obtenido desde la resolución.
     * @throws IOException si ocurre un error al leer o escribir los PDF.
     */
    public static void rellenarNie(
            Path archivoPdf,
            DatosPersona datos,
            String nie) throws IOException {

        validarNie(nie);

        if (datos == null) {
            throw new IllegalArgumentException("Los datos de la persona no pueden ser nulos.");
        }

        String letraInicial = nie.substring(0, 1);
        String numeros = nie.substring(1, 8);
        String letraFinal = nie.substring(8, 9);

        Path archivoTemporal = Files.createTempFile("EX17_TEMP_", ".pdf");

        try {
            try (PDDocument pdfDestino = Loader.loadPDF(archivoPdf.toFile())) {

                PDAcroForm formularioDestino = obtenerFormulario(
                        pdfDestino,
                        "El EX-17 no contiene campos editables."
                );

                // ExtractorDatosPdf ya hizo la lectura del documento origen.
                // RellenadorPdf se limita a escribir los datos en el EX-17.
                rellenarDesdeDatosPersona(formularioDestino, datos, nie);

                escribirCampo(formularioDestino, NIE_LETRA_INICIAL, letraInicial);
                escribirCampo(formularioDestino, NIE_NUMEROS, numeros);
                escribirCampo(formularioDestino, NIE_LETRA_FINAL, letraFinal);

                rellenarLugarYFecha(
                        formularioDestino,
                        valorSeguro(datos.localidad).trim()
                );

                aplicarConfiguracionClasica(formularioDestino);

                pdfDestino.save(archivoTemporal.toFile());
            }

            Files.move(
                    archivoTemporal,
                    archivoPdf,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } finally {
            Files.deleteIfExists(archivoTemporal);
        }
    }

    private static void rellenarDesdeDatosPersona(
            PDAcroForm destino,
            DatosPersona datos,
            String nie) throws IOException {

        // SECCIÓN 1
        escribirCampo(destino, PASAPORTE, datos.pasaporte);

        escribirCampo(
                destino,
                PRIMER_APELLIDO,
                datos.primerApellido
        );

        escribirCampo(
                destino,
                SEGUNDO_APELLIDO,
                datos.segundoApellido
        );

        escribirCampo(destino, NOMBRE, datos.nombre);

        escribirCampo(
                destino,
                DIA_NACIMIENTO,
                datos.diaNacimiento
        );

        escribirCampo(
                destino,
                MES_NACIMIENTO,
                datos.mesNacimiento
        );

        escribirCampo(
                destino,
                ANIO_NACIMIENTO,
                datos.anioNacimiento
        );

        escribirCampo(
                destino,
                LUGAR_NACIMIENTO,
                datos.lugarNacimiento
        );

        escribirCampo(
                destino,
                PAIS_NACIMIENTO,
                datos.paisNacimiento
        );

        escribirCampo(
                destino,
                NACIONALIDAD,
                datos.nacionalidad
        );

        escribirCampo(
                destino,
                NOMBRE_PADRE,
                datos.nombrePadre
        );

        escribirCampo(
                destino,
                NOMBRE_MADRE,
                datos.nombreMadre
        );

        escribirCampo(
                destino,
                DOMICILIO,
                datos.domicilio
        );

        escribirCampo(
                destino,
                NUMERO_DOMICILIO,
                datos.numeroDomicilio
        );

        escribirCampo(
                destino,
                PISO,
                datos.piso
        );

        escribirCampo(
                destino,
                LOCALIDAD,
                datos.localidad
        );

        escribirCampo(
                destino,
                CODIGO_POSTAL,
                datos.codigoPostal
        );

        escribirCampo(
                destino,
                PROVINCIA,
                datos.provincia
        );

        escribirCampo(
                destino,
                TELEFONO,
                datos.telefono
        );

        escribirCampo(
                destino,
                EMAIL,
                datos.email
        );

        // SEXO Y ESTADO CIVIL
        limpiarSexoYEstadoCivil(destino);

        if ("X".equalsIgnoreCase(datos.sexo)) {
            marcarCasilla(destino, SEXO_X_DESTINO);

        } else if ("H".equalsIgnoreCase(datos.sexo)) {
            marcarCasilla(destino, SEXO_H_DESTINO);

        } else if ("M".equalsIgnoreCase(datos.sexo)) {
            marcarCasilla(destino, SEXO_M_DESTINO);
        }

        if ("S".equalsIgnoreCase(datos.estadoCivil)) {
            marcarCasilla(destino, ESTADO_S_DESTINO);

        } else if ("C".equalsIgnoreCase(datos.estadoCivil)) {
            marcarCasilla(destino, ESTADO_C_DESTINO);

        } else if ("V".equalsIgnoreCase(datos.estadoCivil)) {
            marcarCasilla(destino, ESTADO_V_DESTINO);

        } else if ("D".equalsIgnoreCase(datos.estadoCivil)) {
            marcarCasilla(destino, ESTADO_D_DESTINO);

        } else if ("SP".equalsIgnoreCase(datos.estadoCivil)) {
            marcarCasilla(destino, ESTADO_SP_DESTINO);
        }

        // SECCIÓN 3
        String nombreCompleto
                = (valorSeguro(datos.nombre)
                        + " "
                        + valorSeguro(datos.primerApellido)
                        + " "
                        + valorSeguro(datos.segundoApellido))
                        .trim()
                        .replaceAll("\\s+", " ");

        escribirCampo(
                destino,
                NOTIF_NOMBRE,
                nombreCompleto
        );

        escribirCampo(
                destino,
                NOTIF_NIE,
                nie
        );

        escribirCampo(
                destino,
                NOTIF_DOMICILIO,
                datos.domicilio
        );

        escribirCampo(
                destino,
                NOTIF_NUMERO,
                datos.numeroDomicilio
        );

        escribirCampo(
                destino,
                NOTIF_PISO,
                datos.piso
        );

        escribirCampo(
                destino,
                NOTIF_LOCALIDAD,
                datos.localidad
        );

        escribirCampo(
                destino,
                NOTIF_CP,
                datos.codigoPostal
        );

        escribirCampo(
                destino,
                NOTIF_PROVINCIA,
                datos.provincia
        );

        escribirCampo(
                destino,
                NOTIF_TELEFONO,
                datos.telefono
        );

        escribirCampo(
                destino,
                NOTIF_EMAIL,
                datos.email
        );
    }

    private static String valorSeguro(String valor) {

        return valor == null ? "" : valor;
    }

    // ============================================================
    // VALIDACIONES
    // ============================================================
    /**
     * Comprueba que el NIE recibido sea válido antes de escribirlo en el
     * formulario.
     */
    private static void validarNie(String nie) {

        if (!NieValidator.validar(nie)) {

            throw new IllegalArgumentException(
                    "El NIE no es válido: " + nie
            );
        }
    }

        
    
    /**
     * Obtiene el formulario AcroForm de un documento.
     */
    private static PDAcroForm obtenerFormulario(
            PDDocument documento,
            String mensajeError) throws IOException {

        PDAcroForm formulario
                = documento
                        .getDocumentCatalog()
                        .getAcroForm();

        if (formulario == null) {

            throw new IOException(
                    mensajeError
            );
        }

        return formulario;
    }

    // ============================================================
    // CAMPOS DE TEXTO
    // ============================================================
    /**
     * Escribe un valor en un campo del PDF.
     */
    private static void escribirCampo(
            PDAcroForm formulario,
            String nombreCampo,
            String valor) throws IOException {

        PDField campo
                = formulario.getField(nombreCampo);

        if (campo == null) {

            throw new IOException(
                    "No existe el campo PDF: "
                    + nombreCampo
            );
        }

        campo.setValue(
                valor == null ? "" : valor
        );
    }
    
    // ============================================================
    // SEXO Y ESTADO CIVIL
    // ============================================================
    
    /**
     * Desmarca todas las casillas de sexo y estado civil del EX-17 antes de
     * copiar las opciones del documento origen.
     */
    private static void limpiarSexoYEstadoCivil(
            PDAcroForm destino) throws IOException {

        desmarcarCasilla(
                destino,
                SEXO_X_DESTINO
        );

        desmarcarCasilla(
                destino,
                SEXO_H_DESTINO
        );

        desmarcarCasilla(
                destino,
                SEXO_M_DESTINO
        );

        desmarcarCasilla(
                destino,
                ESTADO_S_DESTINO
        );

        desmarcarCasilla(
                destino,
                ESTADO_C_DESTINO
        );

        desmarcarCasilla(
                destino,
                ESTADO_V_DESTINO
        );

        desmarcarCasilla(
                destino,
                ESTADO_D_DESTINO
        );

        desmarcarCasilla(
                destino,
                ESTADO_SP_DESTINO
        );
    }

    
    
    /**
     * Marca una casilla del formulario.
     */
    private static void marcarCasilla(
            PDAcroForm formulario,
            String nombreCasilla) throws IOException {

        PDField campo
                = formulario.getField(nombreCasilla);

        if (campo == null) {

            throw new IOException(
                    "No existe la casilla PDF: "
                    + nombreCasilla
            );
        }

        if (!(campo instanceof PDCheckBox casilla)) {

            throw new IOException(
                    "El campo no es una casilla: "
                    + nombreCasilla
            );
        }

        casilla.check();
    }

    /**
     * Desmarca una casilla del formulario.
     */
    private static void desmarcarCasilla(
            PDAcroForm formulario,
            String nombreCasilla) throws IOException {

        PDField campo
                = formulario.getField(nombreCasilla);

        if (campo == null) {

            throw new IOException(
                    "No existe la casilla PDF: "
                    + nombreCasilla
            );
        }

        if (campo instanceof PDCheckBox casilla) {
            casilla.unCheck();
        }
    }
    
    // ============================================================
    // LUGAR Y FECHA
    // ============================================================
    /**
     * Introduce como lugar de firma la localidad de la persona y utiliza
     * la fecha actual en los campos correspondientes.
     */
    private static void rellenarLugarYFecha(
            PDAcroForm formulario,
            String localidad) throws IOException {

        LocalDate hoy
                = LocalDate.now();

        String dia
                = hoy.format(
                        DateTimeFormatter.ofPattern(
                                "dd"
                        )
                );

        String mes
                = hoy.format(
                        DateTimeFormatter.ofPattern(
                                "MMMM",
                                new Locale("es", "ES")
                        )
                );

        String anio
                = hoy.format(
                        DateTimeFormatter.ofPattern(
                                "yyyy"
                        )
                );

        escribirCampo(
                formulario,
                LUGAR_FIRMA,
                valorSeguro(localidad).trim()
        );

        escribirCampo(
                formulario,
                DIA_FIRMA,
                dia
        );

        escribirCampo(
                formulario,
                MES_FIRMA,
                mes
        );

        escribirCampo(
                formulario,
                ANIO_FIRMA,
                anio
        );
    }

    // ============================================================
    // CONFIGURACIÓN CLÁSICA
    // ============================================================
    /**
     * Aplica las opciones predeterminadas de la configuración clásica.
     */
    private static void aplicarConfiguracionClasica(
            PDAcroForm formulario) throws IOException {

        // Consentimiento para comunicaciones electrónicas.
        marcarCasilla(
                formulario,
                CHECK_CONSIENTO
        );

        // Tipo de tarjeta: Inicial.
        marcarCasilla(
                formulario,
                CHECK_TARJETA_INICIAL
        );

        // Las demás opciones permanecen desmarcadas.
        desmarcarCasilla(
                formulario,
                CHECK_RENOVACION
        );

        desmarcarCasilla(
                formulario,
                CHECK_DUPLICADO
        );
    }
}
