package com.alucardstudio.rellenadorex17;

/**
 * Punto de entrada utilizado por el ejecutable de Windows.
 *
 * Esta clase no extiende JavaFX Application.
 * Su única función es iniciar App correctamente cuando
 * la aplicación se distribuye mediante jpackage.
 *
 * @author Marvin Egoavil
 * @version 1.2
 */
public class Launcher {

    public static void main(String[] args) {
        App.main(args);
    }
}