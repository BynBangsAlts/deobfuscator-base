package com.deobfuscator;

import com.deobfuscator.transform.impl.ZelixStringTransformer;
import org.objectweb.asm.ClassWriter;

public class Main {
  public static void main(String[] args) {
    Context.of()
        // .input("jars/zkm20.jar")
        .input("jars/snake.jar")
        .output("jars/out.jar")
        .transformers(
            new ZelixStringTransformer()
        )
        // .libs("jars/libs/")
        .writerFlags(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
        .execute();
  }
}
