package com.deobfuscator.vm;

import com.deobfuscator.api.inheritance.InheritanceGraph;
import org.objectweb.asm.ClassWriter;

public class SafeClassWriter extends ClassWriter {
  private final InheritanceGraph inheritanceGraph;

  public SafeClassWriter(int flags, InheritanceGraph inheritanceGraph) {
    super(flags);
    this.inheritanceGraph = inheritanceGraph;
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    return inheritanceGraph.getCommonSuperClass(type1, type2);
  }
}
