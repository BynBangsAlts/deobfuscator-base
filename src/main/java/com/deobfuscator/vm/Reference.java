package com.deobfuscator.vm;

public record Reference(String owner, String name, String descriptor) {
  public static Reference field(String owner, String name, String descriptor) {
    return new Reference(owner, name, descriptor);
  }

  public static Reference method(String owner, String name, String descriptor) {
    return new Reference(owner, name, descriptor);
  }
}
