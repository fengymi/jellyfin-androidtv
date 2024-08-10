package org.jellyfin.androidtv.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class ApplicationLanguageUtils {

    public static void setDefaultLocale(Resources resources, Locale... locales) {
        if (resources == null) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }

        resources.getConfiguration().setLocales(new LocaleList(locales));
        resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
    }
}
