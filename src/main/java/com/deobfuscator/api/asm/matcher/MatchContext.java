package com.deobfuscator.api.asm.matcher;

import com.deobfuscator.api.asm.InsnContext;
import com.deobfuscator.api.asm.MethodContext;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MatchContext {
  private final MethodContext methodContext;
  private final InsnContext insn;
  private final Map<String, MatchContext> captures;
  private final List<AbstractInsnNode> collectedInsns;

  private MatchContext(MethodContext methodContext, InsnContext insn, Map<String, MatchContext> captures, List<AbstractInsnNode> collectedInsns) {
    this.methodContext = methodContext;
    this.insn = insn;
    this.captures = captures;
    this.collectedInsns = collectedInsns;
  }

  public static MatchContext of(InsnContext insn) {
    List<AbstractInsnNode> collectedInsns = new ArrayList<>();
    collectedInsns.add(insn.node());
    return new MatchContext(insn.methodContext(), insn, new LinkedHashMap<>(), collectedInsns);
  }

  public MatchContext merge(MatchContext other) {
    Map<String, MatchContext> mergedCaptures = new LinkedHashMap<>(captures);
    mergedCaptures.putAll(other.captures);

    List<AbstractInsnNode> mergedCollectedInsns = new ArrayList<>(collectedInsns);
    for (AbstractInsnNode insn : other.collectedInsns) {
      if (!mergedCollectedInsns.contains(insn)) {
        mergedCollectedInsns.add(insn);
      }
    }

    return new MatchContext(methodContext, insn, mergedCaptures, mergedCollectedInsns);
  }

  public MatchContext withCapture(String name, MatchContext matchContext) {
    Map<String, MatchContext> updatedCaptures = new LinkedHashMap<>(captures);
    updatedCaptures.put(name, matchContext);
    return new MatchContext(methodContext, insn, updatedCaptures, new ArrayList<>(collectedInsns));
  }

  public MethodContext methodContext() {
    return methodContext;
  }

  public InsnContext insn() {
    return insn;
  }

  public Map<String, MatchContext> captures() {
    return captures;
  }

  public List<AbstractInsnNode> collectedInsns() {
    return collectedInsns;
  }
}
