package com.interfaz.teyesdeveloperoptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private TextView title, content, status;
    private final ExecutorService reader = Executors.newSingleThreadExecutor();
    private boolean reading;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        title = findViewById(R.id.pageTitle);
        content = findViewById(R.id.pageContent);
        status = findViewById(R.id.helperStatus);
        findViewById(R.id.menuButton).setOnClickListener(this::showMenu);
        if (state != null) {
            title.setText(state.getCharSequence("title", getString(R.string.welcome_title)));
            content.setText(state.getCharSequence("content", getString(R.string.welcome_body)));
        } else {
            showPage(getString(R.string.welcome_title), getString(R.string.welcome_body));
        }
    }

    @Override protected void onResume() {
        super.onResume();
        status.setText(QuickSettingsAccessibilityService.getConnectedService() == null
                ? R.string.helper_inactive : R.string.helper_active);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putCharSequence("title", title.getText());
        state.putCharSequence("content", content.getText());
    }

    @Override protected void onDestroy() {
        reader.shutdownNow();
        super.onDestroy();
    }

    private void showPage(String heading, String text) {
        title.setText(heading);
        content.setText(text);
        findViewById(R.id.contentScroll).scrollTo(0, 0);
    }

    private void showMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor, Gravity.END);
        menu.inflate(R.menu.main_menu);
        menu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.instructions) showPage(getString(R.string.instructions), getString(R.string.instructions_body));
            else if (id == R.id.readKey) readKey();
            else if (id == R.id.accessibility) openAccessibility();
            else if (id == R.id.quickSettings) openQuickSettings();
            else if (id == R.id.about) showPage(getString(R.string.about), getString(R.string.about_body));
            else if (id == R.id.exit) finishAndRemoveTask();
            return true;
        });
        menu.show();
    }

    private void readKey() {
        if (reading) return;
        reading = true;
        showPage(getString(R.string.read_key), getString(R.string.reading));
        reader.execute(() -> {
            String result;
            try {
                String value = Settings.System.getString(getContentResolver(),
                        "PASSWORD_NETWORK_FACTORY_DEVELOPER_MODE");
                result = TeyesPasswordParser.describe(value);
            } catch (SecurityException e) {
                result = TeyesPasswordParser.unavailable("Access to the stored key was denied.");
            } catch (RuntimeException e) {
                result = TeyesPasswordParser.unavailable("The stored key could not be read (" + e.getClass().getSimpleName() + ").");
            }
            final String report = result + "\n\nCurrent device: " + android.os.Build.MODEL
                    + "\nCurrent firmware: " + android.os.Build.DISPLAY;
            runOnUiThread(() -> {
                reading = false;
                if (!isFinishing() && !isDestroyed()) {
                    new AlertDialog.Builder(this).setTitle(R.string.read_key).setMessage(report)
                            .setPositiveButton(R.string.close, null).show();
                    // Do not retain service codes in saved activity state or export them.
                    if (title.getText().toString().equals(getString(R.string.read_key))) {
                        showPage(getString(R.string.read_key), getString(R.string.key_read_finished));
                    }
                }
            });
        });
    }

    private boolean launch(Intent intent) {
        try {
            startActivity(intent);
            return true;
        } catch (android.content.ActivityNotFoundException | SecurityException e) {
            return false;
        }
    }

    private void openAccessibility() {
        ComponentName component = new ComponentName(this, QuickSettingsAccessibilityService.class);
        Intent details = new Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS")
                .putExtra("android.intent.extra.COMPONENT_NAME", component.flattenToString());
        if (!launch(details)) {
            showPage(getString(R.string.accessibility), getString(R.string.accessibility_fallback));
            if (!launch(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))) {
                new AlertDialog.Builder(this).setTitle(R.string.accessibility)
                        .setMessage(R.string.accessibility_unavailable).setPositiveButton(R.string.close, null).show();
            }
        }
    }

    private void openQuickSettings() {
        QuickSettingsAccessibilityService service = QuickSettingsAccessibilityService.getConnectedService();
        if (service == null) {
            new AlertDialog.Builder(this).setTitle(R.string.helper_required)
                    .setMessage(R.string.helper_required_body)
                    .setPositiveButton(R.string.accessibility, (dialog, which) -> openAccessibility())
                    .setNeutralButton(R.string.app_info, (dialog, which) -> {
                        if (!launch(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + getPackageName())))) {
                            showPage(getString(R.string.app_info), getString(R.string.app_info_unavailable));
                        }
                    }).setNegativeButton(R.string.close, null).show();
            return;
        }
        boolean accepted;
        try { accepted = service.openQuickSettingsFromApp(); }
        catch (RuntimeException e) { accepted = false; }
        showPage(getString(R.string.quick_settings), getString(accepted
                ? R.string.panel_requested : R.string.panel_failed));
    }
}