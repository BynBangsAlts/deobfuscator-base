package com.deobfuscator.api.analysis.cfg.export;


import com.deobfuscator.api.analysis.cfg.IGraph;

public interface IGraphExporter<T> {
    String export(IGraph<T> graph);
}
