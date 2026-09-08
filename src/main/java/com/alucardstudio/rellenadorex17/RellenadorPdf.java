package com.alucardstudio.rellenadorex17;

import java.io.File;
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
 * Gestiona la lectura y escritura de los formularios PDF utilizados
 * por Rellenador EX-17.
 *
 * La clase copia los datos compatibles de un documento origen
 * (EX-31 / EX-32) hacia una plantilla limpia de EX-17 y completa
 * automáticamente los valores correspondientes a la configuración clásica.
 *
 * @author Marvin Egoavil
 * @version 1.0
 */
public final class RellenadorPdf {

    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    /**
     * Clase de utilidades estáticas.
     * No necesita ser instanciada.
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
    // SEXO - EX-31 ORIGEN
    // ============================================================

    private static final String EX31_SEXO_X =
            "Casilla de verificación187";

    private static final String EX31_SEXO_H =
            "Casilla de verificación141";

    private static final String EX31_SEXO_M =
            "Casilla de verificación142";


    // ============================================================
    // ESTADO CIVIL - EX-31 ORIGEN
    // ============================================================

    private static final String EX31_ESTADO_S =
            "Casilla de verificación143";

    private static final String EX31_ESTADO_C =
            "Casilla de verificación144";

    private static final String EX31_ESTADO_V =
            "Casilla de verificación145";

    private static final String EX31_ESTADO_D =
            "Casilla de verificación146";

    private static final String EX31_ESTADO_SP =
            "Casilla de verificación147";


    // ============================================================
    // SEXO Y ESTADO CIVIL - EX-32 ORIGEN
    // ============================================================

    private static final String EX32_SEXO_X =
            "Casilla de verificación1";

    private static final String EX32_SEXO_H =
            "Casilla de verificación2";

    private static final String EX32_SEXO_M =
            "Casilla de verificación3";

    private static final String EX32_ESTADO_S =
            "Casilla de verificación4";

    private static final String EX32_ESTADO_C =
            "Casilla de verificación5";

    private static final String EX32_ESTADO_V =
            "Casilla de verificación6";

    private static final String EX32_ESTADO_D =
            "Casilla de verificación7";

    private static final String EX32_ESTADO_SP =
            "Casilla de verificación8";


    // ============================================================
    // SEXO Y ESTADO CIVIL - EX-17 DESTINO
    // ============================================================

    private static final String SEXO_X_DESTINO =
            "Casilla de verificación1";

    private static final String SEXO_H_DESTINO =
            "Casilla de verificación2";

    private static final String SEXO_M_DESTINO =
            "Casilla de verificación3";

    private static final String ESTADO_S_DESTINO =
            "Casilla de verificación4";

    private static final String ESTADO_C_DESTINO =
            "Casilla de verificación5";

    private static final String ESTADO_V_DESTINO =
            "Casilla de verificación6";

    private static final String ESTADO_D_DESTINO =
            "Casilla de verificación7";

    private static final String ESTADO_SP_DESTINO =
            "Casilla de verificación8";


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

    private static final String CHECK_CONSIENTO =
            "Casilla de verificación9";

    private static final String CHECK_TARJETA_INICIAL =
            "Casilla de verificación10";

    private static final String CHECK_RENOVACION =
            "Casilla de verificación11";

    private static final String CHECK_DUPLICADO =
            "Casilla de verificación12";


    // ============================================================
    // GENERACIÓN PRINCIPAL DEL EX-17
    // ============================================================

    /**
     * Rellena automáticamente un EX-17 utilizando los datos del
     * documento origen y el NIE obtenido desde la resolución.
     *
     * En configuración clásica:
     *
     * - copia los campos personales compatibles;
     * - copia sexo y estado civil;
     * - utiliza el NIE obtenido desde la resolución;
     * - rellena la sección 3;
     * - establece MADRID como lugar;
     * - utiliza la fecha actual;
     * - marca consentimiento;
     * - marca Tarjeta inicial;
     * - desmarca Renovación y Duplicado.
     *
     * @param archivoPdf plantilla EX-17 previamente copiada al destino.
     * @param documentoOrigen EX-31 / EX-32 utilizado como origen de datos.
     * @param nie NIE obtenido desde la resolución.
     * @throws IOException si ocurre un error al leer o escribir los PDF.
     */
    public static void rellenarNie(
            Path archivoPdf,
            File documentoOrigen,
            String nie) throws IOException {

        validarNie(nie);

        String letraInicial = nie.substring(0, 1);
        String numeros = nie.substring(1, 8);
        String letraFinal = nie.substring(8, 9);

        /*
         * Trabajamos con un archivo temporal para evitar guardar encima
         * del mismo PDF que PDFBox tiene abierto.
         */
        Path archivoTemporal = Files.createTempFile(
                "EX17_TEMP_",
                ".pdf"
        );

        try {

            try (
                    PDDocument pdfOrigen =
                            Loader.loadPDF(documentoOrigen);

                    PDDocument pdfDestino =
                            Loader.loadPDF(archivoPdf.toFile())
            ) {

                PDAcroForm formularioOrigen =
                        obtenerFormulario(
                                pdfOrigen,
                                "El documento origen no contiene campos editables."
                        );

                PDAcroForm formularioDestino =
                        obtenerFormulario(
                                pdfDestino,
                                "El EX-17 no contiene campos editables."
                        );


                // --------------------------------------------------------
                // 1. DATOS PERSONALES
                // --------------------------------------------------------

                copiarCamposComunes(
                        formularioOrigen,
                        formularioDestino
                );


                // --------------------------------------------------------
                // 2. SEXO Y ESTADO CIVIL
                // --------------------------------------------------------

                copiarSexoYEstadoCivil(
                        formularioOrigen,
                        formularioDestino
                );


                // --------------------------------------------------------
                // 3. NIE
                // --------------------------------------------------------

                escribirCampo(
                        formularioDestino,
                        NIE_LETRA_INICIAL,
                        letraInicial
                );

                escribirCampo(
                        formularioDestino,
                        NIE_NUMEROS,
                        numeros
                );

                escribirCampo(
                        formularioDestino,
                        NIE_LETRA_FINAL,
                        letraFinal
                );


                // --------------------------------------------------------
                // 4. SECCIÓN 3
                // --------------------------------------------------------

                rellenarSeccion3(
                        formularioDestino,
                        formularioOrigen,
                        nie
                );


                // --------------------------------------------------------
                // 5. LUGAR Y FECHA
                // --------------------------------------------------------

                rellenarLugarYFecha(
                        formularioDestino
                );


                // --------------------------------------------------------
                // 6. CONFIGURACIÓN CLÁSICA
                // --------------------------------------------------------

                aplicarConfiguracionClasica(
                        formularioDestino
                );


                // --------------------------------------------------------
                // 7. GUARDADO TEMPORAL
                // --------------------------------------------------------

                pdfDestino.save(
                        archivoTemporal.toFile()
                );
            }


            /*
             * Los documentos PDF ya están cerrados.
             * Ahora podemos sustituir de forma segura el archivo final.
             */
            Files.move(
                    archivoTemporal,
                    archivoPdf,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } finally {

            // Evita dejar archivos temporales si ocurre un error.
            Files.deleteIfExists(
                    archivoTemporal
            );
        }
    }


    // ============================================================
    // VALIDACIONES
    // ============================================================

    /**
     * Comprueba que el NIE recibido sea válido antes de escribirlo
     * en el formulario.
     */
    private static void validarNie(String nie) {

        if (!NieValidator.validar(nie)) {

            throw new IllegalArgumentException(
                    "El NIE no es válido: " + nie
            );
        }
    }


    /**
     * Comprueba si un PDF contiene un formulario AcroForm editable.
     *
     * @param archivoPdf archivo que queremos comprobar.
     * @return true si contiene al menos un campo editable.
     * @throws IOException si el PDF no puede ser leído.
     */
    public static boolean tieneFormularioEditable(
            File archivoPdf) throws IOException {

        try (PDDocument documento =
                Loader.loadPDF(archivoPdf)) {

            PDAcroForm formulario =
                    documento
                            .getDocumentCatalog()
                            .getAcroForm();

            return formulario != null
                    && formulario
                            .getFieldTree()
                            .iterator()
                            .hasNext();
        }
    }


    /**
     * Obtiene el formulario AcroForm de un documento.
     */
    private static PDAcroForm obtenerFormulario(
            PDDocument documento,
            String mensajeError) throws IOException {

        PDAcroForm formulario =
                documento
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

        PDField campo =
                formulario.getField(nombreCampo);

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


    /**
     * Obtiene el valor de un campo del formulario.
     */
    private static String obtenerValor(
            PDAcroForm formulario,
            String nombreCampo) throws IOException {

        PDField campo =
                formulario.getField(nombreCampo);

        if (campo == null) {

            throw new IOException(
                    "No existe el campo: "
                    + nombreCampo
            );
        }

        String valor =
                campo.getValueAsString();

        return valor == null
                ? ""
                : valor.trim();
    }


    // ============================================================
    // COPIA DE DATOS PERSONALES
    // ============================================================

    /**
     * Copia los campos de texto compatibles entre el documento origen
     * y el EX-17 de destino.
     *
     * El NIE no se copia porque siempre procede de la resolución.
     *
     * Si uno de los campos no existe en alguno de los dos formularios,
     * simplemente se ignora. Esto permite trabajar con documentos que
     * presentan pequeñas diferencias internas.
     */
    private static void copiarCamposComunes(
            PDAcroForm origen,
            PDAcroForm destino) throws IOException {

        String[] campos = {
            PASAPORTE,
            PRIMER_APELLIDO,
            SEGUNDO_APELLIDO,
            NOMBRE,
            DIA_NACIMIENTO,
            MES_NACIMIENTO,
            ANIO_NACIMIENTO,
            LUGAR_NACIMIENTO,
            PAIS_NACIMIENTO,
            NACIONALIDAD,
            NOMBRE_PADRE,
            NOMBRE_MADRE,
            DOMICILIO,
            NUMERO_DOMICILIO,
            PISO,
            LOCALIDAD,
            CODIGO_POSTAL,
            PROVINCIA,
            TELEFONO,
            EMAIL
        };

        for (String nombreCampo : campos) {

            PDField campoOrigen =
                    origen.getField(nombreCampo);

            PDField campoDestino =
                    destino.getField(nombreCampo);

            /*
             * Algunos modelos pueden no contener exactamente
             * los mismos campos. En ese caso simplemente se omiten.
             */
            if (campoOrigen == null
                    || campoDestino == null) {

                continue;
            }

            String valor =
                    campoOrigen.getValueAsString();

            campoDestino.setValue(
                    valor == null ? "" : valor
            );
        }
    }


    // ============================================================
    // SEXO Y ESTADO CIVIL
    // ============================================================

    /**
     * Detecta si el documento origen utiliza la estructura del EX-31
     * o del EX-32 y copia las casillas correspondientes hacia el EX-17.
     */
    private static void copiarSexoYEstadoCivil(
            PDAcroForm origen,
            PDAcroForm destino) throws IOException {

        limpiarSexoYEstadoCivil(
                destino
        );

        boolean esEx31 =
                origen.getField(
                        EX31_SEXO_X
                ) != null;

        if (esEx31) {

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX31_SEXO_X,
                    SEXO_X_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX31_SEXO_H,
                    SEXO_H_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX31_SEXO_M,
                    SEXO_M_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX31_ESTADO_S,
                    ESTADO_S_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX31_ESTADO_C,
                    ESTADO_C_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX31_ESTADO_V,
                    ESTADO_V_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX31_ESTADO_D,
                    ESTADO_D_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX31_ESTADO_SP,
                    ESTADO_SP_DESTINO
            );

        } else {

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX32_SEXO_X,
                    SEXO_X_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX32_SEXO_H,
                    SEXO_H_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX32_SEXO_M,
                    SEXO_M_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX32_ESTADO_S,
                    ESTADO_S_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX32_ESTADO_C,
                    ESTADO_C_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX32_ESTADO_V,
                    ESTADO_V_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX32_ESTADO_D,
                    ESTADO_D_DESTINO
            );

            copiarCasillaSiMarcada(
                    origen,
                    destino,
                    EX32_ESTADO_SP,
                    ESTADO_SP_DESTINO
            );
        }
    }


