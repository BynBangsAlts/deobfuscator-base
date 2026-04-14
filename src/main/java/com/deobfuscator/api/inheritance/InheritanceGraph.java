package com.deobfuscator.api.inheritance;

import com.deobfuscator.Context;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

//skidded from recaf
public class InheritanceGraph {
  private final Map<String, InheritanceVertex> vertices = new LinkedHashMap<>();

  public InheritanceGraph(Context context) {
    build(context.classes());
    build(context.librariesMap().values());
  }

  private void build(Collection<ClassNode> classes) {
    for (ClassNode classNode : classes) {
      vertices.put(classNode.name, new InheritanceVertex(this, classNode));
    }
  }

  public Map<String, InheritanceVertex> vertices() {
    return vertices;
  }

  public InheritanceVertex getVertex(String name) {
    return name == null ? null : vertices.get(name);
  }

  public boolean isAssignableFrom(String parent, String child) {
    if (Objects.equals(parent, child)) {
      return true;
    }

    InheritanceVertex childVertex = getVertex(child);
    if (childVertex == null) {
      return false;
    }

    return childVertex.allParents()
        .stream()
        .anyMatch(vertex -> parent.equals(vertex.name()));
  }

  public Set<InheritanceVertex> getFamily(String name) {
    InheritanceVertex vertex = getVertex(name);
    return vertex == null ? Set.of() : vertex.family();
  }

  public String getCommonSuperClass(String first, String second) {
    if (first == null || second == null) {
      return "java/lang/Object";
    }
    if (first.equals(second)) {
      return first;
    }
    if (isAssignableFrom(first, second)) {
      return first;
    }
    if (isAssignableFrom(second, first)) {
      return second;
    }

    InheritanceVertex vertex = getVertex(first);
    while (vertex != null) {
      vertex = vertex.superClass();
      if (vertex != null && isAssignableFrom(vertex.name(), second)) {
        return vertex.name();
      }
    }

    return "java/lang/Object";
  }

  public Type getCommonSuperType(Type first, Type second) {
    if (first == null) return second;
    if (second == null) return first;
    if (first.equals(second)) return first;

    if (first.getSort() != Type.OBJECT && first.getSort() != Type.ARRAY) {
      return Type.getObjectType("java/lang/Object");
    }
    if (second.getSort() != Type.OBJECT && second.getSort() != Type.ARRAY) {
      return Type.getObjectType("java/lang/Object");
    }

    if (first.getSort() == Type.ARRAY || second.getSort() == Type.ARRAY) {
      return Type.getObjectType("java/lang/Object");
    }

    return Type.getObjectType(getCommonSuperClass(first.getInternalName(), second.getInternalName()));
  }
}
