package com.openwebstart.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class FilenameUtilTest {

    @ParameterizedTest
    @MethodSource("getNamesThatShouldNotChange")
    public void testValidFileNameWithNormalName(String data) {
        //given
        final String name = data;

        //when
        final String converted = FilenameUtil.toSimplifiedFileName(name);

        //than
        Assertions.assertEquals(name, converted);
    }

    @ParameterizedTest
    @MethodSource("getNamesThatShouldChange")
    public void testValidFileNameWithChange(String data, String expectedResult) {
        //given
        final String name = data;

        //when
        final String converted = FilenameUtil.toSimplifiedFileName(name);

        //than
        Assertions.assertEquals(expectedResult, converted);
    }

    private static Stream<Arguments> getNamesThatShouldNotChange() {
        return Stream.of(Arguments.of("java-12.3.4"),
                Arguments.of("12345678901234567890123456789012345678901234567890"),
                Arguments.of("hello"));
    }

    private static Stream<Arguments> getNamesThatShouldChange() {
        return Stream.of(Arguments.of("a b", "a-b"),
                Arguments.of(" a b ", "a-b"),
                Arguments.of("         a b ", "a-b"),
                Arguments.of(" a              b ", "a-b"),
                Arguments.of(" a b              ", "a-b"),
                Arguments.of("java------12.3.4", "java-12.3.4"),
                Arguments.of("Azul Systems, Inc.-11.0.6", "azul-systems-inc.-11.0.6"),
                Arguments.of("Hello", "hello"));
    }
}