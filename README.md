# Sticker Studio Android

Proyecto Android nativo que integra el editor web original mediante WebView y añade:

- Selector de imágenes Android mediante ACTION_OPEN_DOCUMENT, sin permiso global de galería.
- Persistencia local del editor en WebView.
- Sincronización de stickers WebP al almacenamiento privado de la app.
- ContentProvider de solo lectura y acción de solicitud de incorporación a WhatsApp.
- Compilación APK mediante GitHub Actions.

## Compilar desde GitHub con Android

1. Crea un repositorio y sube **todo el contenido** de esta carpeta a la raíz.
2. Abre Actions y ejecuta `Build Sticker Studio APK`.
3. Al finalizar, abre el artefacto `Sticker-Studio-APK` y descarga el APK.

## Requisitos de stickers

Para habilitar el botón de WhatsApp se requieren entre 3 y 30 stickers. Cada WebP debe ser de hasta 100 KB. La integración depende de que WhatsApp acepte el paquete y del contrato de ContentProvider vigente en la versión instalada.

## Nota de compatibilidad

Este proyecto implementa la integración Android basada en el contrato público de WhatsApp, pero debe probarse en un dispositivo real y con WhatsApp instalado. La primera compilación es de depuración; no es una publicación firmada para Play Store. El editor no solicita permiso general para fotos: el selector del sistema concede acceso únicamente a la imagen elegida.
