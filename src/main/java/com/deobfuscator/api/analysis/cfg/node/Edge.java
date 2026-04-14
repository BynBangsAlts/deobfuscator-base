package com.deobfuscator.api.analysis.cfg.node;

public class Edge<T> {
    private Node<?> src, dst;

    public Edge(Node<T> from, Node<T> to) {
        this.src = from;
        this.dst = to;
    }

    public void setSource(Node<?> src) {
        this.src = src;
    }

    public void setDestination(Node<?> dst) {
        this.dst = dst;
    }

    public Node<?> getSource() {
        return src;
    }

    public Node<?> getDestination() {
        return dst;
    }

    @Override
    public String toString() {
        return src + " -> " + dst;
    }
}
