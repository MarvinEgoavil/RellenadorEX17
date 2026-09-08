package com.alucardstudio.rellenadorex17;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

/**
 * Controlador de la pantalla de Configuración Avanzada.
 *
 * Esta clase gestionará: - Navegación entre las opciones avanzadas. -
 * Configuración de secciones. - Fecha y lugar. - Vista previa. - Generación
 * avanzada del EX-17.
 */
public class SecondaryController {

    @FXML
    private TextField txtCiudad;

    @FXML
    private RadioButton rbInicial;

    @FXML
    private RadioButton rbRenovacion;

    @FXML
    private RadioButton rbDuplicado;

    @FXML
    private CheckBox chkConsiento;

    @FXML
    private CheckBox chkCopiarSeccion1;

    @FXML
    private CheckBox chkRellenarSeccion3;

    /**
     * Vuelve a la pantalla principal.
     */
    @FXML
    private void volver() {

        try {

            App.setRoot("primary");

        } catch (IOException e) {

            System.out.println(
                    "ERROR AL VOLVER A LA PANTALLA PRINCIPAL:"
            );

            e.printStackTrace();
        }
    }

    /**
     * Método reservado para la generación avanzada.
     *
     * De momento solo muestra un mensaje en consola. Más adelante aquí
     * conectaremos todas las opciones seleccionadas por el usuario.
     */
    @FXML
    private void generarAvanzado() {

        System.out.println(
                "Configuración avanzada: generación todavía no implementada."
        );
    }

    /**
     * Recoge las opciones seleccionadas en la pantalla avanzada y construye una
     * configuración EX-17.
     *
     * De momento solo la mostramos en consola. En el siguiente paso la
     * guardaremos de forma persistente.
     */
    @FXML
    private void guardarConfiguracion() {

        String tipoTarjeta = "INICIAL";

        if (rbRenovacion.isSelected()) {
            tipoTarjeta = "RENOVACION";
        } else if (rbDuplicado.isSelected()) {
            tipoTarjeta = "DUPLICADO";
        }

        ConfiguracionEx17 configuracion
                = new ConfiguracionEx17(
                        "Configuración temporal",
                        txtCiudad.getText(),
                        true,
                        tipoTarjeta,
                        chkConsiento.isSelected(),
                        chkCopiarSeccion1.isSelected(),
                        chkRellenarSeccion3.isSelected()
                );

        System.out.println("=== CONFIGURACIÓN CAPTURADA ===");
        System.out.println("Nombre: " + configuracion.getNombre());
        System.out.println("Ciudad: " + configuracion.getCiudad());
        System.out.println("Tipo: " + configuracion.getTipoTarjeta());
        System.out.println("Consiento: " + configuracion.isConsiento());
        System.out.println("Copiar sección 1: " + configuracion.isCopiarSeccion1());
        System.out.println("Rellenar sección 3: " + configuracion.isRellenarSeccion3());
    }

}
