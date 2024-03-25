package com.example.sdk.internal.common;

public class ExampleJavaClass {
    private int getNegativeNumber() {
        return -1;
    }

    private static int getConstantNumber() {
        return 1;
    }

    private static void staticThrowUncheckedException() {
        throw new IllegalStateException();
    }

    private static void staticThrowCheckedException() throws Exception {
        throw new IllegalAccessException();
    }

    private static void staticThrowError() {
        throw new OutOfMemoryError();
    }
}
