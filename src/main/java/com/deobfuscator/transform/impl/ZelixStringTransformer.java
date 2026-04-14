package com.deobfuscator.transform.impl;

import com.deobfuscator.api.transformer.Transformer;
import com.deobfuscator.util.InsnHelper;
import com.deobfuscator.vm.Reference;
import com.deobfuscator.vm.VirtualMachine;
import jdk.jfr.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;


@Label("Not a fully working zelix transformer vm is not done !!")
public class ZelixStringTransformer extends Transformer {
  @Override
  protected void transform() {
    VirtualMachine virtualMachine = new VirtualMachine(context());

    scopedClasses().forEach(classNode -> classNode.methods.forEach(methodNode -> {
      boolean changed;
      do {
        changed = transformMethod(classNode.name, methodNode, virtualMachine);
      } while (changed);
    }));
  }

  private boolean transformMethod(String owner, MethodNode methodNode, VirtualMachine virtualMachine) {
    Frame<SourceValue>[] frames;
    try {
      frames = new Analyzer<>(new SourceInterpreter()).analyze(owner, methodNode);
    } catch (AnalyzerException exception) {
      return false;
    }

    var instructions = methodNode.instructions.toArray();
    for (int i = 0; i < instructions.length; i++) {
      var insn = instructions[i];
      if (!(insn instanceof MethodInsnNode methodInsnNode)) continue;
      if (methodInsnNode.getOpcode() != INVOKESTATIC) continue;
      if (!niggaboi(methodInsnNode)) continue;

      Frame<SourceValue> frame = frames[i];
      if (frame == null) continue;

      int cunt = Type.getArgumentTypes(methodInsnNode.desc).length;
      if (frame.getStackSize() < cunt) continue;

      List<Object> arguments = new ArrayList<>(cunt);
      Set<AbstractInsnNode> argumentInstructions = new LinkedHashSet<>();
      boolean valid = true;

      for (int ind = 0; ind < cunt; ind++) {
        int stackIndex = frame.getStackSize() - cunt + ind;
        SourceValue sourceValue = frame.getStack(stackIndex);
        if (sourceValue == null || sourceValue.insns.size() != 1) {
          valid = false;
          break;
        }

        AbstractInsnNode sourceInsn = sourceValue.insns.iterator().next();
        if (!InsnHelper.isInteger(sourceInsn)) {
          valid = false;
          break;
        }

        arguments.add(InsnHelper.getInteger(sourceInsn));
        argumentInstructions.add(sourceInsn);
      }

      if (!valid) continue;

      Object result = virtualMachine.invoke(
          Reference.method(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc),
          arguments
      );
      if (!(result instanceof String string)) continue;

      for (AbstractInsnNode argumentInstruction : argumentInstructions) {
        methodNode.instructions.remove(argumentInstruction);
      }

      methodNode.instructions.set(methodInsnNode, new LdcInsnNode(string));
      markChange();
      return true;
    }

    return false;
  }

  private boolean niggaboi(MethodInsnNode methodInsnNode) {
    Type[] arguments = Type.getArgumentTypes(methodInsnNode.desc);
    if (arguments.length != 2 && arguments.length != 3) {
      return false;
    }

    for (Type argument : arguments) {
      if (!Type.INT_TYPE.equals(argument)) {
        return false;
      }
    }

    return Type.getType(String.class).equals(Type.getReturnType(methodInsnNode.desc));
  }
}
