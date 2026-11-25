package com.stickerstudio.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

public class StickerContentProvider extends ContentProvider {
    private static final String AUTH = "com.stickerstudio.app.stickercontentprovider";
    private static final String PACK = "stickerstudio_custom_pack";
    private static final int META = 1, META_ONE = 2, STICKERS = 3, ASSET = 4;
    private final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override public boolean onCreate() {
        matcher.addURI(AUTH, "metadata", META);
        matcher.addURI(AUTH, "metadata/*", META_ONE);
        matcher.addURI(AUTH, "stickers/*", STICKERS);
        matcher.addURI(AUTH, "stickers_asset/*/*", ASSET);
        return true;
    }

    private JSONObject meta() {
        try {
            String raw = getContext().getSharedPreferences("sticker_pack", 0).getString("metadata", "{}");
            return new JSONObject(raw);
        } catch (Exception e) { return new JSONObject(); }
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
        JSONObject m = meta();
        JSONArray stickers = m.optJSONArray("stickers");
        if (stickers == null) stickers = new JSONArray();
        int match = matcher.match(uri);
        if (match == META || match == META_ONE) {
            MatrixCursor c = new MatrixCursor(new String[]{
                "sticker_pack_identifier","sticker_pack_name","sticker_pack_publisher","sticker_pack_icon",
                "android_play_store_link","ios_app_download_link","sticker_pack_publisher_email",
                "sticker_pack_publisher_website","sticker_pack_privacy_policy_website",
                "sticker_pack_license_agreement_website","image_data_version",
                "whatsapp_will_not_cache_stickers","animated_sticker_pack"
            });
            if (stickers.length() >= 3 && (match == META || PACK.equals(uri.getLastPathSegment()))) {
                c.addRow(new Object[]{PACK, m.optString("name","My stickers"), m.optString("publisher","Sticker Studio"),
                    "tray.png", "", "", "", "", "", "", "1", 0, 0});
            }
            return c;
        }
        if (match == STICKERS) {
            MatrixCursor c = new MatrixCursor(new String[]{"sticker_file_name","sticker_emoji","sticker_accessibility_text"});
            if (PACK.equals(uri.getLastPathSegment())) {
                for (int i=0;i<stickers.length();i++) {
                    String file = stickers.optString(i);
                    c.addRow(new Object[]{file, "😀", "Sticker " + (i+1)});
                }
            }
            return c;
        }
        throw new IllegalArgumentException("URI no compatible: " + uri);
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (matcher.match(uri) != ASSET) throw new FileNotFoundException("Asset desconocido");
        if (!PACK.equals(uri.getPathSegments().get(1))) throw new FileNotFoundException("Paquete desconocido");
        String fileName = uri.getLastPathSegment();
        if (!fileName.matches("sticker_[0-9]{2}\\.webp|tray\\.png")) throw new FileNotFoundException("Archivo no permitido");
        File file = new File(new File(getContext().getFilesDir(), "stickerpacks/" + PACK), fileName);
        if (!file.exists()) throw new FileNotFoundException(fileName);
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public String getType(Uri uri) {
        int m = matcher.match(uri);
        if (m == ASSET) return uri.getLastPathSegment().endsWith(".png") ? "image/png" : "image/webp";
        if (m == META) return "vnd.android.cursor.dir/vnd." + AUTH + ".metadata";
        if (m == META_ONE) return "vnd.android.cursor.item/vnd." + AUTH + ".metadata";
        if (m == STICKERS) return "vnd.android.cursor.dir/vnd." + AUTH + ".stickers";
        return "application/octet-stream";
    }
    @Override public Uri insert(Uri u, ContentValues v) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri u, String s, String[] a) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri u, ContentValues v, String s, String[] a) { throw new UnsupportedOperationException(); }
}
