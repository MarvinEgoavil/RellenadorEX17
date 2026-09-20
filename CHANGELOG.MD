RELLENADOR EX-17
CHANGELOG

Versión 1.2.0
==============

NOVEDADES
---------
- Añadido soporte para generar EX-17 a partir de los formularios:
  - EX-03
  - EX-25
  - EX-26
  - EX-31
  - EX-32
- Arquitectura de extracción separada por tipo de formulario.
- Normalización de los datos extraídos mediante DatosPersona.
- Detección automática del tipo de formulario origen.
- Soporte para distintas variantes de nombre de archivo, como EX03, EX-03 y EX_03.
- Incorporado arrastrar y soltar (Drag & Drop) para cargar documentos.
- La aplicación recuerda la última carpeta utilizada.
- Añadido sistema de registros (logs) para diagnosticar errores.
- Añadido acceso directo a los detalles del último error.
- Añadida opción para abrir la carpeta de registros desde “Acerca de”.
- Mejorada la ventana “Acerca de” con información de versión, autor y datos de contacto.

MEJORAS EN LA GENERACIÓN DEL EX-17
----------------------------------
- El documento origen se extrae una única vez antes de generar el EX-17.
- RellenadorPdf recibe directamente los datos normalizados en DatosPersona.
- Eliminado el antiguo flujo que volvía a leer el PDF origen durante la generación.
- El NIE se obtiene de la resolución, se valida y se distribuye automáticamente en los campos correspondientes del EX-17.
- El NIE también se rellena en la sección de domicilio a efectos de notificaciones.
- Rellenado automático de datos personales, nacimiento, nacionalidad, filiación, domicilio y contacto.
- Rellenado automático de sexo y estado civil cuando el formulario origen proporciona esos datos.
- Rellenado automático de la sección 3 de notificaciones.
- El lugar de firma utiliza la localidad extraída del documento origen.
- La fecha de firma utiliza automáticamente la fecha actual.
- Marcado automático de “Consiento”.
- Marcado automático de “Tarjeta inicial”.
- “Renovación” y “Duplicado” permanecen desmarcados en la configuración clásica.

INTERFAZ
-------
- Actualizado el título a “Rellenador EX-17 v1.2”.
- Interfaz compacta para el flujo principal de generación.
- Los nombres de los archivos seleccionados se muestran resaltados.
- Añadido botón “Nuevo EX-17” al finalizar un expediente o recuperarse de un error.
- Tras un error, los documentos actuales quedan bloqueados para evitar estados inconsistentes.
- Añadido enlace “Ver detalles del error”.
- Mejorados los diálogos de error.
- Incorporado el icono de la aplicación en los diálogos.
- Mejorado el diálogo “Acerca de”, manteniendo la X nativa de cierre y sin botón “Cerrar” adicional.

ERRORES Y DIAGNÓSTICO
---------------------
- Los errores de generación se registran automáticamente.
- Los logs incluyen fecha, hora, versión, etapa, formato detectado, archivo, ruta, excepción, mensaje, versión de Java, sistema operativo y traza.
- Los registros se almacenan en la carpeta local de la aplicación.
- Limpieza automática de registros antiguos de más de 30 días.
- Si falla la generación, se elimina el PDF de salida parcial cuando sea posible.

CORRECCIONES
------------
- Corregida la extracción de sexo y estado civil en los formularios compatibles.
- Corregida la separación de domicilio, número y piso en los extractores correspondientes.
- Corregida la extracción de datos familiares en EX-03.
- Corregida la contaminación de datos provocada por determinadas casillas del EX-03.
- Corregida la detección de formularios cuando el nombre contiene guiones, espacios o guion bajo.
- Corregida la localidad utilizada como lugar de firma.
- Corregido el flujo que podía realizar una doble extracción del documento origen.
- Corregida la ventana “Acerca de”.

MANTENIMIENTO INTERNO
---------------------
- Separación de la lógica específica de EX-03, EX-25, EX-26, EX-31 y EX-32 en extractores independientes.
- Eliminación de código obsoleto de RellenadorPdf perteneciente al antiguo flujo de lectura directa del formulario origen.
- Conservada una única ruta de generación basada en DatosPersona.
- Preparada la arquitectura para incorporar nuevos formularios sin modificar los extractores ya estabilizados.

VALIDACIÓN DE LA VERSIÓN
------------------------
Antes del cierre de la versión 1.2.0 se realizaron pruebas de regresión con:
- EX-03
- EX-25
- EX-26
- EX-31
- EX-32

Los cinco formatos generaron correctamente el EX-17 tras la limpieza final del código.

DATOS DEL PROYECTO
------------------
Aplicación: Rellenador EX-17
Versión: 1.2.0
Desarrollado por: Marvin Egoavil
Gestoría Merlino
© 2026 Gestoría Merlino
