package com.alucardstudio.rellenadorex17;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

/**
 * Controlador principal de Rellenador EX-17.
 *
 * Gestiona la selección de documentos, extracción y validación del NIE,
 * generación del nuevo EX-17 y navegación hacia las demás pantallas
 * de la aplicación.
 *
 * @author Marvin Egoavil
 * @version 1.0
 */
public class PrimaryController {

    // ============================================================
    // CONTROLES DE LA INTERFAZ
    // ============================================================

    @FXML
    private Label lblEx17;

    @FXML
    private Label lblResolucion;

    @FXML
    private Label lblNie;

    @FXML
    private Button btnSubirEx17;

    @FXML
    private Button btnSubirResolucion;

    @FXML
    private Button btnContinuar;

    @FXML
    private Button btnNuevoEx17;


    // ============================================================
    // DATOS DEL EXPEDIENTE ACTUAL
    // ============================================================

    private File archivoEx17;
    private File archivoResolucion;
    private String nieValidado;


    // ============================================================
    // PREFERENCIAS DE LA APLICACIÓN
    // ============================================================

    /**
     * Permite recordar pequeñas preferencias entre ejecuciones.
     *
     * Actualmente se utiliza para guardar la última carpeta desde
     * la que el usuario seleccionó un documento.
     */
    private final Preferences preferencias =
            Preferences.userNodeForPackage(PrimaryController.class);

    private static final String PREF_ULTIMA_CARPETA =
            "ultimaCarpetaDocumentos";


    // ============================================================
    // SELECCIÓN DEL DOCUMENTO ORIGEN
    // ============================================================

    /**
     * Permite seleccionar el documento del que se copiarán los datos.
     *
     * Mientras el selector está abierto se deshabilita temporalmente
     * el botón para impedir que se abran varios FileChooser.
     */
    @FXML
    private void seleccionarEx17() {

        btnSubirEx17.setDisable(true);

        try {

            FileChooser fileChooser = crearSelectorPdf(
                    "Seleccionar EX-17 lleno"
            );

            aplicarUltimaCarpeta(fileChooser);

            File archivoSeleccionado =
                    fileChooser.showOpenDialog(null);

            if (archivoSeleccionado != null) {

                archivoEx17 = archivoSeleccionado;

                lblEx17.setText(
                        "✓ " + archivoEx17.getName()
                );

                guardarUltimaCarpeta(archivoEx17);
            }

            comprobarArchivos();

        } finally {

            // El botón siempre vuelve a habilitarse cuando
            // se cierra el selector, incluso si se pulsa Cancelar.
            btnSubirEx17.setDisable(false);
        }
    }


    // ============================================================
    // SELECCIÓN DE LA RESOLUCIÓN
    // ============================================================

    /**
     * Selecciona la resolución de concesión y extrae automáticamente
     * el NIE contenido en ella.
     */
    @FXML
    private void seleccionarResolucion() {

        btnSubirResolucion.setDisable(true);

        try {

            FileChooser fileChooser = crearSelectorPdf(
                    "Seleccionar resolución de concesión"
            );

            aplicarUltimaCarpeta(fileChooser);

            File archivoSeleccionado =
                    fileChooser.showOpenDialog(null);

            if (archivoSeleccionado != null) {

                archivoResolucion = archivoSeleccionado;

                lblResolucion.setText(
                        "✓ " + archivoResolucion.getName()
                );

                guardarUltimaCarpeta(archivoResolucion);

                extraerYValidarNie();
            }

            comprobarArchivos();

        } finally {

            // Evita que el botón quede bloqueado después
            // de cerrar o cancelar el selector.
            btnSubirResolucion.setDisable(false);
        }
    }


    // ============================================================
    // EXTRACCIÓN Y VALIDACIÓN DEL NIE
    // ============================================================

    /**
     * Extrae el NIE de la resolución seleccionada y comprueba
     * que tenga un formato válido.
     */
    private void extraerYValidarNie() {

        nieValidado = null;

        try {

            String nie =
                    NieExtractor.extraerNie(archivoResolucion);

            if (nie == null) {

                mostrarEstadoNie(
                        "⚠ No se encontró ningún NIE",
                        "#d97706"
                );

                return;
            }

            if (!NieValidator.validar(nie)) {

                mostrarEstadoNie(
                        "⚠ NIE detectado pero NO válido: " + nie,
                        "#dc2626"
                );

                return;
            }

            nieValidado = nie;

            mostrarEstadoNie(
                    "✓ NIE detectado y válido: " + nie,
                    "#16a34a"
            );

        } catch (IOException e) {

            mostrarEstadoNie(
                    "⚠ Error al leer la resolución",
                    "#dc2626"
            );

            System.err.println(
                    "ERROR AL LEER LA RESOLUCIÓN:"
            );

            e.printStackTrace();
        }
    }


