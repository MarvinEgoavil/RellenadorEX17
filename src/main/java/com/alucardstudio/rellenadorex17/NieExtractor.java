package com.alucardstudio.rellenadorex17;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class NieExtractor {

    private static final Pattern PATRON_NIE =
            Pattern.compile("\\b[XYZxyz][0-9]{7}[A-Za-z]\\b");

    public static String extraerNie(File pdf) throws IOException {

        try (PDDocument documento = Loader.loadPDF(pdf)) {

            PDFTextStripper stripper = new PDFTextStripper();

            String texto = stripper.getText(documento);

            Matcher matcher = PATRON_NIE.matcher(texto);

            if (matcher.find()) {
                return matcher.group().toUpperCase();
            }
        }

        return null;
    }
}