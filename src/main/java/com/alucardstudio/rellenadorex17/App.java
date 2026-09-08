package com.alucardstudio.rellenadorex17;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;

/**
 * Clase principal de la aplicación JavaFX.
 *
 * Se encarga de cargar la vista principal y mostrar la ventana del Rellenador
 * EX-17.
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        // Cargamos la interfaz principal.
        //
        // Ancho: 720 px
        // Alto: 650 px
        //
        // Dejamos suficiente espacio para que, cuando aparezca
        // el botón "+ NUEVO EX-17", la opción de
        // "Configuración avanzada" no quede pegada al borde.
        scene = new Scene(
                loadFXML("primary"),
                520,
                500
        );

        // Asignamos la escena a la ventana.
        stage.setScene(scene);

        // Título de la ventana.
        stage.setTitle("Rellenador EX-17 v1.0");
        
        // Icono de la aplicación.
        stage.getIcons().add(
                new Image(
                        App.class.getResourceAsStream("/icon.png")
                )
        );

        // La pantalla clásica no necesita redimensionarse.
        stage.setResizable(false);

        // Mostramos la aplicación.
        stage.show();
    }

    /**
     * Cambia la vista principal de la aplicación.
     *
     * También adapta automáticamente el tamaño de la ventana dependiendo de la
     * pantalla que se esté mostrando.
     */
    static void setRoot(String fxml) throws IOException {

        scene.setRoot(loadFXML(fxml));

        Stage stage = (Stage) scene.getWindow();

        if ("secondary".equals(fxml)) {

            stage.setWidth(1040);
            stage.setHeight(760);

            stage.setResizable(true);

        } else {

            stage.setWidth(520);
            stage.setHeight(500);

            stage.setResizable(false);
        }

        // Centramos nuevamente la ventana después de cambiar su tamaño.
        stage.centerOnScreen();
    }

    /**
     * Carga un archivo FXML desde resources.
     */
    private static Parent loadFXML(String fxml) throws IOException {

        FXMLLoader fxmlLoader
                = new FXMLLoader(
                        App.class.getResource(fxml + ".fxml")
                );

        return fxmlLoader.load();
    }

    /**
     * Punto de entrada de la aplicación.
     */
    public static void main(String[] args) {
        launch();
    }
}
