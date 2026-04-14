package com.deobfuscator.vm;

import com.deobfuscator.Context;
import com.deobfuscator.api.inheritance.InheritanceGraph;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClassBytes {
  public Map<String, byte[]> toBytes(Context context) {
    Map<String, byte[]> classes = new LinkedHashMap<>();
    InheritanceGraph inheritanceGraph = new InheritanceGraph(context);

    for (ClassNode classNode : context.classes()) {
      ClassWriter classWriter = new SafeClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, inheritanceGraph);
      classNode.accept(classWriter);
      classes.put(classNode.name.replace('/', '.'), classWriter.toByteArray());
    }

    classes.putAll(context.libraryClassBytesMap());
    return classes;
  }
}
