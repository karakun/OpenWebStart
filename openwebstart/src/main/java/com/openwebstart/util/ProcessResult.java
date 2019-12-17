package com.openwebstart.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ProcessResult {

    private final int exitValue;

    private final String standardOut;

    private final String errorOut;

    public ProcessResult(final int exitValue, final String standardOut, final String errorOut) {
        this.exitValue = exitValue;
        this.standardOut = standardOut;
        this.errorOut = errorOut;
    }

    public boolean wasSuccessful() {
        return exitValue == 0;
    }

    public boolean wasUnsuccessful() {
        return !wasSuccessful();
    }

    public int getExitValue() {
        return exitValue;
    }

    public String getErrorOut() {
        return errorOut;
    }

    public String getStandardOut() {
        return standardOut;
    }

    public List<String> getStandardOutLines() {
        return getLines(standardOut);
    }

    public List<String> getErrorOutLines() {
        return getLines(errorOut);
    }

    private List<String> getLines(final String src) {
        final List<String> lines = new ArrayList<>();
        final Scanner sc = new Scanner(src);
        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }
        return Collections.unmodifiableList(lines);
    }
}
