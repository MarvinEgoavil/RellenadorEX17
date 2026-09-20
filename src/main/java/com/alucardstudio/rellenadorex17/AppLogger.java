package com.alucardstudio.rellenadorex17;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro persistente de errores de Rellenador EX-17.
 *
 * Crea un archivo diario dentro de la carpeta "logs".
 */
public final class AppLogger {

    private static final String VERSION = "1.2.0";

    private static final DateTimeFormatter FECHA_ARCHIVO
            = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter FECHA_HORA
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private AppLogger() {
    }

    /**
     * Registra un error y devuelve la ruta exacta del archivo de log.
     *
     * @return ruta del log escrito, o null si no se pudo registrar.
     */
    public static Path error(
            String etapa,
            File archivo,
            String formato,
            Throwable error) {

        try {
            String localAppData = System.getenv("LOCALAPPDATA");

            Path baseAplicacion = localAppData != null && !localAppData.isBlank()
                    ? Path.of(localAppData, "Rellenador EX-17")
                    : Path.of(System.getProperty("user.home"), "Rellenador EX-17");

            Path carpetaLogs = baseAplicacion.resolve("logs");

            Files.createDirectories(carpetaLogs);
            eliminarLogsAntiguos(carpetaLogs);

            Path archivoLog = carpetaLogs.resolve(
                    "rellenador-ex17-"
                    + LocalDateTime.now().format(FECHA_ARCHIVO)
                    + ".log"
            );

            String contenido = construirEntrada(
                    etapa,
                    archivo,
                    formato,
                    error
            );

            Files.writeString(
                    archivoLog,
                    contenido,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            return archivoLog;

        } catch (IOException errorLog) {
            System.err.println("No se pudo escribir el registro de errores.");
            errorLog.printStackTrace();
            return null;
        }
    }

    private static String construirEntrada(
            String etapa,
            File archivo,
            String formato,
            Throwable error) {

        StringWriter stack = new StringWriter();

        if (error != null) {
            error.printStackTrace(new PrintWriter(stack));
        }

        String nombre = archivo == null
                ? "(sin archivo)"
                : archivo.getName();

        String ruta = archivo == null
                ? "(sin ruta)"
                : archivo.getAbsolutePath();

        String extension = obtenerExtension(nombre);

        long tamano = archivo != null && archivo.isFile()
                ? archivo.length()
                : -1L;

        return System.lineSeparator()
                + "============================================================"
                + System.lineSeparator()
                + "FECHA/HORA : " + LocalDateTime.now().format(FECHA_HORA)
                + System.lineSeparator()
                + "VERSION    : " + VERSION
                + System.lineSeparator()
                + "ETAPA      : " + seguro(etapa)
                + System.lineSeparator()
                + "FORMATO    : " + seguro(formato)
                + System.lineSeparator()
                + "ARCHIVO    : " + nombre
                + System.lineSeparator()
                + "EXTENSION  : " + seguro(extension)
                + System.lineSeparator()
                + "TAMANO     : " + tamano + " bytes"
                + System.lineSeparator()
                + "RUTA       : " + ruta
                + System.lineSeparator()
                + "EXCEPCION  : "
                + (error == null ? "(sin excepción)" : error.getClass().getName())
                + System.lineSeparator()
                + "MENSAJE    : "
                + (error == null ? "(sin mensaje)" : seguro(error.getMessage()))
                + System.lineSeparator()
                + "JAVA       : " + System.getProperty("java.version")
                + System.lineSeparator()
                + "SO         : "
                + System.getProperty("os.name") + " "
                + System.getProperty("os.version")
                + System.lineSeparator()
                + "STACK TRACE:"
                + System.lineSeparator()
                + stack
                + "============================================================"
                + System.lineSeparator();
    }

    private static void eliminarLogsAntiguos(Path carpetaLogs) {

        try (var archivos = Files.list(carpetaLogs)) {

            long limite = System.currentTimeMillis()
                    - (30L * 24L * 60L * 60L * 1000L);

            archivos.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName()
                            .toString()
                            .startsWith("rellenador-ex17-"))
                    .filter(path -> path.getFileName()
                            .toString()
                            .endsWith(".log"))
                    .forEach(path -> {
                        try {
                            if (Files.getLastModifiedTime(path).toMillis() < limite) {
                                Files.deleteIfExists(path);
                            }
                        } catch (IOException ignored) {
                            // Un fallo de limpieza nunca debe impedir
                            // que la aplicación continúe funcionando.
                        }
                    });

        } catch (IOException ignored) {
            // La limpieza es secundaria; el registro principal continúa.
        }
    }

    private static String obtenerExtension(String nombre) {

        int punto = nombre.lastIndexOf('.');

        if (punto < 0 || punto == nombre.length() - 1) {
            return "(sin extensión)";
        }

        return nombre.substring(punto + 1).toLowerCase();
    }

    private static String seguro(String valor) {
        return valor == null || valor.isBlank() ? "(no disponible)" : valor;
    }
}