    /**
     * Actualiza el mensaje que informa del estado del NIE.
     */
    private void mostrarEstadoNie(String mensaje, String color) {

        lblNie.setText(mensaje);

        lblNie.setStyle(
                "-fx-text-fill: " + color + "; "
                + "-fx-font-size: 14px; "
                + "-fx-font-weight: bold;"
        );
    }


    // ============================================================
    // CONTROL DEL BOTÓN CONTINUAR
    // ============================================================

    /**
     * Habilita CONTINUAR únicamente cuando existen los dos
     * documentos necesarios y se ha obtenido un NIE válido.
     */
    private void comprobarArchivos() {

        boolean todoCorrecto =
                archivoEx17 != null
                && archivoResolucion != null
                && nieValidado != null;

        btnContinuar.setDisable(!todoCorrecto);
    }


    // ============================================================
    // GENERACIÓN DEL EX-17
    // ============================================================

    /**
     * Genera el nuevo EX-17 utilizando:
     *
     * - el documento origen seleccionado;
     * - la resolución de concesión;
     * - el NIE previamente validado;
     * - la plantilla interna del EX-17.
     */
    @FXML
    private void continuar() {

        // --------------------------------------------------------
        // 1. Comprobar el documento origen
        // --------------------------------------------------------

        if (!documentoOrigenValido()) {
            return;
        }

        // --------------------------------------------------------
        // 2. Elegir dónde guardar el nuevo EX-17
        // --------------------------------------------------------

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(
                "Guardar nuevo EX-17"
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivo PDF",
                        "*.pdf"
                )
        );

        aplicarUltimaCarpeta(fileChooser);

