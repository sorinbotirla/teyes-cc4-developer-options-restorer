package com.interfaz.teyesdeveloperoptions;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import java.lang.ref.WeakReference;

public final class QuickSettingsAccessibilityService extends AccessibilityService {
    private static WeakReference<QuickSettingsAccessibilityService> connected = new WeakReference<>(null);

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // no accessibility events or window content needed for a global action
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.eventTypes = 0;
            info.flags = 0;
            setServiceInfo(info);
        }
        connected = new WeakReference<>(this);
    }

    static QuickSettingsAccessibilityService getConnectedService() {
        return connected.get();
    }

    boolean openQuickSettingsFromApp() {
        // invoked only by the foreground app button
        return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS);
    }

    void disableFromApp() {
        if (android.os.Build.VERSION.SDK_INT < 24) {
            return;
        }
        if (connected.get() == this) {
            connected.clear();
        }
        disableSelf();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // deliberately unused
    }

    @Override
    public void onInterrupt() {
        // no running actions to interrupt
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (connected.get() == this) {
            connected.clear();
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        if (connected.get() == this) {
            connected.clear();
        }
        super.onDestroy();
    }
}
