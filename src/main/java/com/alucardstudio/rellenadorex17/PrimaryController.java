package com.alucardstudio.rellenadorex17;

import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
/**
 * Controlador principal de Rellenador EX-17.
 *
 * Gestiona la selección de documentos, extracción y validación del NIE,
 * generación del nuevo EX-17 y navegación hacia las demás pantallas de la
 * aplicación.
 *
 * @author Marvin Egoavil
 * @version 1.2
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
    private Hyperlink linkVerError;

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
    private Path ultimoLogError;

    // ============================================================
    // PREFERENCIAS DE LA APLICACIÓN
    // ============================================================
    /**
     * Permite recordar pequeñas preferencias entre ejecuciones.
     *
     * Actualmente se utiliza para guardar la última carpeta desde la que el
     * usuario seleccionó un documento.
     */
    private final Preferences preferencias
            = Preferences.userNodeForPackage(PrimaryController.class);

    private static final String PREF_ULTIMA_CARPETA
            = "ultimaCarpetaDocumentos";

    
    // ============================================================
// DRAG & DROP
// ============================================================

/**
 * Configura la carga de documentos mediante arrastrar y soltar.
 */
@FXML
private void initialize() {

    configurarDragAndDropEx17();
    configurarDragAndDropResolucion();
}

/**
 * Permite arrastrar un EX-31 / EX-32 sobre su botón.
 */
