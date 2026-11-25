package com.apkforge.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import android.database.Cursor;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import java.io.*;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int PICK_ZIP = 41;
    private TextView status, projectPath;
    private String importedPath = "";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUI();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density); }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        return t;
    }

    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(34), dp(22), dp(28));
        root.setBackgroundColor(Color.rgb(12,13,20));
        scroll.addView(root);

        TextView brand = text("APK FORGE", 13, Color.rgb(173,145,255));
        brand.setTypeface(null, Typeface.BOLD);
        root.addView(brand);

        TextView title = text("Tu taller de APKs", 29, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.topMargin = dp(8); root.addView(title, tp);

        TextView sub = text("Prepara proyectos Android y compílalos localmente desde tu teléfono.", 15, 0xFFB8B8C7);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.topMargin = dp(8); root.addView(sub, sp);

        addSection(root, "01  ·  PROYECTO");
        Button importBtn = button("＋  Importar proyecto ZIP");
        root.addView(importBtn);
        importBtn.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/zip");
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(i, PICK_ZIP);
        });

        projectPath = text("Todavía no has importado ningún proyecto.", 13, 0xFFB8B8C7);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2);
        pp.topMargin = dp(12); root.addView(projectPath, pp);

        addSection(root, "02  ·  COMPILACIÓN LOCAL");
        TextView explain = text("APK Forge utiliza Termux + Termux Studio como motor de compilación. El motor se instala una sola vez y después Gradle compila los proyectos en el propio teléfono.", 14, 0xFFD0D0DA);
        root.addView(explain);

        Button setup = button("Ver instrucciones para instalar el motor");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.topMargin = dp(14); root.addView(setup, bp);
        setup.setOnClickListener(v -> showSetup());

        Button copy = button("Copiar comando de compilación");
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.topMargin = dp(10); root.addView(copy, cp);
        copy.setOnClickListener(v -> copyBuildCommand());

        Button open = button("Abrir Termux");
        LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(-1, -2);
        op.topMargin = dp(10); root.addView(open, op);
        open.setOnClickListener(v -> {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.termux");
            if (launch != null) startActivity(launch);
            else status.setText("Instala Termux desde F-Droid o GitHub y vuelve aquí.");
        });

        addSection(root, "03  ·  ESTADO");
        status = text("Listo. Importa un proyecto Android Gradle para comenzar.", 13, 0xFFB8B8C7);
        status.setPadding(dp(14), dp(14), dp(14), dp(14));
        status.setBackgroundColor(0xFF1B1D2A);
        root.addView(status);

        TextView foot = text("APK Forge 0.1 · El compilador se ejecuta en Termux; esta app organiza el flujo y prepara los comandos.", 11, 0xFF777889);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.topMargin = dp(24); root.addView(foot, fp);

        setContentView(scroll);
    }

    private void addSection(LinearLayout root, String s) {
        TextView t = text(s, 12, 0xFFAA91FF);
        t.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(28); p.bottomMargin = dp(12); root.addView(t, p);
    }

    private Button button(String s) {
        Button b = new Button(this);
        b.setText(s); b.setAllCaps(false); b.setTextSize(14);
        b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6550B8));
        return b;
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ZIP && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                String name = "project_" + System.currentTimeMillis() + ".zip";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, name);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/zip");
                values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/APKForge/Projects");
                Uri out = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (out == null) throw new IOException("No se pudo crear el archivo en Descargas.");
                try (InputStream in = getContentResolver().openInputStream(uri);
                     OutputStream os = getContentResolver().openOutputStream(out)) {
                    if (in == null || os == null) throw new IOException("No se pudo leer el ZIP.");
                    byte[] buf = new byte[8192]; int n;
                    while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
                }
                importedPath = "/storage/emulated/0/Download/APKForge/Projects/" + name;
                projectPath.setText("Proyecto importado:\n" + importedPath);
                status.setText("ZIP copiado a Descargas. Extrae el proyecto desde Termux y compílalo.");
            } catch (Exception e) {
                status.setText("Error al importar: " + e.getMessage());
            }
        }
    }

    private void copyBuildCommand() {
        if (importedPath.isEmpty()) {
            status.setText("Primero importa un ZIP de proyecto.");
            return;
        }
        String cmd = "mkdir -p ~/apkforge/projects ~/apkforge/builds && "
            + "unzip -o '" + importedPath + "' -d ~/apkforge/projects/importado && "
            + "find ~/apkforge/projects/importado -name settings.gradle -o -name settings.gradle.kts";
        android.content.ClipboardManager cm = (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(android.content.ClipData.newPlainText("APK Forge", cmd));
        status.setText("Comando copiado. Pégalo en Termux. Luego entra a la carpeta que contiene settings.gradle y ejecuta: studio build .");
    }

    private void showSetup() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Instalar motor local")
            .setMessage("1. Instala Termux desde F-Droid o GitHub (no Play Store).\n\n2. Abre Termux y ejecuta:\n\npkg update -y && pkg install -y git\ngit clone https://github.com/poordevcode/termux-android-studio.git ~/termux-studio\nbash ~/termux-studio/install.sh\nsource ~/.bashrc\nstudio doctor\n\nLa instalación descarga JDK, Gradle y Android SDK (aprox. 3 GB). Necesitas Android ARM64 y conexión para la instalación inicial.")
            .setPositiveButton("Entendido", null).show();
    }
}
