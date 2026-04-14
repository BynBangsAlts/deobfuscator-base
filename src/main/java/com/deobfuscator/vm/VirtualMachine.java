package com.deobfuscator.vm;

import com.deobfuscator.Context;
import org.objectweb.asm.Type;
import org.objectweb.asm.Handle;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualMachine {
  private final MemoryClassLoader classLoader;
  private final Map<String, byte[]> classes;
  private final Map<String, byte[]> resources;
  private Path runtimeDir;

  public VirtualMachine(Context context) {
    this.classes = new ClassBytes().toBytes(context);
    this.resources = context.filesMap();
    this.classLoader = new MemoryClassLoader(getClass().getClassLoader(), classes, resources);
  }

  public MemoryClassLoader classLoader() {
    return classLoader;
  }


  public Object invoke(Reference reference, List<Object> arguments) {
    return invoke(reference, Set.of(), Map.of(), arguments);
  }

  public Object invoke(Reference reference, Set<Reference> referencedFields, Map<Reference, KnownValue> state, List<Object> arguments) {
    try {
      Class<?> ownerClass = classLoader.loadClass(reference.owner().replace('/', '.'));
      Class<?>[] parameterTypes = toClasses(Type.getArgumentTypes(reference.descriptor()), classLoader);
      Method method = ownerClass.getDeclaredMethod(reference.name(), parameterTypes);
      if (!Modifier.isStatic(method.getModifiers())) {
        return null;
      }

      List<FieldSnapshot> snapshots = new ArrayList<>();
      try {
        for (Reference fieldReference : referencedFields) {
          Field field = ownerClass.getDeclaredField(fieldReference.name());
          field.setAccessible(true);
          snapshots.add(new FieldSnapshot(field, field.get(null)));

          KnownValue knownValue = state.get(fieldReference);
          if (knownValue == null || !knownValue.known()) {
            return null;
          }

          field.set(null, coerceFieldValue(knownValue.value(), Type.getType(fieldReference.descriptor())));
        }

        method.setAccessible(true);
        Object[] invocationArguments = coerceArguments(arguments, Type.getArgumentTypes(reference.descriptor()));
        return method.invoke(null, invocationArguments);
      } finally {
        for (FieldSnapshot snapshot : snapshots) {
          snapshot.field().setAccessible(true);
          snapshot.field().set(null, snapshot.value());
        }
      }
    } catch (ClassNotFoundException | NoClassDefFoundError exception) {
      return null;
    } catch (InvocationTargetException exception) {
      exception.printStackTrace();
      return null;
    } catch (VerifyError exception) {
      return externalInvoke(reference, arguments);
    } catch (ReflectiveOperationException | LinkageError exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private Object externalInvoke(Reference reference, List<Object> arguments) {
    if (!arguments.stream().allMatch(argument -> argument instanceof Number)) {
      return null;
    }

    try {
      Path dir = ensureRuntimeDir();
      String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
      String classPath = System.getProperty("java.class.path");

      List<String> command = new ArrayList<>();
      command.add(javaExecutable);
      command.add("-noverify");
      command.add("-cp");
      command.add(classPath);
      command.add("com.deobfuscator.vm.InvokeRunner");
      command.add(dir.toString());
      command.add(reference.owner());
      command.add(reference.name());
      command.add(reference.descriptor());
      for (Object argument : arguments) {
        command.add(String.valueOf(((Number) argument).intValue()));
      }

      Process process = new ProcessBuilder(command)
          .start();

      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        System.err.println("[VirtualMachine] External invoke failed:");
        if (!error.isBlank()) {
          System.err.println(error);
        }
        if (!output.isBlank()) {
          System.err.println(output);
        }
        return null;
      }

      String trimmed = output.trim();
      if (trimmed.isEmpty() || "null".equals(trimmed)) {
        return null;
      }

      return trimmed;
    } catch (InterruptedException exception) {
      exception.printStackTrace();
      Thread.currentThread().interrupt();
      return null;
    } catch (IOException exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private ResolvedInvocation externalResolveInvokeDynamic(String name, String descriptor, Handle bootstrapHandle) {
    try {
      Path dir = ensureRuntimeDir();
      String javaExecutable = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
      String classPath = System.getProperty("java.class.path");

      List<String> command = new ArrayList<>();
      command.add(javaExecutable);
      command.add("-noverify");
      command.add("--add-opens");
      command.add("java.base/java.lang.invoke=ALL-UNNAMED");
      command.add("-cp");
      command.add(classPath);
      command.add("com.deobfuscator.vm.ResolveIndyRunner");
      command.add(dir.toString());
      command.add(name);
      command.add(descriptor);
      command.add(String.valueOf(bootstrapHandle.getTag()));
      command.add(bootstrapHandle.getOwner());
      command.add(bootstrapHandle.getName());
      command.add(bootstrapHandle.getDesc());
      command.add(String.valueOf(bootstrapHandle.isInterface()));

      Process process = new ProcessBuilder(command).start();

      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      String error = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        if (!error.isBlank()) {
          System.err.println(error);
        }
        if (!output.isBlank()) {
          System.err.println(output);
        }
        return null;
      }

      String trimmed = output.trim();
      if (trimmed.isEmpty() || "null".equals(trimmed)) {
        return null;
      }

      String decoded = new String(Base64.getDecoder().decode(trimmed), StandardCharsets.UTF_8);
      String[] parts = decoded.split("\t", 5);
      if (parts.length != 5) {
        return null;
      }

      return new ResolvedInvocation(
          Integer.parseInt(parts[0]),
          parts[1],
          parts[2],
          parts[3],
          Boolean.parseBoolean(parts[4])
      );
    } catch (InterruptedException exception) {
      exception.printStackTrace();
      Thread.currentThread().interrupt();
      return null;
    } catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private Path ensureRuntimeDir() throws IOException {
    if (runtimeDir != null) {
      return runtimeDir;
    }

    runtimeDir = Files.createTempDirectory("deobf-vm");
    Path classesDir = runtimeDir.resolve("classes");
    Files.createDirectories(classesDir);
    StringBuilder index = new StringBuilder();
    AtomicInteger counter = new AtomicInteger();

    for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
      String fileName = counter.getAndIncrement() + ".bin";
      Path classFile = classesDir.resolve(fileName);
      Files.write(classFile, entry.getValue());
      index.append(entry.getKey()).append('\t').append(fileName).append('\n');
    }

    Files.writeString(runtimeDir.resolve("classes.idx"), index.toString(), StandardCharsets.UTF_8);

    for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
      Path resourceFile = runtimeDir.resolve(entry.getKey());
      if (resourceFile.getParent() != null) {
        Files.createDirectories(resourceFile.getParent());
      }
      Files.write(resourceFile, entry.getValue());
    }

    return runtimeDir;
  }

  public static Object coerceFieldValue(Object value, Type type) {
    if (Type.BOOLEAN_TYPE.equals(type)) {
      return value instanceof Boolean bool ? bool : ((Number) value).intValue() != 0;
    }
    if (Type.INT_TYPE.equals(type)) {
      return ((Number) value).intValue();
    }
    if (Type.LONG_TYPE.equals(type)) {
      return ((Number) value).longValue();
    }
    if (Type.SHORT_TYPE.equals(type)) {
      return ((Number) value).shortValue();
    }
    if (Type.BYTE_TYPE.equals(type)) {
      return ((Number) value).byteValue();
    }
    if (Type.CHAR_TYPE.equals(type)) {
      return value instanceof Character character ? character : (char) ((Number) value).intValue();
    }
    if (Type.FLOAT_TYPE.equals(type)) {
      return ((Number) value).floatValue();
    }
    if (Type.DOUBLE_TYPE.equals(type)) {
      return ((Number) value).doubleValue();
    }
    return value;
  }

  public static Object[] coerceArguments(List<Object> arguments, Type[] types) {
    Object[] values = new Object[arguments.size()];
    for (int i = 0; i < arguments.size(); i++) {
      Object value = arguments.get(i);
      values[i] = switch (types[i].getSort()) {
        case Type.BOOLEAN -> value instanceof Boolean bool ? bool : ((Number) value).intValue() != 0;
        case Type.INT -> ((Number) value).intValue();
        case Type.LONG -> ((Number) value).longValue();
        case Type.SHORT -> ((Number) value).shortValue();
        case Type.BYTE -> ((Number) value).byteValue();
        case Type.CHAR -> value instanceof Character character ? character : (char) ((Number) value).intValue();
        case Type.FLOAT -> ((Number) value).floatValue();
        case Type.DOUBLE -> ((Number) value).doubleValue();
        default -> value;
      };
    }
    return values;
  }

  public static Class<?>[] toClasses(Type[] types, ClassLoader classLoader) throws ClassNotFoundException {
    Class<?>[] classes = new Class<?>[types.length];
    for (int i = 0; i < types.length; i++) {
      classes[i] = switch (types[i].getSort()) {
        case Type.VOID -> void.class;
        case Type.BOOLEAN -> boolean.class;
        case Type.CHAR -> char.class;
        case Type.BYTE -> byte.class;
        case Type.SHORT -> short.class;
        case Type.INT -> int.class;
        case Type.FLOAT -> float.class;
        case Type.LONG -> long.class;
        case Type.DOUBLE -> double.class;
        case Type.OBJECT -> Class.forName(types[i].getClassName(), true, classLoader);
        case Type.ARRAY -> Class.forName(types[i].getDescriptor().replace('/', '.'), true, classLoader);
        default -> throw new ClassNotFoundException("Unsupported helper type nigga " + types[i]);
      };
    }
    return classes;
  }

  private String encodeArgument(Type type, Object value) {
    return switch (type.getSort()) {
      case Type.OBJECT ->
          "java/lang/String".equals(type.getInternalName())
              ? Base64.getEncoder().encodeToString(String.valueOf(value).getBytes(StandardCharsets.UTF_8))
              : "";
      default -> String.valueOf(value);
    };
  }
}