private void configurarDragAndDropEx17() {

    btnSubirEx17.setOnDragOver(event -> {

        if (contienePdf(event)) {
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.consume();
    });

    btnSubirEx17.setOnDragDropped(event -> {

        File archivo = obtenerPdf(event);

        if (archivo != null) {

            archivoEx17 = archivo;

            lblEx17.setText(
                    "✓ " + archivoEx17.getName()
            );
            lblEx17.setStyle(
                    "-fx-text-fill: #60A5FA; "
                    + "-fx-font-weight: bold;"
            );

            guardarUltimaCarpeta(archivoEx17);

            comprobarArchivos();
        }

        event.setDropCompleted(archivo != null);
        event.consume();
    });
}

/**
 * Permite arrastrar una resolución sobre su botón.
 */
private void configurarDragAndDropResolucion() {

    btnSubirResolucion.setOnDragOver(event -> {

        if (contienePdf(event)) {
            event.acceptTransferModes(TransferMode.COPY);
        }

        event.consume();
    });

    btnSubirResolucion.setOnDragDropped(event -> {

        File archivo = obtenerPdf(event);

        if (archivo != null) {

            archivoResolucion = archivo;

            lblResolucion.setText(
                    "✓ " + archivoResolucion.getName()
            );
            lblResolucion.setStyle(
                    "-fx-text-fill: #60A5FA; "
                    + "-fx-font-weight: bold;"
            );

            guardarUltimaCarpeta(archivoResolucion);

            extraerYValidarNie();
            comprobarArchivos();
        }

        event.setDropCompleted(archivo != null);
        event.consume();
    });
}

/**
 * Comprueba que el elemento arrastrado contenga al menos un PDF.
 */
private boolean contienePdf(DragEvent event) {

    Dragboard dragboard = event.getDragboard();

    return dragboard.hasFiles()
            && !dragboard.getFiles().isEmpty()
            && esPdf(dragboard.getFiles().get(0));
}

/**
 * Obtiene el primer PDF arrastrado.
 */
private File obtenerPdf(DragEvent event) {

    Dragboard dragboard = event.getDragboard();

    if (!dragboard.hasFiles()
            || dragboard.getFiles().isEmpty()) {

        return null;
    }

    File archivo = dragboard.getFiles().get(0);

    if (!esPdf(archivo)) {
        return null;
    }

    return archivo;
}

/**
 * Comprueba que el archivo sea un PDF.
 */
private boolean esPdf(File archivo) {

    return archivo != null
            && archivo.isFile()
            && archivo.getName()
                    .toLowerCase()
                    .endsWith(".pdf");
}
    
    // ============================================================
    // SELECCIÓN DEL DOCUMENTO ORIGEN
    // ============================================================
    /**
     * Permite seleccionar el documento del que se copiarán los datos.
     *
     * Mientras el selector está abierto se deshabilita temporalmente el botón
     * para impedir que se abran varios FileChooser.
     */
    @FXML
    private void seleccionarEx17() {

        btnSubirEx17.setDisable(true);

        try {

            FileChooser fileChooser = crearSelectorPdf(
                    "Seleccionar EX-17 lleno"
            );

            aplicarUltimaCarpeta(fileChooser);

            File archivoSeleccionado
                    = fileChooser.showOpenDialog(null);

            if (archivoSeleccionado != null) {

                archivoEx17 = archivoSeleccionado;

                lblEx17.setText(
                        "✓ " + archivoEx17.getName()
                );
                lblEx17.setStyle(
                        "-fx-text-fill: #60A5FA; "
                        + "-fx-font-weight: bold;"
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
     * Selecciona la resolución de concesión y extrae automáticamente el NIE
     * contenido en ella.
     */
    @FXML
    private void seleccionarResolucion() {

        btnSubirResolucion.setDisable(true);

        try {

            FileChooser fileChooser = crearSelectorPdf(
                    "Seleccionar resolución de concesión"
            );

            aplicarUltimaCarpeta(fileChooser);

            File archivoSeleccionado
                    = fileChooser.showOpenDialog(null);

            if (archivoSeleccionado != null) {

                archivoResolucion = archivoSeleccionado;

                lblResolucion.setText(
                        "✓ " + archivoResolucion.getName()
                );
                lblResolucion.setStyle(
                        "-fx-text-fill: #60A5FA; "
                        + "-fx-font-weight: bold;"
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
     * Extrae el NIE de la resolución seleccionada y comprueba que tenga un
     * formato válido.
     */
    private void extraerYValidarNie() {

        nieValidado = null;

        try {

            String nie
                    = NieExtractor.extraerNie(archivoResolucion);

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

            AppLogger.error(
                    "LECTURA_RESOLUCION",
                    archivoResolucion,
                    null,
                    e
            );

            e.printStackTrace();
        }
    }

    /**
     * Actualiza el mensaje que informa del estado del NIE.
     */
    private void mostrarEstadoNie(String mensaje, String color) {

        lblNie.setOnMouseClicked(null);
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
     * Habilita CONTINUAR únicamente cuando existen los dos documentos
     * necesarios y se ha obtenido un NIE válido.
     */
    private void comprobarArchivos() {

        boolean todoCorrecto
                = archivoEx17 != null
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
     * - el documento origen seleccionado; - la resolución de concesión; - el
     * NIE previamente validado; - la plantilla interna del EX-17.
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

        String fechaHora
                = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(
                                "dd-MM-yyyy_HH-mm-ss"
                        )
                );

        String nombreArchivo
                = "EX17_"
                + nieValidado
                + "_"
                + fechaHora
                + ".pdf";

        fileChooser.setInitialFileName(
                nombreArchivo
        );

        File archivoDestino
                = fileChooser.showSaveDialog(null);

        if (archivoDestino == null) {
            return;
        }

        // --------------------------------------------------------
        // 3. Generar el documento
        // --------------------------------------------------------
        try {

            // Extraemos UNA sola vez antes de crear el archivo de salida.
            // Así, si el PDF origen falla, no queda un EX-17 vacío.
            DatosPersona datos = ExtractorDatosPdf.extraer(archivoEx17);

            Path destino
                    = archivoDestino.toPath();

            PlantillaEx17.copiarPlantilla(
                    destino
            );

            RellenadorPdf.rellenarNie(
                    destino,
                    datos,
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

            Path archivoLog = AppLogger.error(
                    "GENERACION_EX17",
                    archivoEx17,
                    ExtractorDatosPdf.getUltimoTipoDetectado(),
                    e
            );

            ultimoLogError = archivoLog;

            // Si se alcanzó a crear un archivo parcial, lo eliminamos.
            try {
                java.nio.file.Files.deleteIfExists(archivoDestino.toPath());
            } catch (IOException errorBorrado) {
                AppLogger.error(
                        "LIMPIEZA_ARCHIVO_PARCIAL",
                        archivoDestino,
                        ExtractorDatosPdf.getUltimoTipoDetectado(),
                        errorBorrado
                );
            }

            mostrarEstadoNie(
                    "⚠ No se pudo generar el EX-17.",
                    "#dc2626"
            );

            mostrarDialogoErrorGeneracion(archivoLog);
            mostrarEstadoErrorRecuperable();

            e.printStackTrace();
        }
    }

    /**
     * Comprueba que el documento origen contenga un formulario editable que
     * pueda ser procesado por PDFBox.
     *
     * Esto evita generar un EX-17 vacío cuando el usuario carga un PDF
     * incompatible.
     */
    /**
     * Deja la pantalla preparada para recuperarse de un error:
     * el mensaje permite volver a abrir el último log y se ofrece
     * iniciar inmediatamente un nuevo expediente.
     */
    private void mostrarEstadoErrorRecuperable() {

        lblNie.setOnMouseClicked(null);
        lblNie.setText("⚠ No se pudo generar el EX-17.");
        lblNie.setStyle(
                "-fx-text-fill: #dc2626; "
                + "-fx-font-size: 14px; "
                + "-fx-font-weight: bold;"
        );

        linkVerError.setVisible(ultimoLogError != null);
        linkVerError.setManaged(ultimoLogError != null);

        btnContinuar.setVisible(false);
        btnContinuar.setManaged(false);

        btnNuevoEx17.setVisible(true);
        btnNuevoEx17.setManaged(true);
        btnNuevoEx17.setDisable(false);

        btnSubirEx17.setDisable(true);
        btnSubirResolucion.setDisable(true);
    }

    @FXML
    private void verUltimoError() {
        abrirArchivoLog(ultimoLogError);
    }

    /**
     * Muestra un aviso profesional cuando falla la generación y permite
     * abrir directamente el archivo de registro correspondiente.
     */
    private void mostrarDialogoErrorGeneracion(Path archivoLog) {

        Dialog<Void> dialogo = new Dialog<>();

        dialogo.setTitle("Error al generar EX-17");
        dialogo.setHeaderText(null);
        dialogo.setResizable(false);

        Label titulo = new Label("No se pudo generar el EX-17");
        titulo.setStyle(
                "-fx-font-size: 15px; "
                + "-fx-font-weight: bold;"
        );

        Label mensaje = new Label(
                "El error se registró automáticamente."
        );
        mensaje.setStyle(
                "-fx-font-size: 13px; "
                + "-fx-text-fill: #555555;"
        );

        Button btnVerDetalles = new Button("Ver detalles");

        btnVerDetalles.setOnAction(event -> abrirArchivoLog(archivoLog));

        HBox botones = new HBox(btnVerDetalles);
        botones.setAlignment(Pos.CENTER_RIGHT);

        VBox contenido = new VBox(10, titulo, mensaje, botones);
        contenido.setPrefWidth(360);
        contenido.setStyle(
                "-fx-padding: 16px 18px 12px 18px;"
        );

        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setGraphic(null);
        dialogo.getDialogPane().setPrefWidth(400);
        dialogo.getDialogPane().setMinWidth(400);
        dialogo.getDialogPane().setMaxWidth(400);

        // Botón invisible de cancelación: permite que la X nativa cierre
        // correctamente el diálogo sin añadir otra fila de botones visible.
        ButtonType cierreNativo = new ButtonType(
                "Cerrar",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        dialogo.getDialogPane().getButtonTypes().add(cierreNativo);

        javafx.scene.Node botonNativo
                = dialogo.getDialogPane().lookupButton(cierreNativo);

        botonNativo.setVisible(false);
        botonNativo.setManaged(false);

        dialogo.setOnShown(event -> {
            aplicarIconoAplicacion(dialogo);
            dialogo.getDialogPane().getScene().getWindow().sizeToScene();
        });

        dialogo.showAndWait();
    }

    /**
     * Aplica icon.png, ubicado en src/main/resources, a cualquier diálogo.
     */
    private void aplicarIconoAplicacion(Dialog<?> dialogo) {

        try {
            var recursoIcono = getClass().getResource("/icon.png");

            if (recursoIcono == null) {
                return;
            }

            Stage stage = (Stage) dialogo.getDialogPane()
                    .getScene()
                    .getWindow();

            stage.getIcons().clear();
            stage.getIcons().add(
                    new Image(recursoIcono.toExternalForm())
            );

        } catch (Exception e) {
            // El icono es decorativo: un fallo aquí nunca debe
            // impedir que el diálogo de error pueda mostrarse.
        }
    }

    /**
     * Abre el log exacto generado por el error con la aplicación
     * predeterminada de Windows.
     */
    private void abrirArchivoLog(Path archivoLog) {

        if (archivoLog == null
                || !java.nio.file.Files.exists(archivoLog)) {

            mostrarEstadoNie(
                    "⚠ No se encontró el archivo de registro.",
                    "#dc2626"
            );
            return;
        }

        try {

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivoLog.toFile());
            } else {
                throw new IOException(
                        "La apertura automática de archivos no está disponible."
                );
            }

        } catch (IOException e) {

            AppLogger.error(
                    "APERTURA_LOG",
                    archivoLog.toFile(),
                    ExtractorDatosPdf.getUltimoTipoDetectado(),
                    e
            );

            mostrarEstadoNie(
                    "⚠ No se pudo abrir el registro. Está guardado en: "
                    + archivoLog,
                    "#dc2626"
            );
        }
    }

    private boolean documentoOrigenValido() {

        if (archivoEx17 == null
                || !archivoEx17.exists()
                || !archivoEx17.isFile()) {

            mostrarEstadoNie(
                    "⚠ Documento origen no válido",
                    "#dc2626"
            );

            return false;
        }

        return true;
    }

    /**
     * Cambia la interfaz al estado de expediente terminado.
     *
     * Una vez generado el PDF se bloquean los documentos actuales para evitar
     * modificaciones accidentales.
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
     * Limpia los datos del expediente actual y prepara la aplicación para
     * generar un nuevo EX-17.
     */
    private void reiniciarFormulario() {

        archivoEx17 = null;
        archivoResolucion = null;
        nieValidado = null;
        ultimoLogError = null;

        linkVerError.setVisible(false);
        linkVerError.setManaged(false);

        lblEx17.setText(
                "Ningún archivo seleccionado"
        );

        lblResolucion.setText(
                "Ningún archivo seleccionado"
        );

        lblEx17.setStyle("");
        lblResolucion.setStyle("");

        lblNie.setOnMouseClicked(null);
        lblNie.setText("");

        lblNie.setStyle(
                "-fx-font-size: 14px; "
                + "-fx-font-weight: bold;"
        );

        btnContinuar.setDisable(true);
    }

    /**
     * Inicia un nuevo expediente manteniendo las preferencias generales de la
     * aplicación.
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

        FileChooser fileChooser
                = new FileChooser();

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
     * Abre el FileChooser en la última carpeta utilizada, siempre que esa
     * carpeta todavía exista.
     */
    private void aplicarUltimaCarpeta(
            FileChooser fileChooser) {

        String rutaGuardada
                = preferencias.get(
                        PREF_ULTIMA_CARPETA,
                        null
                );

        if (rutaGuardada == null) {
            return;
        }

        File carpeta
                = new File(rutaGuardada);

        if (carpeta.exists()
                && carpeta.isDirectory()) {

            fileChooser.setInitialDirectory(
                    carpeta
            );
        }
    }

    /**
     * Guarda la carpeta del archivo utilizado para recuperarla la próxima vez
     * que se abra un FileChooser.
     */
    private void guardarUltimaCarpeta(
            File archivo) {

        if (archivo == null) {
            return;
        }

        File carpeta
                = archivo.getParentFile();

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
    // ACERCA DE
    // ============================================================
    /**
     * Muestra información general, versión y datos de contacto de la
     * aplicación.
     */
    @FXML
    private void abrirAcercaDe() {

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Acerca de");
        dialogo.setHeaderText(null);
        dialogo.setResizable(false);

        Label titulo = new Label("Rellenador EX-17 · Versión 1.2");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label informacion = new Label(
                "Gestoría Merlino\n\n"
                + "Desarrollado por Marvin Egoavil\n\n"
                + "Correo: marvinegoavilz@gmail.com\n"
                + "Teléfono: 722516228\n\n"
                + "© 2026 Gestoría Merlino"
        );
        informacion.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        Button btnAbrirRegistros = new Button("Abrir registros");
        btnAbrirRegistros.setOnAction(event -> abrirCarpetaLogs());

        HBox acciones = new HBox(btnAbrirRegistros);
        acciones.setAlignment(Pos.CENTER_RIGHT);

        VBox contenido = new VBox(14, titulo, informacion, acciones);
        contenido.setPrefWidth(400);
        contenido.setStyle("-fx-padding: 16px 18px 12px 18px;");

        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setGraphic(null);

        ButtonType cierreNativo = new ButtonType(
                "Cerrar",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        dialogo.getDialogPane().getButtonTypes().add(cierreNativo);

        javafx.scene.Node botonNativo =
                dialogo.getDialogPane().lookupButton(cierreNativo);
        botonNativo.setVisible(false);
        botonNativo.setManaged(false);

        dialogo.setOnShown(event -> {
            aplicarIconoAplicacion(dialogo);
            dialogo.getDialogPane().getScene().getWindow().sizeToScene();
        });

        dialogo.showAndWait();
    }

    /**
     * Abre la carpeta general de registros de la aplicación.
     */
    private void abrirCarpetaLogs() {

        try {
            String localAppData = System.getenv("LOCALAPPDATA");

            Path baseAplicacion = localAppData != null && !localAppData.isBlank()
                    ? Path.of(localAppData, "Rellenador EX-17")
                    : Path.of(System.getProperty("user.home"), "Rellenador EX-17");

            Path carpetaLogs = baseAplicacion.resolve("logs");
            java.nio.file.Files.createDirectories(carpetaLogs);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(carpetaLogs.toFile());
            } else {
                throw new IOException(
                        "La apertura automática de carpetas no está disponible."
                );
            }

        } catch (IOException e) {

            AppLogger.error(
                    "APERTURA_CARPETA_LOGS",
                    null,
                    null,
                    e
            );

            mostrarEstadoNie(
                    "⚠ No se pudo abrir la carpeta de registros.",
                    "#dc2626"
            );
        }
    }

}