package com.deobfuscator.api.analysis.cfg;


import com.deobfuscator.api.analysis.cfg.collections.ArrayList;
import com.deobfuscator.api.analysis.cfg.node.Node;

public interface IGraph<T> {
    ArrayList<Node<T>> getNodes();

    void connect(Node<T> first, Node<T> second);

    void disconnect(Node<T> first, Node<T> second);

    boolean hasEdge(Node<T> first, Node<T> second);

    ArrayList<Node<T>> getNeighbors(Node<T> node);

    void removeNode(Node<T> node);
}
