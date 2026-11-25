STICKER STUDIO ANDROID — PROYECTO INICIAL

Qué contiene:
- app/src/main/assets/www/: editor web original sin sonidos, preparado para ejecutarse dentro de la app.
- MainActivity.java: WebView y puente JavaScript para guardar stickers localmente y abrir el diálogo oficial de WhatsApp.
- StickerContentProvider.java: ContentProvider para que WhatsApp lea los stickers del paquete.
- .github/workflows/build-apk.yml: compilación automática del APK mediante GitHub Actions.

REQUISITOS DE WHATSAPP:
- Paquetes estáticos: 3 a 30 stickers.
- Cada sticker debe ser WebP de 512 x 512 y máximo 100 KB.
- La app valida cantidad, tamaño y dimensiones antes de solicitar la importación.

CÓMO OBTENER EL APK DESDE ANDROID:
1. Crea un repositorio nuevo en GitHub desde el navegador del teléfono.
2. Sube TODO el contenido de esta carpeta al repositorio (no la carpeta contenedora adicional).
3. Espera a que termine la acción "Build Sticker Studio APK".
4. En GitHub abre Actions > Build Sticker Studio APK > último resultado.
5. Descarga el artefacto "Sticker-Studio-debug" y extrae el APK.
6. Instala el APK en tu Android y acepta la instalación desde esa fuente si Android lo solicita.
7. Abre Sticker Studio, crea al menos 3 stickers y pulsa "Añadir paquete a WhatsApp".

IMPORTANTE:
- Este es un primer proyecto nativo de integración, no un APK ya compilado.
- La importación requiere que WhatsApp esté instalado y acepte el paquete.
- Para distribuir el APK a otras personas habrá que firmar una versión release.
- El icono del paquete se genera a partir del primer sticker.
- La colección del editor web se guarda en el almacenamiento local de la app; no desinstales la app si quieres conservarla.
- No contiene funciones de stickers con sonido.
- El identificador de aplicación predeterminado es com.stickerstudio.app. Se puede cambiar antes de publicar.
