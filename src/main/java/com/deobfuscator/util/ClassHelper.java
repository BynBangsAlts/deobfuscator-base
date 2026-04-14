package com.deobfuscator.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class ClassHelper {
  private ClassHelper() {
  }

  public static boolean isClass(String path, byte[] data) {
    if (path == null || data == null || data.length < 4) {
      return false;
    }

    return path.endsWith(".class")
        && data[0] == (byte) 0xCA
        && data[1] == (byte) 0xFE
        && data[2] == (byte) 0xBA
        && data[3] == (byte) 0xBE;
  }

  public static ClassNode read(byte[] bytes, int flags) {
    ClassReader classReader = new ClassReader(bytes);
    ClassNode classNode = new ClassNode();
    classReader.accept(classNode, flags);
    return classNode;
  }
}
