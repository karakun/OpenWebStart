package com.openwebstart.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

public class OpenWebStartResourceBundle extends ResourceBundle {
    
    /**
     * Bundle with unicode data
     */
    private final ResourceBundle bundle;

    /**
     * Initializing constructor
     *
     * @param bundle
     */
    private OpenWebStartResourceBundle(final ResourceBundle bundle, ResourceBundle parent) {
        this.bundle = bundle;
        this.parent = parent;
    }

    @Override
    public String getBaseBundleName() {
        return bundle.getBaseBundleName();
    }

    @Override
    public Locale getLocale() {
        return bundle.getLocale();
    }

    @Override
    public boolean containsKey(String key) {
        return bundle.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return bundle.keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Enumeration getKeys() {
        return bundle.getKeys();
    }

    @Override
    protected Object handleGetObject(final String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    /**
     * Gets the unicode friendly resource bundle
     *
     * @param baseNames List of all properties files, which should be given hierarchically (root first)
     * @return Unicode friendly resource bundle
     * @see java.util.ResourceBundle#getBundle(String)
     */
    public static final ResourceBundle getBundle(final List<String> baseNames, final Locale locale) {
        return getBundle(null, baseNames, locale);
    }

    public static final ResourceBundle getBundle(final List<String> baseNames) {
        return getBundle(baseNames, Locale.getDefault());
    }

    public static final ResourceBundle getBundle(final ResourceBundle parent, final String baseName) {
        return getBundle(parent, Collections.singletonList(baseName));
    }

    public static final ResourceBundle getBundle(final ResourceBundle parent, final List<String> baseNames) {
        return getBundle(parent, baseNames, Locale.getDefault());

    }

    public static final ResourceBundle getBundle(final ResourceBundle parent, final List<String> baseNames, final Locale locale) {
        final List<PropertyResourceBundle> resourceBundles = new ArrayList<>();
        for (String baseName : baseNames) {
            final ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);
            if (bundle instanceof PropertyResourceBundle) {
                resourceBundles.add((PropertyResourceBundle) bundle);
            } else if (baseNames.size() == 1) {
                return bundle;
            } else {
                throw new ClassCastException("Given base names are not properties base names");
            }
        }

        ResourceBundle prevBundle = parent;
        for (PropertyResourceBundle resourceBundle : resourceBundles) {
            final ResourceBundle currentUtf8ResourceBundle = new OpenWebStartResourceBundle(resourceBundle, prevBundle);
            prevBundle = currentUtf8ResourceBundle;
        }

        return prevBundle;
    }
}
