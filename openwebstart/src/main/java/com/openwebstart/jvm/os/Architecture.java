package com.openwebstart.jvm.os;

public enum Architecture {

    X86("x86"), X64("x64"), ARM32("arm32"), ARM64("arm64"), AARCH64("aarch64");

    private final String name;

    Architecture(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
