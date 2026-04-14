package com.deobfuscator.api.inheritance;

import org.objectweb.asm.tree.ClassNode;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

//skidded from recaf
public class InheritanceVertex {
  private final InheritanceGraph graph;
  private final ClassNode classNode;

  public InheritanceVertex(InheritanceGraph graph, ClassNode classNode) {
    this.graph = graph;
    this.classNode = classNode;
  }

  public String name() {
    return classNode.name;
  }

  public String superName() {
    return classNode.superName;
  }

  public List<String> interfaces() {
    return classNode.interfaces == null ? List.of() : classNode.interfaces;
  }

  public ClassNode classNode() {
    return classNode;
  }

  public boolean isJavaLangObject() {
    return Objects.equals("java/lang/Object", name());
  }

  public InheritanceVertex superClass() {
    return graph.getVertex(superName());
  }

  public Set<InheritanceVertex> directParents() {
    Set<InheritanceVertex> parents = new LinkedHashSet<>();

    InheritanceVertex superClass = superClass();
    if (superClass != null) {
      parents.add(superClass);
    }

    for (String itf : interfaces()) {
      InheritanceVertex vertex = graph.getVertex(itf);
      if (vertex != null) {
        parents.add(vertex);
      }
    }

    return parents;
  }

  public Set<InheritanceVertex> allParents() {
    Set<InheritanceVertex> parents = new LinkedHashSet<>();
    visitParents(this, parents);
    parents.remove(this);
    return parents;
  }

  public Set<InheritanceVertex> directChildren() {
    Set<InheritanceVertex> children = new LinkedHashSet<>();
    for (InheritanceVertex vertex : graph.vertices().values()) {
      if (vertex == this) continue;
      if (name().equals(vertex.superName()) || vertex.interfaces().contains(name())) {
        children.add(vertex);
      }
    }
    return children;
  }

  public Set<InheritanceVertex> allChildren() {
    Set<InheritanceVertex> children = new LinkedHashSet<>();
    visitChildren(this, children);
    children.remove(this);
    return children;
  }

  public Set<InheritanceVertex> family() {
    Set<InheritanceVertex> family = new LinkedHashSet<>();
    visitFamily(this, family);
    return family;
  }

  private void visitParents(InheritanceVertex current, Set<InheritanceVertex> vertices) {
    if (!vertices.add(current)) {
      return;
    }

    for (InheritanceVertex parent : current.directParents()) {
      visitParents(parent, vertices);
    }
  }

  private void visitChildren(InheritanceVertex current, Set<InheritanceVertex> vertices) {
    if (!vertices.add(current)) {
      return;
    }

    for (InheritanceVertex child : current.directChildren()) {
      visitChildren(child, vertices);
    }
  }

  private void visitFamily(InheritanceVertex current, Set<InheritanceVertex> vertices) {
    if (!vertices.add(current)) {
      return;
    }

    for (InheritanceVertex vertex : current.directParents()) {
      visitFamily(vertex, vertices);
    }

    for (InheritanceVertex vertex : current.directChildren()) {
      visitFamily(vertex, vertices);
    }
  }
}
