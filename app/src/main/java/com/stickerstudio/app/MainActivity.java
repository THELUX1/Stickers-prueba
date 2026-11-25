package com.stickerstudio.app;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String PACK_ID = "stickerstudio_custom_pack";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new StickerBridge(), "StickerBridge");
        setContentView(webView);
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    private class StickerBridge {
        @JavascriptInterface
        public void addPack(String json) {
            runOnUiThread(() -> {
                try {
                    JSONObject data = new JSONObject(json);
                    JSONArray stickers = data.getJSONArray("stickers");
                    if (stickers.length() < 3 || stickers.length() > 30) {
                        Toast.makeText(MainActivity.this, "El paquete debe tener entre 3 y 30 stickers", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String name = data.optString("name", "Mi paquete");
                    String publisher = data.optString("publisher", "Sticker Studio");
                    File dir = new File(getFilesDir(), "stickerpacks/" + PACK_ID);
                    if (!dir.exists() && !dir.mkdirs()) throw new Exception("No se pudo crear la carpeta");

                    JSONArray filenames = new JSONArray();
                    for (int i = 0; i < stickers.length(); i++) {
                        String dataUrl = stickers.getString(i);
                        int comma = dataUrl.indexOf(',');
                        if (comma < 0) throw new Exception("Imagen no válida");
                        byte[] bytes = Base64.decode(dataUrl.substring(comma + 1), Base64.DEFAULT);
                        if (bytes.length > 100 * 1024) throw new Exception("Un sticker supera 100 KB");
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (bmp == null || bmp.getWidth() != 512 || bmp.getHeight() != 512)
                            throw new Exception("Cada sticker debe medir 512 × 512");
                        String fileName = String.format(java.util.Locale.US, "sticker_%02d.webp", i + 1);
                        try (FileOutputStream out = new FileOutputStream(new File(dir, fileName))) {
                            out.write(bytes);
                        }
                        filenames.put(fileName);
                        bmp.recycle();
                    }

                    // WhatsApp tray icon is a PNG. Convert the first sticker to PNG.
                    byte[] first = Base64.decode(stickers.getString(0).substring(stickers.getString(0).indexOf(',') + 1), Base64.DEFAULT);
                    Bitmap firstBmp = BitmapFactory.decodeByteArray(first, 0, first.length);
                    Bitmap tray = Bitmap.createScaledBitmap(firstBmp, 96, 96, true);
                    try (FileOutputStream out = new FileOutputStream(new File(dir, "tray.png"))) {
                        tray.compress(Bitmap.CompressFormat.PNG, 100, out);
                    }
                    firstBmp.recycle();
                    tray.recycle();

                    JSONObject meta = new JSONObject();
                    meta.put("name", name);
                    meta.put("publisher", publisher);
                    meta.put("stickers", filenames);
                    getSharedPreferences("sticker_pack", MODE_PRIVATE).edit().putString("metadata", meta.toString()).apply();

                    Intent intent = new Intent("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
                    intent.putExtra("sticker_pack_id", PACK_ID);
                    intent.putExtra("sticker_pack_authority", "com.stickerstudio.app.stickercontentprovider");
                    intent.putExtra("sticker_pack_name", name);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "No se encontró WhatsApp. Instala o actualiza WhatsApp.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "No se pudo crear el paquete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
