package com.alucardstudio.rellenadorex17;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

public class ListarCamposPdf {

    public static void listar(File archivo) throws IOException {

        try (PDDocument documento = Loader.loadPDF(archivo)) {

            PDAcroForm formulario =
                    documento.getDocumentCatalog().getAcroForm();

            if (formulario == null) {
                System.out.println("El PDF no tiene campos editables.");
                return;
            }

            System.out.println("=== CAMPOS DEL PDF ===");

            for (PDField campo : formulario.getFieldTree()) {
                System.out.println(
                        campo.getFullyQualifiedName()
                        + " | Valor actual: "
                        + campo.getValueAsString()
                );
            }
        }
    }
}