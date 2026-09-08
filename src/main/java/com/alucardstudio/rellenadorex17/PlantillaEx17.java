package com.alucardstudio.rellenadorex17;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

public class PlantillaEx17 {

    private static final String RUTA_PLANTILLA =
            "/com/alucardstudio/rellenadorex17/pdf/EX17_PLANTILLA.pdf";

    public static Path copiarPlantilla(Path destino) throws IOException {

        try (InputStream entrada =
                PlantillaEx17.class.getResourceAsStream(RUTA_PLANTILLA)) {

            if (entrada == null) {
                throw new IOException("No se encontró la plantilla EX-17 interna.");
            }

            Files.copy(
                    entrada,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return destino;
        }
    }
}