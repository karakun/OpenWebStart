/* PacFileEvaluator.java
   Copyright (C) 2011 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package com.openwebstart.proxy.pac;

import net.adoptopenjdk.icedteaweb.IcedTeaWebConstants;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.adoptopenjdk.icedteaweb.shaded.mozilla.javascript.Context;
import net.adoptopenjdk.icedteaweb.shaded.mozilla.javascript.ContextFactory;
import net.adoptopenjdk.icedteaweb.shaded.mozilla.javascript.Function;
import net.adoptopenjdk.icedteaweb.shaded.mozilla.javascript.Scriptable;
import net.sourceforge.jnlp.util.TimedHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketPermission;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.PropertyPermission;

import static net.adoptopenjdk.icedteaweb.JavaSystemPropertiesConstants.VM_NAME;
import static sun.security.util.SecurityConstants.PROPERTY_READ_ACTION;

/**
 * Represents a Proxy Auto Config file. This object can be used to evaluate the
 * proxy file to findPreferencesFile the proxy for a given url.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Proxy_auto-config#The_PAC_file">The PAC File</a>
 */
//TODO: Class should be refactored
public class PacFileEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(PacFileEvaluator.class);

    private static final String PAC_FUNCS_JS = "net/sourceforge/jnlp/runtime/pac-funcs.js";

    private final String pacHelperFunctionContents;
    private final String pacContents;
    private final URL pacUrl;
    private final TimedHashMap<String, String> pacCache = new TimedHashMap<>();

    /**
     * Initialize a new object by using the PAC file located at the given URL.
     *
     * @param pacUrl the url of the PAC file to use
     */
    public PacFileEvaluator(final URL pacUrl) {
        LOG.debug("Create Rhino-based PAC evaluator for '{}'", pacUrl);
        this.pacHelperFunctionContents = getHelperFunctionContents();
        this.pacUrl = pacUrl;
        this.pacContents = getContent(pacUrl);
    }

    /**
     * Get the proxies for accessing a given URL. The result is obtained by
     * evaluating the PAC file with the given url (and the host) as input.
     * <p>
     * This method performs caching of the result.
     *
     * @param url the url for which a proxy is desired
     * @return a list of proxies in a string like
     * <pre>"PROXY foo.example.com:8080; PROXY bar.example.com:8080; DIRECT"</pre>
     * @see #getProxiesWithoutCaching(URL)
     */
    String getProxies(final URL url) {
        final String cachedResult = getFromCache(url);
        if (cachedResult != null) {
            return cachedResult;
        }

        final String result = getProxiesWithoutCaching(url);
        addToCache(url, result);
        return result;
    }

    /**
     * Get the proxies for accessing a given URL. The result is obtained by
     * evaluating the PAC file with the given url (and the host) as input.
     *
     * @param url the url for which a proxy is desired
     * @return a list of proxies in a string like
     * <pre>"PROXY example.com:3128; DIRECT"</pre>
     * @see #getProxies(URL)
     */
    private String getProxiesWithoutCaching(final URL url) {
        if (pacHelperFunctionContents == null) {
            LOG.error("Error loading pac functions");
            return PacConstants.DIRECT;
        }

        final EvaluatePacAction evaluatePacAction = new EvaluatePacAction(pacContents, pacUrl.toString(),
                pacHelperFunctionContents, url);

        // Purposefully giving only these permissions rather than using java.policy. The "evaluatePacAction"
        // isn't supposed to do very much and so doesn't require all the default permissions given by
        // java.policy
        final Permissions p = new Permissions();
        p.add(new RuntimePermission("accessClassInPackage.org.mozilla.javascript"));
        p.add(new SocketPermission("*", "resolve"));
        p.add(new PropertyPermission(VM_NAME, PROPERTY_READ_ACTION));

        final ProtectionDomain pd = new ProtectionDomain(null, p);
        final AccessControlContext context = new AccessControlContext(new ProtectionDomain[]{pd});

        return AccessController.doPrivileged(evaluatePacAction, context);
    }

    /**
     * Returns the contents of file at pacUrl as a String.
     */
    private String getContent(final URL pacUrl) {
        final StringBuilder contents = new StringBuilder();
        try {
            String line;
            try (BufferedReader pacReader = new BufferedReader(new InputStreamReader(pacUrl.openStream()))) {
                while ((line = pacReader.readLine()) != null) {
                    contents.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            return null;
        }

        return contents.toString();
    }

    /**
     * Returns the pac helper functions as a String. The functions are read
     * from net/sourceforge/jnlp/resources/pac-funcs.js
     */
    private String getHelperFunctionContents() {
        return getContent(getPacFuncJsUrl());
    }

    private URL getPacFuncJsUrl() {
        final ClassLoader cl = this.getClass().getClassLoader();
        if (cl != null) {
            return cl.getResource(PAC_FUNCS_JS);
        }
        return ClassLoader.getSystemClassLoader().getResource(PAC_FUNCS_JS);
    }

    /**
     * Gets an entry from the cache
     */
    private String getFromCache(final URL url) {
        final String lookupString = url.getProtocol() + "://" + url.getHost();
        final String result = pacCache.get(lookupString);
        return result;
    }

    /**
     * Adds an entry to the cache
     */
    private void addToCache(final URL url, final String proxyResult) {
        final String lookupString = url.getAuthority() + "://" + url.getHost();
        pacCache.put(lookupString, proxyResult);
    }

    /**
     * Helper classs to run remote javascript code (specified by the user as
     * PAC URL) inside a sandbox.
     */
    private static class EvaluatePacAction implements PrivilegedAction<String> {

        private final String pacContents;
        private final String pacUrl;
        private final String pacFuncsContents;
        private final URL url;

        EvaluatePacAction(String pacContents, String pacUrl, String pacFuncsContents, URL url) {
            this.pacContents = pacContents;
            this.pacUrl = pacUrl;
            this.pacFuncsContents = pacFuncsContents;
            this.url = url;
        }

        public String run() {
            Context cx = ContextFactory.getGlobal().enterContext();
            try {
                /*
                 * TODO defense in depth.
                 *
                 * This is already running within a sandbox, but we can (and we
                 * should) lock it down further. Look into ClassShutter.
                 */
                final Scriptable scope = cx.initStandardObjects();
                // any optimization level greater than -1 will trigger code generation
                // and this block will then need classloader permissions
                cx.setOptimizationLevel(-1);
                cx.evaluateString(scope, pacFuncsContents, "internal", 1, null);
                cx.evaluateString(scope, pacContents, pacUrl, 1, null);

                final Object functionObj = scope.get("FindProxyForURL", scope);
                if (functionObj instanceof Function) {
                    final Object[] args = {url.toString(), url.getHost()};
                    final Object result = ((Function) functionObj).call(cx, scope, scope, args);
                    return (String) result;
                } else {
                    LOG.error("FindProxyForURL not found");
                    return null;
                }
            } catch (Exception e) {
                LOG.error(IcedTeaWebConstants.DEFAULT_ERROR_MESSAGE, e);
                return PacConstants.DIRECT;
            } finally {
                Context.exit();
            }
        }
    }

}
