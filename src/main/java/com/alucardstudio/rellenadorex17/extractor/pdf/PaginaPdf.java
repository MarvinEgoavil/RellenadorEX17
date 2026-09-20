package com.alucardstudio.rellenadorex17.extractor.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Representación posicional de una página PDF.
 *
 * Lee los TextPosition una sola vez y permite posteriormente
 * recuperar texto por coordenadas sin volver a procesar el PDF.
 */
public final class PaginaPdf {

    private final List<TextPosition> posiciones;

    private PaginaPdf(List<TextPosition> posiciones) {
        this.posiciones = posiciones;
    }

    public static PaginaPdf cargar(
            PDDocument documento,
            int numeroPagina) throws IOException {

        List<TextPosition> posiciones = new ArrayList<>();

        PDFTextStripper stripper = new PDFTextStripper() {

            @Override
            protected void processTextPosition(TextPosition text) {
                posiciones.add(text);
                super.processTextPosition(text);
            }
        };

        stripper.setSortByPosition(true);
        stripper.setStartPage(numeroPagina);
        stripper.setEndPage(numeroPagina);

        // IMPORTANTE:
        // getText() inicializa correctamente PDFTextStripper.
        // No utilizamos processPage() directamente.
        stripper.getText(documento);

        return new PaginaPdf(posiciones);
    }

    public String textoEn(
            float x,
            float y,
            float ancho,
            float alto) {

        float xMax = x + ancho;
        float yMax = y + alto;

        List<TextPosition> campo = posiciones.stream()
                .filter(p -> p.getXDirAdj() >= x)
                .filter(p -> p.getXDirAdj() <= xMax)
                .filter(p -> p.getYDirAdj() >= y)
                .filter(p -> p.getYDirAdj() <= yMax)
                .sorted(Comparator.comparingDouble(TextPosition::getXDirAdj))
                .toList();

        if (campo.isEmpty()) {
            return "";
        }

        StringBuilder resultado = new StringBuilder();
        TextPosition anterior = null;

        for (TextPosition actual : campo) {

            String texto = actual.getUnicode();

            if (texto == null || texto.isBlank()) {
                continue;
            }

            if ("✔".equals(texto) || "✓".equals(texto)) {
                continue;
            }

            if (anterior != null) {

                float finAnterior =
                        anterior.getXDirAdj()
                        + anterior.getWidthDirAdj();

                float distancia =
                        actual.getXDirAdj() - finAnterior;

                float anchoEspacio = Math.max(
                        anterior.getWidthOfSpace(),
                        actual.getWidthOfSpace()
                );

                /*
                 * Si PDFBox no proporciona un ancho de espacio
                 * razonable, utilizamos el tamaño de fuente
                 * únicamente como fallback.
                 */
                if (anchoEspacio <= 0
                        || Float.isNaN(anchoEspacio)
                        || Float.isInfinite(anchoEspacio)) {

                    anchoEspacio = Math.max(
                            anterior.getFontSizeInPt(),
                            actual.getFontSizeInPt()
                    ) * 0.30f;
                }

                /*
                 * Solo insertamos un espacio cuando existe
                 * una separación física real entre los caracteres.
                 */
                if (distancia > anchoEspacio * 0.60f) {
                    resultado.append(' ');
                }
            }

            resultado.append(texto);
            anterior = actual;
        }

        return normalizar(resultado.toString());
    }

    private static String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace('\u00A0', ' ')
                .replace("✔", " ")
                .replace("✓", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}