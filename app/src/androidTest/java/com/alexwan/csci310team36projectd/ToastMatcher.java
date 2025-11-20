package com.alexwan.csci310team36projectd;

import android.os.IBinder;
import android.view.WindowManager;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import androidx.test.espresso.Root;

public class ToastMatcher extends TypeSafeMatcher<Root> {

    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;

        // TYPE_TOAST is deprecated on newer APIs, so allow TYPE_APPLICATION too
        if (type == WindowManager.LayoutParams.TYPE_TOAST ||
                type == WindowManager.LayoutParams.TYPE_APPLICATION) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            return windowToken == appToken;
        }
        return false;
    }
}
