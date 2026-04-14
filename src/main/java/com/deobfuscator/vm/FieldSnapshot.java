package com.deobfuscator.vm;

import java.lang.reflect.Field;

public record FieldSnapshot(Field field, Object value) {
}
