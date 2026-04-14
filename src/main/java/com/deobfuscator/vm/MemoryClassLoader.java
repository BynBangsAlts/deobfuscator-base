package com.deobfuscator.vm;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MemoryClassLoader extends ClassLoader {
  private final Map<String, byte[]> classes;
  private final Map<String, byte[]> resources;
  private final Set<String> generatedStubs = new HashSet<>();

  public MemoryClassLoader(ClassLoader parent, Map<String, byte[]> classes) {
    this(parent, classes, Map.of());
  }

  public MemoryClassLoader(ClassLoader parent, Map<String, byte[]> classes, Map<String, byte[]> resources) {
    super(parent);
    this.classes = new LinkedHashMap<>(classes);
    this.resources = new LinkedHashMap<>(resources);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      Class<?> loadedClass = findLoadedClass(name);
      if (loadedClass == null) {
        if (classes.containsKey(name)) {
          loadedClass = findClass(name);
        } else {
          try {
            loadedClass = super.loadClass(name, false);
          } catch (ClassNotFoundException exception) {
            loadedClass = findClass(name);
          }
        }
      }

      if (resolve) {
        resolveClass(loadedClass);
      }

      return loadedClass;
    }
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] bytes = classes.get(name);
    if (bytes != null) {
      return defineClass(name, bytes, 0, bytes.length);
    }

    if (shouldGenerateStub(name)) {
      generatedStubs.add(name);
      byte[] stubBytes = StubFactory.createStubClass(name);
      classes.put(name, stubBytes);
     // System.err.println("[MemoryClassLoader] Generated new stub : " + name);
      return defineClass(name, stubBytes, 0, stubBytes.length);
    }

    throw new ClassNotFoundException(name);
  }

  public byte[] getResourceBytes(String name) {
    return resources.get(name);
  }

  private boolean shouldGenerateStub(String name) {
    if (name == null) {
      return false;
    }
    if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.") || name.startsWith("sun.")) {
      return false;
    }
    return !generatedStubs.contains(name);
  }
}