        String fechaHora =
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd-MM-yyyy_HH-mm-ss"
                        )
                );

        String nombreArchivo =
                "EX17_"
                + nieValidado
                + "_"
                + fechaHora
                + ".pdf";

        fileChooser.setInitialFileName(
                nombreArchivo
        );

        File archivoDestino =
                fileChooser.showSaveDialog(null);

        if (archivoDestino == null) {
            return;
        }

        // --------------------------------------------------------
        // 3. Generar el documento
        // --------------------------------------------------------

        try {

            Path destino =
                    archivoDestino.toPath();

            PlantillaEx17.copiarPlantilla(
                    destino
            );

            RellenadorPdf.rellenarNie(
                    destino,
                    archivoEx17,
                    nieValidado
            );

            guardarUltimaCarpeta(
                    archivoDestino
            );

            System.out.println(
                    "✓ EX-17 generado correctamente:"
            );

            System.out.println(
                    archivoDestino.getAbsolutePath()
            );

            finalizarExpediente();

        } catch (IOException e) {

            System.err.println(
                    "ERROR AL GENERAR EL EX-17:"
            );

            e.printStackTrace();
        }
    }


    /**
     * Comprueba que el documento origen contenga un formulario
     * editable que pueda ser procesado por PDFBox.
     *
     * Esto evita generar un EX-17 vacío cuando el usuario carga
     * un PDF incompatible.
     */
    private boolean documentoOrigenValido() {

        try {

            if (!RellenadorPdf.tieneFormularioEditable(
                    archivoEx17
            )) {

                mostrarEstadoNie(
                        "⚠ El documento origen no contiene campos editables",
                        "#dc2626"
                );

                return false;
            }

            return true;

        } catch (IOException e) {

            mostrarEstadoNie(
                    "⚠ Error al comprobar el documento origen",
                    "#dc2626"
            );

            System.err.println(
                    "ERROR AL COMPROBAR EL DOCUMENTO ORIGEN:"
            );

            e.printStackTrace();

            return false;
        }
    }


    /**
     * Cambia la interfaz al estado de expediente terminado.
     *
     * Una vez generado el PDF se bloquean los documentos actuales
     * para evitar modificaciones accidentales.
     */
    private void finalizarExpediente() {

        // Ocultar CONTINUAR.
        btnContinuar.setVisible(false);
        btnContinuar.setManaged(false);

        // Mostrar NUEVO EX-17.
        btnNuevoEx17.setVisible(true);
        btnNuevoEx17.setManaged(true);

        // Bloquear selección de documentos.
        btnSubirEx17.setDisable(true);
        btnSubirResolucion.setDisable(true);
    }


    // ============================================================
    // NUEVO EXPEDIENTE
    // ============================================================

    /**
     * Limpia los datos del expediente actual y prepara la aplicación
     * para generar un nuevo EX-17.
     */
    private void reiniciarFormulario() {

        archivoEx17 = null;
        archivoResolucion = null;
        nieValidado = null;

        lblEx17.setText(
                "Ningún archivo seleccionado"
        );

        lblResolucion.setText(
                "Ningún archivo seleccionado"
        );

        lblNie.setText("");

        lblNie.setStyle(
                "-fx-font-size: 14px; "
                + "-fx-font-weight: bold;"
        );

        btnContinuar.setDisable(true);
    }


    /**
     * Inicia un nuevo expediente manteniendo las preferencias
     * generales de la aplicación.
     */
    @FXML
    private void nuevoEx17() {

        reiniciarFormulario();

        // Permitir seleccionar nuevos documentos.
        btnSubirEx17.setDisable(false);
        btnSubirResolucion.setDisable(false);

        // Restaurar CONTINUAR.
        btnContinuar.setVisible(true);
        btnContinuar.setManaged(true);
        btnContinuar.setDisable(true);

        // Ocultar NUEVO EX-17.
        btnNuevoEx17.setVisible(false);
        btnNuevoEx17.setManaged(false);
    }


    // ============================================================
    // FILECHOOSER Y PREFERENCIAS
    // ============================================================

    /**
     * Crea un FileChooser configurado para seleccionar archivos PDF.
     */
    private FileChooser crearSelectorPdf(String titulo) {

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(titulo);

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivos PDF",
                        "*.pdf"
                )
        );

        return fileChooser;
    }


    /**
     * Abre el FileChooser en la última carpeta utilizada,
     * siempre que esa carpeta todavía exista.
     */
    private void aplicarUltimaCarpeta(
            FileChooser fileChooser) {

        String rutaGuardada =
                preferencias.get(
                        PREF_ULTIMA_CARPETA,
                        null
                );

        if (rutaGuardada == null) {
            return;
        }

        File carpeta =
                new File(rutaGuardada);

        if (carpeta.exists()
                && carpeta.isDirectory()) {

            fileChooser.setInitialDirectory(
                    carpeta
            );
        }
    }


    /**
     * Guarda la carpeta del archivo utilizado para recuperarla
     * la próxima vez que se abra un FileChooser.
     */
    private void guardarUltimaCarpeta(
            File archivo) {

        if (archivo == null) {
            return;
        }

        File carpeta =
                archivo.getParentFile();

        if (carpeta != null
                && carpeta.exists()
                && carpeta.isDirectory()) {

            preferencias.put(
                    PREF_ULTIMA_CARPETA,
                    carpeta.getAbsolutePath()
            );
        }
    }


    // ============================================================
    // CONFIGURACIÓN AVANZADA
    // ============================================================

    /**
     * Abre la pantalla de Configuración avanzada.
     *
     * Esta funcionalidad continuará desarrollándose
     * en la versión 2.0.
     */
    @FXML
    private void abrirConfiguracionAvanzada() {

        try {

            App.setRoot("secondary");

        } catch (IOException e) {

            System.err.println(
                    "ERROR AL ABRIR CONFIGURACIÓN AVANZADA:"
            );

            e.printStackTrace();
        }
    }


    // ============================================================
    // ACERCA DE
    // ============================================================

    /**
     * Muestra información general, versión y datos de contacto
     * de la aplicación.
     */
    @FXML
    private void abrirAcercaDe() {

        Alert alerta =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alerta.setTitle(
                "Acerca de"
        );

        alerta.setHeaderText(
                "Rellenador EX-17 · Versión 1.0"
        );

        alerta.setContentText(
                "Gestoría Merlino\n\n"
                + "Desarrollado por Marvin Egoavil\n\n"
                + "Correo: marvinegoavilz@gmail.com\n"
                + "Teléfono: 722516228\n\n"
                + "© 2026 Gestoría Merlino"
        );

        alerta.showAndWait();
    }
}