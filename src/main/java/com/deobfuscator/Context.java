package com.deobfuscator;

import com.deobfuscator.api.transformer.Transformer;
import com.deobfuscator.util.ClassHelper;
import com.deobfuscator.vm.SafeClassWriter;
import com.deobfuscator.api.inheritance.InheritanceGraph;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Context {
  public static Context of() {
    return new Context();
  }

  private Path input;
  private Path output;
  private final List<Path> libraries = new ArrayList<>();
  private final List<Transformer> transformers = new ArrayList<>();
  private final Map<String, ClassNode> classes = new LinkedHashMap<>();
  private final Map<String, ClassNode> librariesMap = new LinkedHashMap<>();
  private final Map<String, byte[]> libraryClassBytesMap = new LinkedHashMap<>();
  private final Map<String, byte[]> files = new LinkedHashMap<>();
  private int writerFlags = ClassWriter.COMPUTE_MAXS;

  private Context() {
  }

  public Context input(String path) {
    this.input = Paths.get(path);
    return this;
  }

  public Context output(String path) {
    this.output = Paths.get(path);
    return this;
  }

  public Context libs(String... paths) {
    Arrays.stream(paths).map(Paths::get).forEach(this.libraries::add);
    return this;
  }

  public Context transformers(Transformer... transformers) {
    this.transformers.addAll(Arrays.asList(transformers));
    return this;
  }

  public Context writerFlags(int writerFlags) {
    this.writerFlags = writerFlags;
    return this;
  }

  public Collection<ClassNode> classes() {
    return this.classes.values();
  }

  public Map<String, ClassNode> classesMap() {
    return this.classes;
  }

  public Map<String, byte[]> filesMap() {
    return this.files;
  }

  public Map<String, ClassNode> librariesMap() {
    return librariesMap;
  }

  public Map<String, byte[]> libraryClassBytesMap() {
    return libraryClassBytesMap;
  }

  public ClassNode getClassInfo(String name) {
    for (ClassNode classNode : classes.values()) {
      if (name.equals(classNode.name)) {
        return classNode;
      }
    }

    return librariesMap.get(name);
  }

  public int writerFlags() {
    return writerFlags;
  }

  public List<Path> libraries() {
    return libraries;
  }

  public void execute() {
    if (input == null) {
      throw new IllegalStateException("Input path was not provided");
    }
    if (output == null) {
      throw new IllegalStateException("Output path was not provided");
    }

    loadInput();
    loadLibraries();
    runTransformers();
    saveOutput();
  }

  private void loadInput() {
    if (Files.notExists(input)) {
      throw new IllegalArgumentException("Input jar does not exist: " + input);
    }

    try (InputStream inputStream = Files.newInputStream(input);
         ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        if (entry.isDirectory()) continue;

        byte[] data = zipInputStream.readAllBytes();
        if (entry.getName().endsWith(".class")) {
          ClassReader classReader = new ClassReader(data);
          ClassNode classNode = new ClassNode();
          classReader.accept(classNode, ClassReader.SKIP_DEBUG);
          inlineJSR(classNode);
          classes.put(entry.getName(), classNode);
        } else {
          files.put(entry.getName(), data);
        }
      }
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not load input jar: " + input, exception);
    }
  }

  private void loadLibraries() {
    for (Path library : libraries) {
      for (Path jar : collectJars(library)) {
        try (InputStream inputStream = Files.newInputStream(jar);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
          ZipEntry entry;
          while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.isDirectory()) continue;

            byte[] data = zipInputStream.readAllBytes();
            if (!ClassHelper.isClass(entry.getName(), data)) {
              continue;
            }

            ClassReader classReader = new ClassReader(data);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            librariesMap.putIfAbsent(classNode.name, classNode);
            libraryClassBytesMap.putIfAbsent(classNode.name.replace('/', '.'), data);
          }
        } catch (IOException exception) {
          throw new UncheckedIOException("Could not load library jar: " + jar, exception);
        }
      }
    }
  }

  private void runTransformers() {
    for (Transformer transformer : transformers) {
      transformer.run(this);
    }
  }

  private void saveOutput() {
    InheritanceGraph inheritanceGraph = new InheritanceGraph(this);

    try {
      if (output.getParent() != null) {
        Files.createDirectories(output.getParent());
      }
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not create output directory", exception);
    }

    try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(output))) {
      for (Map.Entry<String, ClassNode> entry : classes.entrySet()) {
        ClassWriter classWriter = new SafeClassWriter(writerFlags, inheritanceGraph);
        entry.getValue().accept(classWriter);
        jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
        jarOutputStream.write(classWriter.toByteArray());
        jarOutputStream.closeEntry();
      }

      for (Map.Entry<String, byte[]> entry : files.entrySet()) {
        jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
        jarOutputStream.write(entry.getValue());
        jarOutputStream.closeEntry();
      }
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not save output jar: " + output, exception);
    }
  }

  private void inlineJSR(ClassNode cn) {
    for (int i = 0; i < cn.methods.size(); i++) {
      MethodNode mn = cn.methods.get(i);
      JSRInlinerAdapter adapter = new JSRInlinerAdapter(
              mn,
              mn.access,
              mn.name,
              mn.desc,
              mn.signature,
              mn.exceptions.toArray(new String[0])
      );
      mn.accept(adapter);
      cn.methods.set(i, adapter);
    }
  }


  public static List<Path> collectJars(Path root) {
    if (root == null || Files.notExists(root)) {
      return List.of();
    }

    try (Stream<Path> stream = Files.isDirectory(root) ? Files.walk(root) : Stream.of(root)) {
      return stream
          .filter(path -> path.toString().endsWith(".jar"))
          .toList();
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not collect libraries from " + root, exception);
    }
  }
}
