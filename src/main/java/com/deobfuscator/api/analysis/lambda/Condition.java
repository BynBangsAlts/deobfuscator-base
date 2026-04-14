package com.deobfuscator.api.analysis.lambda;

/**
 * @param <T> input param #1
 */
public interface Condition<T> {
    boolean test(T t);
}