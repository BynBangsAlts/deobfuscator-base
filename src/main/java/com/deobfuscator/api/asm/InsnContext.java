package com.deobfuscator.api.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public record InsnContext(MethodContext methodContext, AbstractInsnNode node) {
  public AbstractInsnNode node() {
    return node;
  }

  public MethodNode methodNode() {
    return methodContext.methodNode();
  }

  public int opcode() {
    return node.getOpcode();
  }

  public boolean isVarStore() {
    return node instanceof VarInsnNode varInsnNode && switch (varInsnNode.getOpcode()) {
      case org.objectweb.asm.Opcodes.ISTORE,
          org.objectweb.asm.Opcodes.LSTORE,
          org.objectweb.asm.Opcodes.FSTORE,
          org.objectweb.asm.Opcodes.DSTORE,
          org.objectweb.asm.Opcodes.ASTORE -> true;
      default -> false;
    };
  }

  public int asInteger() {
    if (node instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst instanceof Number number) {
      return number.intValue();
    }
    if (node instanceof IntInsnNode intInsnNode) {
      return intInsnNode.operand;
    }

    return switch (node.getOpcode()) {
      case org.objectweb.asm.Opcodes.ICONST_M1 -> -1;
      case org.objectweb.asm.Opcodes.ICONST_0 -> 0;
      case org.objectweb.asm.Opcodes.ICONST_1 -> 1;
      case org.objectweb.asm.Opcodes.ICONST_2 -> 2;
      case org.objectweb.asm.Opcodes.ICONST_3 -> 3;
      case org.objectweb.asm.Opcodes.ICONST_4 -> 4;
      case org.objectweb.asm.Opcodes.ICONST_5 -> 5;
      default -> throw new IllegalStateException("Instruction is not an integer constant: " + node.getOpcode());
    };
  }

  public Type type() {
    return Type.getType(node.getClass().descriptorString());
  }

  public InsnContext next() {
    AbstractInsnNode next = node.getNext();
    return next == null ? null : methodContext.at(next);
  }
}
