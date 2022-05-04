package com.openwebstart.launcher;

import com.install4j.api.launcher.StartupNotification;
import com.install4j.runtime.installer.helper.InstallerUtil;
import com.openwebstart.install4j.Install4JUtils;
import com.openwebstart.proxy.windows.WindowsProxyUtils;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.logging.FileLog;

import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.openwebstart.util.PathQuoteUtil.quoteIfRequired;


/**
 * This launcher resolves OS specific command line argument handling and starts Iced-Tea Web with the correct
 * argument arrangement.
 */
public class OpenWebStartLauncher {

    static {
        // this is placed here above the anything else to ensure no logger has been created prior to this line
        FileLog.setLogFileNamePrefix(FileLog.FILE_LOG_NAME_FORMATTER.format(new Date()));
        FileLog.setLogFileNamePostfix("ows-init");
    }

    private static final Logger LOG = LoggerFactory.getLogger(OpenWebStartLauncher.class);

    static {
        if (InstallerUtil.isWindows()) {
            // Use system proxy in case access to windows registry is blocked
            System.setProperty("java.net.useSystemProxies", "true");
            WindowsProxyUtils.windowsDefaultProxySelector = ProxySelector.getDefault();
        }
    }

    public static void main(String... args) {
        final List<String> arguments = new ArrayList<>(Arrays.asList(args));
        if (!InstallerUtil.isMacOS()) {
            LOG.info("OWS main args {}.", arguments);
            PhaseTwoWebStartLauncher.main(arguments.toArray(new String[0]));
        } else {
            Install4JUtils.applicationVersion().ifPresent(v -> LOG.info("Starting OpenWebStart MacLauncher {}", v));

            StartupNotification.registerStartupListener(parameters -> {
                try {
                    LOG.info("MacOS detected, Launcher needs to add JNLP file name {} to the list of arguments.", parameters);
                    arguments.add(parameters); // add file name at the end
                    LOG.info("OWS main args {}.", arguments);

                    final List<String> commands = new ArrayList<>();
                    commands.add(quoteIfRequired(JavaSystemProperties.getJavaHome() + "/bin/java"));
                    commands.add("-Dapple.awt.UIElement=true");
                    commands.add("-cp");
                    commands.add(JavaSystemProperties.getJavaClassPath());
                    commands.add(PhaseTwoWebStartLauncher.class.getName());
                    commands.addAll(arguments);

                    LOG.info("Starting: " + commands);
                    new ProcessBuilder().command(commands).inheritIO().start();
                } catch (Exception e) {
                    LOG.error("Error in starting JNLP application", e);
                    throw new RuntimeException("Error in starting JNLP application", e);
                }
            });
        }
    }
}
