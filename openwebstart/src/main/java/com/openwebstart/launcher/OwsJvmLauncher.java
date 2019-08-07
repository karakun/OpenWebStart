package com.openwebstart.launcher;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import net.adoptopenjdk.icedteaweb.JvmPropertyConstants;
import net.adoptopenjdk.icedteaweb.StreamUtils;
import net.adoptopenjdk.icedteaweb.jvm.JvmUtils;
import net.adoptopenjdk.icedteaweb.launch.JvmLauncher;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.runtime.Boot;

import static com.openwebstart.util.PathQuoteUtil.quoteIfRequired;

/**
 * ...
 */
class OwsJvmLauncher implements JvmLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(OwsJvmLauncher.class);

    @Override
    public void launchExternal(JNLPFile jnlpFile, List<String> args) throws Exception {
        final String javaBinary = "/bin/java".replace('/', File.separatorChar);
        final String pathToJavaBinary = System.getProperty(JvmPropertyConstants.JAVA_HOME) + javaBinary;
        final String pathToJavaws = JvmUtils.getPathToJavawsJar();
        launchExternal(pathToJavaBinary, pathToJavaws, jnlpFile.getNewVMArgs(), args);
    }

    /**
     * @param pathToJavaBinary path to the java binary of the JRE in which to start OWS
     * @param pathToJavawsJar  path to the javaws.jar included in OWS
     * @param vmArgs           the arguments to pass to the jvm
     * @param javawsArgs       the arguments to pass to javaws (aka IcedTea-Web)
     */
    private void launchExternal(String pathToJavaBinary, String pathToJavawsJar, List<String> vmArgs, List<String> javawsArgs) throws Exception {
        final List<String> commands = new LinkedList<>();

        commands.add(quoteIfRequired(pathToJavaBinary));
        commands.add("-cp");
        commands.add(quoteIfRequired(pathToJavawsJar));

        commands.addAll(vmArgs);
        commands.add(Boot.class.getName());
        commands.addAll(javawsArgs);

        LOG.info("About to launch external with commands: '{}'", commands.toString());

        final Process p = new ProcessBuilder()
                .command(commands)
                .inheritIO()
                .start();

        StreamUtils.waitForSafely(p);
    }

}
