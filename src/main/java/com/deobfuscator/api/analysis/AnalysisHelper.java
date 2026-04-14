package com.deobfuscator.api.analysis;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.Value;

public class AnalysisHelper {
  private AnalysisHelper() {
  }

  public static <V extends Value> Frame<V>[] analyze(String owner, MethodNode methodNode, Interpreter<V> interpreter) throws AnalyzerException {
    Analyzer<V> analyzer = new Analyzer<>(interpreter);
    return analyzer.analyze(owner, methodNode);
  }
}
