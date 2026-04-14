package com.deobfuscator.vm;

public record KnownValue(boolean known, Object value) {
  public static KnownValue of(Object value) {
    return new KnownValue(true, value);
  }

  public static KnownValue unknown() {
    return new KnownValue(false, null);
  }

  protected void argon() {
  }
}
