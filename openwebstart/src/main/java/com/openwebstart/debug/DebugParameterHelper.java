package com.openwebstart.debug;

/**
 * Helper to create the parameters for remote debugging
 */
public class DebugParameterHelper {

    private static final String YES = "y";
    private static final String NO = "n";
    private static final String REMOTE_DEBUG_COMMAND_PREFIX = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=";
    private static final String SPECIFIC_PORT = ",address=";

    public static String getRemoteDebugParameters(final boolean usingAnyPort, final boolean startSuspended, final String host, final int port) {
        final String remoteDebugCommand = REMOTE_DEBUG_COMMAND_PREFIX + (startSuspended ? YES : NO);
        if (usingAnyPort) {
            return remoteDebugCommand;
        }
        return remoteDebugCommand + SPECIFIC_PORT + host + ":" + port;
    }
}
