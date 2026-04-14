package com.deobfuscator.api.asm.matcher;

import com.deobfuscator.api.asm.InsnContext;
import com.deobfuscator.api.asm.MethodContext;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@FunctionalInterface
public interface Match {
  MatchContext matchResult(InsnContext context);

  static Match of(Predicate<InsnContext> predicate) {
    return context -> {
      if (context == null || !predicate.test(context)) {
        return null;
      }

      return MatchContext.of(context);
    };
  }

  default boolean matches(InsnContext context) {
    return matchResult(context) != null;
  }

  default Match and(Match other) {
    return context -> {
      MatchContext left = matchResult(context);
      if (left == null) {
        return null;
      }

      MatchContext right = other.matchResult(context);
      if (right == null) {
        return null;
      }

      return left.merge(right);
    };
  }

  default Match or(Match other) {
    return context -> {
      MatchContext left = matchResult(context);
      if (left != null) {
        return left;
      }

      return other.matchResult(context);
    };
  }

  default Match capture(String name) {
    return context -> {
      MatchContext result = matchResult(context);
      if (result == null) {
        return null;
      }

      return result.withCapture(name, result);
    };
  }

  default List<MatchContext> findAllMatches(MethodContext methodContext) {
    List<MatchContext> matches = new ArrayList<>();
    for (AbstractInsnNode insn : methodContext.methodNode().instructions.toArray()) {
      MatchContext result = matchResult(methodContext.at(insn));
      if (result != null) {
        matches.add(result);
      }
    }
    return matches;
  }
}
