package com.openwebstart.jvm.localfinder;

import com.openwebstart.jvm.os.OperationSystem;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class WindowsRuntimeFinder extends BaseRuntimeFinder {

    private static final String PROGRAMS_32 = System.getenv("ProgramFiles(X86)") + File.separatorChar;
    private static final String PROGRAM_64 = System.getenv("ProgramFiles") + File.separatorChar;

    private static final String JVM_FOLDER_32 = PROGRAMS_32 + "java";
    private static final String JVM_FOLDER_64 = PROGRAM_64 + "java";

    private static final String CORRETTO_FOLDER_32 = PROGRAMS_32 + "Amazon Corretto";
    private static final String CORRETTO_FOLDER_64 = PROGRAM_64 + "Amazon Corretto";

    private static final String ECLIPSE_FOLDER_32 = PROGRAMS_32 + "Eclipse Foundation";
    private static final String ECLIPSE_FOLDER_64 = PROGRAM_64 + "Eclipse Foundation";

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

    @Override
    Collection<String> getDefaultLocations() {
        return Arrays.asList(
                JVM_FOLDER_32, JVM_FOLDER_64,
                CORRETTO_FOLDER_32, CORRETTO_FOLDER_64,
                ECLIPSE_FOLDER_32, ECLIPSE_FOLDER_64,
                ADOPT_FOLDER_32, ADOPT_FOLDER_64,
                ZULU_FOLDER_32, ZULU_FOLDER_64,
                BELLSOFT_FOLDER_32, BELLSOFT_FOLDER_64,
                SDK_MAN_FOLDER
        );
    }

    @Override
    List<OperationSystem> getSupportedOperationSystems() {
        return Arrays.asList(OperationSystem.WIN32, OperationSystem.WIN64);
    }
}
