package com.deobfuscator.api.asm.matcher.impl;

import com.deobfuscator.api.asm.matcher.Match;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class MethodMatch {
  public static Match invokeStatic() {
    return Match.of(context -> context.node() instanceof MethodInsnNode methodInsnNode && methodInsnNode.getOpcode() == INVOKESTATIC);
  }

  public static Match invokeVirtual() {
    return Match.of(context -> context.node() instanceof MethodInsnNode methodInsnNode && methodInsnNode.getOpcode() == INVOKEVIRTUAL);
  }
}
