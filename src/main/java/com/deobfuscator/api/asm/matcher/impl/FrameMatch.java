package com.deobfuscator.api.asm.matcher.impl;

import com.deobfuscator.api.asm.matcher.Match;

public class FrameMatch {
  private FrameMatch() {
  }

  public static Match stack(int index, Match match) {
    return match;
  }

  public static Match stackOriginal(int index, Match match) {
    return match;
  }
}
