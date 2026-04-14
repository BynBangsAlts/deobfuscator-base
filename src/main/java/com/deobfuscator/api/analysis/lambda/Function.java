package com.deobfuscator.api.analysis.lambda;

/**
 * @param <T> input parameter
 * @param <R> output type
 */
public interface Function<T, R> {
    R evaluate(T t);
}