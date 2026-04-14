package com.deobfuscator.api.transformer;

import com.deobfuscator.Context;
import com.deobfuscator.api.inheritance.InheritanceGraph;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;

public abstract class Transformer {
  private Context context;
  private InheritanceGraph inheritanceGraph;
  private int changes;

  public final int run(Context context) {
    this.context = context;
    this.inheritanceGraph = new InheritanceGraph(context);
    this.changes = 0;

    try {
      transform();
    } catch (Exception exception) {
      throw new RuntimeException("Transformer failed: " + getClass().getName(), exception);
    }

    System.out.println("[" + getClass().getSimpleName() + "] changes: " + changes);
    return changes;
  }

  protected abstract void transform() throws Exception;

  protected Context context() {
    return context;
  }

  protected Collection<ClassNode> scopedClasses() {
    return context.classes();
  }

  protected InheritanceGraph inheritanceGraph() {
    return inheritanceGraph;
  }

  protected void markChange() {
    changes++;
  }
}
