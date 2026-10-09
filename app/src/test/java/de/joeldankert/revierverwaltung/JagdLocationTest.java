package de.joeldankert.revierverwaltung;

import static org.junit.Assert.*;

import android.Manifest;
import android.content.pm.PackageManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

@RunWith(RobolectricTestRunner.class)
public class JagdLocationTest {
    private MainActivity activity() {
        return Robolectric.buildActivity(MainActivity.class).setup().get();
    }

    private WebChromeClient chrome(MainActivity activity) {
        android.view.ViewGroup root = activity.findViewById(android.R.id.content);
        return ((WebView) root.getChildAt(0)).getWebChromeClient();
    }

    @Test public void requestsPermissionAndGrantsOnlyAfterRuntimeApproval() {
        MainActivity activity = activity();
        AtomicBoolean called = new AtomicBoolean(false);
        chrome(activity).onGeolocationPermissionsShowPrompt("https://revierverwaltung.duckdns.org", (origin, allow, retain) -> {
            assertEquals("https://revierverwaltung.duckdns.org", origin);
            assertTrue(allow);
            assertFalse(retain);
            called.set(true);
        });
        assertFalse(called.get());
        org.robolectric.shadows.ShadowActivity.PermissionsRequest request = Shadows.shadowOf(activity).getLastRequestedPermission();
        assertNotNull(request);
        assertEquals(42, request.requestCode);
        assertArrayEquals(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, request.requestedPermissions);
        activity.onRequestPermissionsResult(42, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, new int[]{PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_GRANTED});
        assertTrue(called.get());
    }

    @Test public void rejectsUntrustedOriginWithoutRequestingPermission() {
        MainActivity activity = activity();
        AtomicBoolean rejected = new AtomicBoolean(false);
        chrome(activity).onGeolocationPermissionsShowPrompt("https://evil.example", (origin, allow, retain) -> rejected.set(!allow && !retain));
        assertTrue(rejected.get());
    }

    @Test public void rejectsLocationWhenAndroidPermissionDenied() {
        MainActivity activity = activity();
        AtomicBoolean rejected = new AtomicBoolean(false);
        chrome(activity).onGeolocationPermissionsShowPrompt("https://revierverwaltung.duckdns.org", (origin, allow, retain) -> rejected.set(!allow && !retain));
        activity.onRequestPermissionsResult(42, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, new int[]{PackageManager.PERMISSION_DENIED, PackageManager.PERMISSION_DENIED});
        assertTrue(rejected.get());
    }

    @Test public void existingPermissionAllowsLocationWithoutNewDialog() {
        MainActivity activity = activity();
        Shadows.shadowOf(activity.getApplication()).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
        AtomicBoolean allowed = new AtomicBoolean(false);
        chrome(activity).onGeolocationPermissionsShowPrompt("https://revierverwaltung.duckdns.org", (origin, allow, retain) -> allowed.set(allow && !retain));
        assertTrue(allowed.get());
        assertNull(Shadows.shadowOf(activity).getLastRequestedPermission());
    }

    @Test public void hidingPromptRejectsOutstandingRequest() {
        MainActivity activity = activity();
        AtomicBoolean rejected = new AtomicBoolean(false);
        WebChromeClient client = chrome(activity);
        client.onGeolocationPermissionsShowPrompt("https://revierverwaltung.duckdns.org", (origin, allow, retain) -> rejected.set(!allow));
        client.onGeolocationPermissionsHidePrompt();
        assertTrue(rejected.get());
    }
}
