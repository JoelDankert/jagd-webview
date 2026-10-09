package de.joeldankert.revierverwaltung;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public final class MainActivity extends Activity {
    private static final String URL = "https://revierverwaltung.duckdns.org/";
    private static final int LOCATION_REQUEST = 42;
    private WebView webView;
    private OnBackInvokedCallback modernBackCallback;
    private GeolocationPermissions.Callback pendingLocation;
    private String pendingOrigin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                Uri uri = Uri.parse(origin);
                if (!"https".equals(uri.getScheme())
                        || !"revierverwaltung.duckdns.org".equals(uri.getHost())
                        || (uri.getPort() != -1 && uri.getPort() != 443)) {
                    callback.invoke(origin, false, false);
                    return;
                }
                denyPendingLocation();
                if (hasLocationPermission()) {
                    callback.invoke(origin, true, false);
                } else {
                    pendingLocation = callback;
                    pendingOrigin = origin;
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
                }
            }

            @Override
            public void onGeolocationPermissionsHidePrompt() {
                denyPendingLocation();
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        CookieManager.getInstance().setAcceptCookie(true);
        webView.loadUrl(URL);
        setContentView(webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            modernBackCallback = this::handleBack;
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, modernBackCallback);
        }
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_REQUEST || pendingLocation == null) return;
        boolean granted = false;
        for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
            if ((Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i])
                    || Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[i]))
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted = true;
            }
        }
        GeolocationPermissions.Callback callback = pendingLocation;
        String origin = pendingOrigin;
        pendingLocation = null;
        pendingOrigin = null;
        callback.invoke(origin, granted, false);
    }

    private void denyPendingLocation() {
        if (pendingLocation == null) return;
        GeolocationPermissions.Callback callback = pendingLocation;
        String origin = pendingOrigin;
        pendingLocation = null;
        pendingOrigin = null;
        callback.invoke(origin, false, false);
    }

    @Override
    protected void onPause() {
        CookieManager.getInstance().flush();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && modernBackCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(modernBackCallback);
            modernBackCallback = null;
        }
        denyPendingLocation();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        handleBack();
    }

    private void handleBack() {
        if (webView == null) return;
        webView.evaluateJavascript("window.jagdHandleBack ? window.jagdHandleBack() : null", handled -> {
            // true: submenu closed; false: app root. Neither should close the Activity.
            if ("true".equals(handled) || "false".equals(handled)) return;
            // No SPA handler (for example, the legal page): use actual WebView pages.
            if (webView.canGoBack()) webView.goBack();
        });
    }
}
