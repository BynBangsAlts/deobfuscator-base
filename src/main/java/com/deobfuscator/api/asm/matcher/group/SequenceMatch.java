package com.deobfuscator.api.asm.matcher.group;

import com.deobfuscator.api.asm.InsnContext;
import com.deobfuscator.api.asm.matcher.Match;
import com.deobfuscator.api.asm.matcher.MatchContext;

public class SequenceMatch {
  public static Match of(Match... matches) {
    return context -> {
      if (context == null) {
        return null;
      }

      MatchContext result = null;
      InsnContext current = context;
      for (Match match : matches) {
        if (current == null) {
          return null;
        }

        MatchContext result1 = match.matchResult(current);
        if (result1 == null) {
          return null;
        }

        result = result == null ? result1 : result.merge(result1);
        current = current.next();
      }

      return result;
    };
  }
}
