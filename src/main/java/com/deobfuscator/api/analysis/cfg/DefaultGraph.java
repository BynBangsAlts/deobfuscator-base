package com.deobfuscator.api.analysis.cfg;


import com.deobfuscator.api.analysis.cfg.collections.ArrayList;
import com.deobfuscator.api.analysis.cfg.node.Edge;
import com.deobfuscator.api.analysis.cfg.node.Node;

public class DefaultGraph<T> implements IGraph<T> {
    private final ArrayList<Node<T>> nodes;

    public DefaultGraph() {
        this.nodes = new ArrayList<>();
    }

    @Override
    public ArrayList<Node<T>> getNodes() {
        return nodes;
    }

    public Node<T> addNode(Node<T> node) {
        this.nodes.add(node);
        return node;
    }

    public Node<T> addNode(T value) {
        return addNode(new Node<>(value));
    }

    @Override
    public void connect(Node<T> first, Node<T> second) {
        var edge = new Edge<>(first, second);

        first.getOutgoingEdges().add(edge);
        second.getIncomingEdges().add(edge);
    }

    @Override
    public void disconnect(Node<T> first, Node<T> second) {
        for (int i = 0; i < first.getOutgoingEdges().size(); i++) {
            var edge = first.getOutgoingEdges().get(i);
            if (edge.getDestination() == second) {
                first.getOutgoingEdges().remove(i);
                break;
            }
        }

        for (int i = 0; i < second.getIncomingEdges().size(); i++) {
            var edge = second.getIncomingEdges().get(i);
            if (edge.getSource() == first) {
                second.getIncomingEdges().remove(i);
                break;
            }
        }
    }

    @Override
    public boolean hasEdge(Node<T> first, Node<T> second) {
        for (var edge : first.getOutgoingEdges()) {
            if (edge.getDestination() == second) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ArrayList<Node<T>> getNeighbors(Node<T> node) {
        var neighbors = new ArrayList<Node<T>>();
        for (var edge : node.getOutgoingEdges()) {
            @SuppressWarnings("unchecked")
            Node<T> dst = (Node<T>) edge.getDestination();
            neighbors.add(dst);
        }
        return neighbors;
    }

    @Override
    public void removeNode(Node<T> node) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) == node) {
                nodes.remove(i);
                break;
            }
        }

        for (var other : nodes) {
            for (int i = 0; i < other.getOutgoingEdges().size(); i++) {
                var edge = other.getOutgoingEdges().get(i);
                if (edge.getDestination() == node) {
                    other.getOutgoingEdges().remove(i);
                    i--;
                }
            }

            for (int i = 0; i < other.getIncomingEdges().size(); i++) {
                var edge = other.getIncomingEdges().get(i);
                if (edge.getSource() == node) {
                    other.getIncomingEdges().remove(i);
                    i--;
                }
            }
        }
    }
}
