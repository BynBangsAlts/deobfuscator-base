package com.deobfuscator.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class InsnHelper {
  private InsnHelper() {
  }

  public static boolean isInteger(AbstractInsnNode insn) {
    if (insn == null) {
      return false;
    }

    if (insn instanceof LdcInsnNode ldcInsnNode) {
      return ldcInsnNode.cst instanceof Integer;
    }

    if (insn instanceof IntInsnNode) {
      return true;
    }

    return switch (insn.getOpcode()) {
      case Opcodes.ICONST_M1,
          Opcodes.ICONST_0,
          Opcodes.ICONST_1,
          Opcodes.ICONST_2,
          Opcodes.ICONST_3,
          Opcodes.ICONST_4,
          Opcodes.ICONST_5 -> true;
      default -> false;
    };
  }

  public static int getInteger(AbstractInsnNode insn) {
    if (insn instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Number number) {
      return number.intValue();
    }

    if (insn instanceof IntInsnNode intInsnNode) {
      return intInsnNode.operand;
    }

    return switch (insn.getOpcode()) {
      case Opcodes.ICONST_M1 -> -1;
      case Opcodes.ICONST_0 -> 0;
      case Opcodes.ICONST_1 -> 1;
      case Opcodes.ICONST_2 -> 2;
      case Opcodes.ICONST_3 -> 3;
      case Opcodes.ICONST_4 -> 4;
      case Opcodes.ICONST_5 -> 5;
      default -> throw new IllegalStateException("Instruction is not an integer push");
    };
  }
}
