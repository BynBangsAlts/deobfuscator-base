package com.deobfuscator.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class MethodHelper {
  private MethodHelper() {
  }

  public static boolean isStatic(MethodNode methodNode) {
    return (methodNode.access & ACC_STATIC) != 0;
  }

  public static int getFirstParameterIdx(MethodNode methodNode) {
    return isStatic(methodNode) ? 0 : 1;
  }

  public static int getLastParameterIdx(MethodNode methodNode) {
    int index = getFirstParameterIdx(methodNode);
    for (Type argumentType : Type.getArgumentTypes(methodNode.desc)) {
      index += argumentType.getSize();
    }
    return index;
  }

  public static int getMaxLocalFromParameters(MethodNode methodNode) {
    return getLastParameterIdx(methodNode);
  }
}