    /**
     * Desmarca todas las casillas de sexo y estado civil del EX-17
     * antes de copiar las opciones del documento origen.
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
     * Marca la casilla de destino únicamente si la casilla
     * correspondiente del documento origen está marcada.
     */
    private static void copiarCasillaSiMarcada(
            PDAcroForm origen,
            PDAcroForm destino,
            String casillaOrigen,
            String casillaDestino) throws IOException {

        if (estaMarcadaSeguro(
                origen,
                casillaOrigen
        )) {

            marcarCasilla(
                    destino,
                    casillaDestino
            );
        }
    }


    /**
     * Comprueba de forma segura si una casilla está marcada.
     *
     * Si el campo no existe o no es una casilla devuelve false.
     */
    private static boolean estaMarcadaSeguro(
            PDAcroForm formulario,
            String nombreCasilla) {

        PDField campo =
                formulario.getField(nombreCasilla);

        if (!(campo instanceof PDCheckBox casilla)) {
            return false;
        }

        String valor =
                casilla.getValue();

        return valor != null
                && !valor.equalsIgnoreCase("Off");
    }


    /**
     * Marca una casilla del formulario.
     */
    private static void marcarCasilla(
            PDAcroForm formulario,
            String nombreCasilla) throws IOException {

        PDField campo =
                formulario.getField(nombreCasilla);

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

        PDField campo =
                formulario.getField(nombreCasilla);

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
    // SECCIÓN 3
    // ============================================================

    /**
     * Rellena el domicilio a efectos de notificaciones utilizando
     * los datos disponibles en la sección 1.
     */
    private static void rellenarSeccion3(
            PDAcroForm destino,
            PDAcroForm origen,
            String nie) throws IOException {

        String nombre =
                obtenerValor(
                        origen,
                        NOMBRE
                );

        String apellido1 =
                obtenerValor(
                        origen,
                        PRIMER_APELLIDO
                );

        String apellido2 =
                obtenerValor(
                        origen,
                        SEGUNDO_APELLIDO
                );

        String nombreCompleto =
                (nombre
                + " "
                + apellido1
                + " "
                + apellido2)
                        .trim()
                        .replaceAll("\\s+", " ");


        escribirCampo(
                destino,
                NOTIF_NOMBRE,
                nombreCompleto
        );

        // El NIE siempre procede de la resolución.
        escribirCampo(
                destino,
                NOTIF_NIE,
                nie
        );

        escribirCampo(
                destino,
                NOTIF_DOMICILIO,
                obtenerValor(
                        origen,
                        DOMICILIO
                )
        );

        escribirCampo(
                destino,
                NOTIF_NUMERO,
                obtenerValor(
                        origen,
                        NUMERO_DOMICILIO
                )
        );

        escribirCampo(
                destino,
                NOTIF_PISO,
                obtenerValor(
                        origen,
                        PISO
                )
        );

        escribirCampo(
                destino,
                NOTIF_LOCALIDAD,
                obtenerValor(
                        origen,
                        LOCALIDAD
                )
        );

        escribirCampo(
                destino,
                NOTIF_CP,
                obtenerValor(
                        origen,
                        CODIGO_POSTAL
                )
        );

        escribirCampo(
                destino,
                NOTIF_PROVINCIA,
                obtenerValor(
                        origen,
                        PROVINCIA
                )
        );

        escribirCampo(
                destino,
                NOTIF_TELEFONO,
                obtenerValor(
                        origen,
                        TELEFONO
                )
        );

        escribirCampo(
                destino,
                NOTIF_EMAIL,
                obtenerValor(
                        origen,
                        EMAIL
                )
        );
    }


    // ============================================================
    // LUGAR Y FECHA
    // ============================================================

    /**
     * Introduce MADRID y la fecha actual en los campos correspondientes.
     */
    private static void rellenarLugarYFecha(
            PDAcroForm formulario) throws IOException {

        LocalDate hoy =
                LocalDate.now();

        String dia =
                hoy.format(
                        DateTimeFormatter.ofPattern(
                                "dd"
                        )
                );

        String mes =
                hoy.format(
                        DateTimeFormatter.ofPattern(
                                "MMMM",
                                new Locale("es", "ES")
                        )
                );

        String anio =
                hoy.format(
                        DateTimeFormatter.ofPattern(
                                "yyyy"
                        )
                );


        escribirCampo(
                formulario,
                LUGAR_FIRMA,
                "MADRID"
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


    // ============================================================
    // HERRAMIENTAS DE DIAGNÓSTICO
    // ============================================================

    /**
     * Muestra en consola todos los campos del PDF que contienen
     * algún valor.
     *
     * Método destinado únicamente a diagnóstico y desarrollo.
     * No modifica el documento.
     */
    public static void mostrarCamposRellenos(
            File archivoPdf) throws IOException {

        try (PDDocument documento =
                Loader.loadPDF(archivoPdf)) {

            PDAcroForm formulario =
                    documento
                            .getDocumentCatalog()
                            .getAcroForm();

            if (formulario == null) {

                System.out.println(
                        "El PDF no tiene formulario."
                );

                return;
            }

            System.out.println(
                    "=== CAMPOS CON VALOR ==="
            );

            for (PDField campo :
                    formulario.getFieldTree()) {

                String valor =
                        campo.getValueAsString();

                if (valor != null
                        && !valor.isBlank()
                        && !valor.equalsIgnoreCase("Off")) {

                    System.out.println(
                            campo.getFullyQualifiedName()
                            + " = "
                            + valor
                    );
                }
            }
        }
    }


    /**
     * Muestra en consola todas las casillas existentes en un PDF
     * y su estado actual.
     *
     * Método destinado únicamente a diagnóstico y desarrollo.
     * No modifica el documento.
     */
    public static void mostrarCasillas(
            File archivoPdf) throws IOException {

        try (PDDocument documento =
                Loader.loadPDF(archivoPdf)) {

            PDAcroForm formulario =
                    documento
                            .getDocumentCatalog()
                            .getAcroForm();

            if (formulario == null) {

                System.out.println(
                        "El PDF no tiene formulario."
                );

                return;
            }

            System.out.println(
                    "=== CASILLAS DEL PDF ==="
            );

            for (PDField campo :
                    formulario.getFieldTree()) {

                if (campo instanceof PDCheckBox casilla) {

                    System.out.println(
                            casilla.getFullyQualifiedName()
                            + " = "
                            + casilla.getValue()
                    );
                }
            }
        }
    }
}