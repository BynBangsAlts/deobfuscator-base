package com.deobfuscator.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class AsmHelper {
  private AsmHelper() {
  }

  public static void updateMethodDescriptor(MethodNode methodNode, String desc) {
    methodNode.desc = desc;
  }

  public static MethodNode findMethod(ClassNode classNode, String name, String desc) {
    for (MethodNode method : classNode.methods) {
      if (method.name.equals(name) && method.desc.equals(desc)) {
        return method;
      }
    }

    return null;
  }

  public static boolean isReturn(AbstractInsnNode insn) {
    int opcode = insn.getOpcode();
    return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
  }

  public static boolean isConstant(AbstractInsnNode insn) {
    return switch (insn.getOpcode()) {
      case Opcodes.ACONST_NULL,
          Opcodes.ICONST_M1,
          Opcodes.ICONST_0,
          Opcodes.ICONST_1,
          Opcodes.ICONST_2,
          Opcodes.ICONST_3,
          Opcodes.ICONST_4,
          Opcodes.ICONST_5,
          Opcodes.LCONST_0,
          Opcodes.LCONST_1,
          Opcodes.FCONST_0,
          Opcodes.FCONST_1,
          Opcodes.FCONST_2,
          Opcodes.DCONST_0,
          Opcodes.DCONST_1,
          Opcodes.BIPUSH,
          Opcodes.SIPUSH,
          Opcodes.LDC -> true;
      default -> false;
    };
  }

  public static InsnList copy(InsnList source) {
    InsnList copy = new InsnList();
    for (AbstractInsnNode insn : source.toArray()) {
      copy.add(insn.clone(null));
    }
    return copy;
  }
}
