package com.deobfuscator.api.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public record MethodContext(ClassNode classNode, MethodNode methodNode) {
  public static MethodContext of(ClassNode classNode, MethodNode methodNode) {
    return new MethodContext(classNode, methodNode);
  }

  public InsnContext at(AbstractInsnNode insn) {
    return new InsnContext(this, insn);
  }
}
