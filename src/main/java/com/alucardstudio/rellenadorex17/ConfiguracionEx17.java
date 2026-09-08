package com.alucardstudio.rellenadorex17;

/**
 * Representa una configuración personalizada para generar un EX-17.
 *
 * Esta clase almacenará las opciones seleccionadas por el usuario en
 * Configuración avanzada.
 *
 * Más adelante estas configuraciones podrán guardarse en un archivo y
 * recuperarse incluso después de cerrar la aplicación.
 */
public class ConfiguracionEx17 {

    // Nombre que el usuario dará a la configuración.
    // Ejemplo: "Madrid - Inicial"
    private String nombre;

    // Ciudad que aparecerá en lugar y fecha.
    private String ciudad;

    // Indica si se utilizará automáticamente la fecha actual.
    private boolean usarFechaActual;

    // Tipo de tarjeta:
    // INICIAL, RENOVACION o DUPLICADO.
    private String tipoTarjeta;

    // Indica si se marcará el consentimiento para comunicaciones DEHú.
    private boolean consiento;

    // Indica si se copiarán los datos de la Sección 1.
    private boolean copiarSeccion1;

    // Indica si la Sección 3 se rellenará automáticamente
    // utilizando los datos de la Sección 1.
    private boolean rellenarSeccion3;

    // ============================================================
    // CONSTRUCTOR VACÍO
    // ============================================================

    /*
     * Lo dejamos preparado porque más adelante será útil
     * cuando carguemos configuraciones desde un archivo.
     */
    public ConfiguracionEx17() {
    }

    // ============================================================
    // CONSTRUCTOR COMPLETO
    // ============================================================
    public ConfiguracionEx17(
            String nombre,
            String ciudad,
            boolean usarFechaActual,
            String tipoTarjeta,
            boolean consiento,
            boolean copiarSeccion1,
            boolean rellenarSeccion3) {

        this.nombre = nombre;
        this.ciudad = ciudad;
        this.usarFechaActual = usarFechaActual;
        this.tipoTarjeta = tipoTarjeta;
        this.consiento = consiento;
        this.copiarSeccion1 = copiarSeccion1;
        this.rellenarSeccion3 = rellenarSeccion3;
    }

    // ============================================================
    // GETTERS Y SETTERS
    // ============================================================
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public boolean isUsarFechaActual() {
        return usarFechaActual;
    }

    public void setUsarFechaActual(boolean usarFechaActual) {
        this.usarFechaActual = usarFechaActual;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public boolean isConsiento() {
        return consiento;
    }

    public void setConsiento(boolean consiento) {
        this.consiento = consiento;
    }

    public boolean isCopiarSeccion1() {
        return copiarSeccion1;
    }

    public void setCopiarSeccion1(boolean copiarSeccion1) {
        this.copiarSeccion1 = copiarSeccion1;
    }

    public boolean isRellenarSeccion3() {
        return rellenarSeccion3;
    }

    public void setRellenarSeccion3(boolean rellenarSeccion3) {
        this.rellenarSeccion3 = rellenarSeccion3;
    }

    // ============================================================
    // REPRESENTACIÓN DE TEXTO
    // ============================================================

    /*
     * Esto será útil cuando mostremos una configuración
     * dentro de una lista o ComboBox.
     *
     * En vez de enseñar:
     * ConfiguracionEx17@4a2d...
     *
     * aparecerá:
     * Madrid - Inicial
     */
    @Override
    public String toString() {
        return nombre;
    }
}
