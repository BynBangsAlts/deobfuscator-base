package com.deobfuscator.api.asm.matcher.impl;

import com.deobfuscator.api.asm.matcher.Match;

public class OpcodeMatch {
  public static Match of(int opcode) {
    return Match.of(context -> context.opcode() == opcode);
  }
}
