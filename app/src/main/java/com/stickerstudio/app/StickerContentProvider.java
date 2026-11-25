package com.stickerstudio.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.content.res.AssetFileDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;

public class StickerContentProvider extends ContentProvider {
    private static final String AUTH = "com.stickerstudio.app.stickercontentprovider";
    private static final String PACK_ID = "stickerstudio_pack";
    private JSONObject pack() {
        try { return new JSONObject(getContext().getSharedPreferences("sticker_pack", 0).getString("metadata", "{}")); }
        catch (Exception e) { return new JSONObject(); }
    }
    @Override public boolean onCreate() { return true; }

    @Override public String getType(Uri uri) {
        java.util.List<String> p = uri.getPathSegments();
        if (p.size() == 1 && p.get(0).equals("metadata")) return "vnd.android.cursor.dir/vnd." + AUTH + ".metadata";
        if (p.size() == 2 && p.get(0).equals("metadata")) return "vnd.android.cursor.item/vnd." + AUTH + ".metadata";
        if (p.size() == 2 && p.get(0).equals("stickers")) return "vnd.android.cursor.dir/vnd." + AUTH + ".stickers";
        if (p.size() == 3 && p.get(0).equals("stickers_asset")) return p.get(2).endsWith(".png") ? "image/png" : "image/webp";
        throw new IllegalArgumentException("URI desconocida: " + uri);
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        java.util.List<String> p = uri.getPathSegments();
        JSONObject meta = pack();
        if (p.size() >= 1 && p.get(0).equals("metadata")) {
            String[] cols = {"sticker_pack_identifier","sticker_pack_name","sticker_pack_publisher","sticker_pack_icon","android_play_store_link","ios_app_download_link","sticker_pack_publisher_email","sticker_pack_publisher_website","sticker_pack_privacy_policy_website","sticker_pack_license_agreement_website","image_data_version","whatsapp_will_not_cache_stickers","animated_sticker_pack"};
            MatrixCursor c = new MatrixCursor(cols);
            if (p.size() == 1 || (p.size() == 2 && PACK_ID.equals(p.get(1)))) {
                c.addRow(new Object[]{PACK_ID,meta.optString("name","Mi paquete"),meta.optString("author","Creado por mí"),"sticker_pack_icon.png","","","","","","",meta.optString("version","1"),0,0});
            }
            return c;
        }
        if (p.size() == 2 && p.get(0).equals("stickers") && PACK_ID.equals(p.get(1))) {
            MatrixCursor c = new MatrixCursor(new String[]{"sticker_file_name","sticker_emoji","sticker_accessibility_text"});
            JSONArray arr = meta.optJSONArray("stickers");
            if (arr != null) for (int i=0; i<arr.length(); i++) {
                JSONObject item = arr.optJSONObject(i);
                if (item != null) c.addRow(new Object[]{item.optString("file"),item.optString("emoji","😀"),"Sticker " + (i+1)});
            }
            return c;
        }
        throw new IllegalArgumentException("URI desconocida: " + uri);
    }

    @Override public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        java.util.List<String> p = uri.getPathSegments();
        String name = null;
        if (p.size() == 3 && p.get(0).equals("stickers_asset") && PACK_ID.equals(p.get(1))) name = p.get(2);
        if (name == null || !(name.matches("sticker_[0-9]{2}\\.webp") || name.equals("sticker_pack_icon.png"))) throw new FileNotFoundException("Archivo no válido: " + uri);
        File file = new File(getContext().getFilesDir(), "sticker_pack/" + name);
        if (!file.exists()) throw new FileNotFoundException(name);
        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        return new AssetFileDescriptor(pfd, 0, file.length());
    }
    @Override public android.os.ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        AssetFileDescriptor afd = openAssetFile(uri, mode);
        return afd.getParcelFileDescriptor();
    }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
