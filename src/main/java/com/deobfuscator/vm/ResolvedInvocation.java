package com.deobfuscator.vm;

public record ResolvedInvocation(int opcode, String owner, String name, String descriptor, boolean itf) {
}
