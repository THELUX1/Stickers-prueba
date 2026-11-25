package com.stickerstudio.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.webkit.JavascriptInterface;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_REQUEST = 4102;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(16,17,26));
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        webView.addJavascriptInterface(new StickerBridge(), "AndroidStickerBridge");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                try { startActivityForResult(intent, FILE_REQUEST); }
                catch (ActivityNotFoundException e) { fileCallback = null; callback.onReceiveValue(null); Toast.makeText(MainActivity.this, "No se encontró un selector de archivos", Toast.LENGTH_LONG).show(); }
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/stickerstudio/index.html");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST && fileCallback != null) {
            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null) result = new Uri[]{data.getData()};
            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    private class StickerBridge {
        @JavascriptInterface public void syncPack(String json) {
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray arr = obj.optJSONArray("stickers");
                if (arr == null) arr = new JSONArray();
                if (arr.length() > 30) throw new Exception("Máximo 30 stickers");
                File dir = new File(getFilesDir(), "sticker_pack");
                if (!dir.exists()) dir.mkdirs();
                for (File f : dir.listFiles()) if (f.isFile()) f.delete();
                JSONArray saved = new JSONArray();
                for (int i=0; i<arr.length(); i++) {
                    JSONObject item = arr.getJSONObject(i);
                    String data = item.optString("data", "");
                    int comma = data.indexOf(',');
                    if (comma < 0) continue;
                    byte[] bytes = Base64.decode(data.substring(comma+1), Base64.DEFAULT);
                    if (bytes.length > 100*1024) throw new Exception("Un sticker supera 100 KB");
                    String name = String.format(java.util.Locale.US, "sticker_%02d.webp", i+1);
                    FileOutputStream out = new FileOutputStream(new File(dir, name)); out.write(bytes); out.close();
                    if (i == 0) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (bitmap != null) {
                            Bitmap icon = Bitmap.createScaledBitmap(bitmap, 96, 96, true);
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            icon.compress(Bitmap.CompressFormat.PNG, 100, bos);
                            FileOutputStream iconOut = new FileOutputStream(new File(dir, "sticker_pack_icon.png"));
                            iconOut.write(bos.toByteArray()); iconOut.close(); icon.recycle(); bitmap.recycle();
                        }
                    }
                    JSONObject row = new JSONObject(); row.put("file", name); row.put("emoji", "😀"); saved.put(row);
                }
                JSONObject meta = new JSONObject();
                meta.put("name", obj.optString("packName", "Mi paquete"));
                meta.put("author", obj.optString("packAuthor", "Creado por mí"));
                meta.put("stickers", saved);
                meta.put("version", String.valueOf(System.currentTimeMillis()));
                if (!new File(dir, "sticker_pack_icon.png").exists()) throw new Exception("No se pudo crear el icono del paquete");
                getSharedPreferences("sticker_pack", MODE_PRIVATE).edit().putString("metadata", meta.toString()).apply();
                getContentResolver().notifyChange(Uri.parse("content://com.stickerstudio.app.stickercontentprovider/metadata"), null);
            } catch (Exception e) { android.util.Log.e("StickerStudio", "No se pudo sincronizar el paquete", e); }
        }
        @JavascriptInterface public void addToWhatsApp() {
            runOnUiThread(() -> {
                try {
                    String metaText = getSharedPreferences("sticker_pack", MODE_PRIVATE).getString("metadata", "{}");
                    JSONObject meta = new JSONObject(metaText);
                    JSONArray list = meta.optJSONArray("stickers");
                    if (list == null || list.length() < 3) { Toast.makeText(MainActivity.this, "WhatsApp requiere al menos 3 stickers", Toast.LENGTH_LONG).show(); return; }
                    Intent intent = new Intent("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
                    intent.putExtra("sticker_pack_id", "stickerstudio_pack");
                    intent.putExtra("sticker_pack_authority", "com.stickerstudio.app.stickercontentprovider");
                    intent.putExtra("sticker_pack_name", meta.optString("name", "Mi paquete"));
                    intent.setPackage("com.whatsapp");
                    try { startActivityForResult(intent, 0); }
                    catch (ActivityNotFoundException e) {
                        intent.setPackage("com.whatsapp.w4b");
                        try { startActivity(intent); } catch (ActivityNotFoundException ex) { Toast.makeText(MainActivity.this, "Instala WhatsApp para añadir el paquete", Toast.LENGTH_LONG).show(); }
                    }
                } catch (Exception e) { Toast.makeText(MainActivity.this, "No se pudo preparar el paquete", Toast.LENGTH_LONG).show(); }
            });
        }
    }
}
