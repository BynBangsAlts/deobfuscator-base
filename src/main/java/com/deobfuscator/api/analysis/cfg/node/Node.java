package com.deobfuscator.api.analysis.cfg.node;


import com.deobfuscator.api.analysis.cfg.collections.ArrayList;

public class Node<T> {
    private T value;
    private final ArrayList<Edge<T>> incomingEdges, outgoingEdges;

    public Node(T value) {
        this.value = value;

        this.incomingEdges = new ArrayList<>();
        this.outgoingEdges = new ArrayList<>();
    }

    public ArrayList<Edge<T>> getIncomingEdges() {
        return incomingEdges;
    }

    public ArrayList<Edge<T>> getOutgoingEdges() {
        return outgoingEdges;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
