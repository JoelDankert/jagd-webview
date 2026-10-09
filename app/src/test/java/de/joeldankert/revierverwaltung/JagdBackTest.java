package de.joeldankert.revierverwaltung;

import static org.junit.Assert.*;
import android.content.Context;
import android.webkit.WebView;
import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class JagdBackTest {
    private static class HistoryWebView extends WebView {
        boolean navigable = true;
        boolean wentBack = false;
        HistoryWebView(Context context) { super(context); }
        @Override public boolean canGoBack() { return navigable; }
        @Override public void goBack() { wentBack = true; }
    }

    @Test public void androidBackUsesWebViewHistory() throws Exception {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        HistoryWebView webView = new HistoryWebView(activity);
        Field field = MainActivity.class.getDeclaredField("webView");
        field.setAccessible(true);
        field.set(activity, webView);

        activity.onBackPressed();
        assertTrue("Android Back must call WebView.goBack()", webView.wentBack);
        assertFalse(activity.isFinishing());
    }

    @Test public void androidBackExitsWhenThereIsNoHistory() throws Exception {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        HistoryWebView webView = new HistoryWebView(activity);
        webView.navigable = false;
        Field field = MainActivity.class.getDeclaredField("webView");
        field.setAccessible(true);
        field.set(activity, webView);

        activity.onBackPressed();
        assertFalse(webView.wentBack);
        assertTrue(activity.isFinishing());
    }
}
