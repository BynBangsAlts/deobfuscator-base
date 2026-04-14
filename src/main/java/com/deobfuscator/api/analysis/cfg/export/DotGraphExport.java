package com.deobfuscator.api.analysis.cfg.export;


import com.deobfuscator.api.analysis.cfg.IGraph;

public class DotGraphExport<T> implements IGraphExporter<T> {
    @Override
    public String export(IGraph<T> graph) {
        var builder = new StringBuilder("digraph g {\n");
        builder.append("\tnode [shape=rect];\n");

        for(var node : graph.getNodes()) {
            builder.append('\n');

            for(var edge : node.getOutgoingEdges()) {
                builder.append('\t')
                        .append(edge.getSource())
                        .append(" -> ")
                        .append(edge.getDestination())
                        .append('\n');
            }
        }

        builder.append("}");
        return builder.toString();
    }
}
