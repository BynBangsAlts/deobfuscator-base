package com.deobfuscator.vm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V21;

public class StubFactory {
  private StubFactory() {
  }


  //TODO improve stub generation
  public static byte[] createStubClass(String className) {
    String internalName = className.replace('.', '/');
    ClassWriter classWriter = new ClassWriter(0);
    classWriter.visit(V21, ACC_PUBLIC | ACC_SUPER, internalName, null, "java/lang/Object", null);
    MethodVisitor init = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    init.visitCode();
    init.visitVarInsn(ALOAD, 0);
    init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    init.visitInsn(RETURN);
    init.visitMaxs(1, 1);
    init.visitEnd();
    classWriter.visitEnd();
    return classWriter.toByteArray();
  }
}
