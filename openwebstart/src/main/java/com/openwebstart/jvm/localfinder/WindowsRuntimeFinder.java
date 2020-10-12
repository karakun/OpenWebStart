package com.openwebstart.jvm.localfinder;

import com.openwebstart.func.ResultWithInput;
import com.openwebstart.jvm.os.OperationSystem;
import com.openwebstart.jvm.runtimes.LocalJavaRuntime;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

class WindowsRuntimeFinder {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsRuntimeFinder.class);

    private static final String PROGRAMS_32 = System.getenv("ProgramFiles(X86)") + File.separatorChar;
    private static final String PROGRAM_64 = System.getenv("ProgramFiles") + File.separatorChar;

    private static final String JVM_FOLDER_32 = PROGRAMS_32 + "java";
    private static final String JVM_FOLDER_64 = PROGRAM_64 + "java";

    private static final String CORRETTO_FOLDER_32 = PROGRAMS_32 + "Amazon Corretto";
    private static final String CORRETTO_FOLDER_64 = PROGRAM_64 + "Amazon Corretto";

    private static final String ADOPT_FOLDER_32 = PROGRAMS_32 + "AdoptOpenJDK";
    private static final String ADOPT_FOLDER_64 = PROGRAM_64 + "AdoptOpenJDK";

    private static final String ZULU_FOLDER_32 = PROGRAMS_32 + "Zulu";
    private static final String ZULU_FOLDER_64 = PROGRAM_64 + "Zulu";

    private static final String BELLSOFT_FOLDER_32 = PROGRAMS_32 + "BellSoft";
    private static final String BELLSOFT_FOLDER_64 = PROGRAM_64 + "BellSoft";

    // This is based on the assumption that the windows installation and the cygwin installation left the
    // Windows' default user directory and the cygwin home directory pretty much to the defaults
    private static final String CYGWIN_HOME = "cygwin64" + File.separatorChar + "home";
    private static final String CYGWIN_USER_HOME = JavaSystemProperties.getUserHome().replace("Users", CYGWIN_HOME);
    private static final String SDK_MAN_FOLDER = CYGWIN_USER_HOME + File.separatorChar + ".sdkman";

    static List<ResultWithInput<Path, LocalJavaRuntime>> findLocalRuntimes() {
        LOG.debug("Searching for local runtimes");

        return JdkFinder.findLocalJdks(
                Paths.get(JVM_FOLDER_32), Paths.get(JVM_FOLDER_64),
                Paths.get(CORRETTO_FOLDER_32), Paths.get(CORRETTO_FOLDER_64),
                Paths.get(ADOPT_FOLDER_32), Paths.get(ADOPT_FOLDER_64),
                Paths.get(ZULU_FOLDER_32), Paths.get(ZULU_FOLDER_64),
                Paths.get(BELLSOFT_FOLDER_32), Paths.get(BELLSOFT_FOLDER_64),
                Paths.get(SDK_MAN_FOLDER)
        );
    }

    static List<OperationSystem> getSupportedOperationSystems() {
        return Arrays.asList(OperationSystem.WIN32, OperationSystem.WIN64);
    }
}
