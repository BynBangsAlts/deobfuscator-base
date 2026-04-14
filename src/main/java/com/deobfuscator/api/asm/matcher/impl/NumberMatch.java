package com.deobfuscator.api.asm.matcher.impl;

import com.deobfuscator.api.asm.matcher.Match;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class NumberMatch {
  public static Match numInteger() {
    return Match.of(context -> {
      AbstractInsnNode insn = context.node();
      if (insn instanceof LdcInsnNode ldcInsnNode) {
        return ldcInsnNode.cst instanceof Integer;
      }

      if (insn instanceof IntInsnNode) {
        return true;
      }

      return switch (insn.getOpcode()) {
        case org.objectweb.asm.Opcodes.ICONST_M1,
            org.objectweb.asm.Opcodes.ICONST_0,
            org.objectweb.asm.Opcodes.ICONST_1,
            org.objectweb.asm.Opcodes.ICONST_2,
            org.objectweb.asm.Opcodes.ICONST_3,
            org.objectweb.asm.Opcodes.ICONST_4,
            org.objectweb.asm.Opcodes.ICONST_5 -> true;
        default -> false;
      };
    });
  }
}
