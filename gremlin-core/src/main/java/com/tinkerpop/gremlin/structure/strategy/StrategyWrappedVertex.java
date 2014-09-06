package com.tinkerpop.gremlin.structure.strategy;

import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.MetaProperty;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.wrapped.WrappedVertex;
import com.tinkerpop.gremlin.util.StreamFactory;

import java.util.Iterator;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class StrategyWrappedVertex extends StrategyWrappedElement implements Vertex, StrategyWrapped, WrappedVertex<Vertex> {
    private final Vertex baseVertex;
    private final Strategy.Context<StrategyWrappedVertex> strategyContext;
    private final StrategyWrappedVertexIterators iterators;

    public StrategyWrappedVertex(final Vertex baseVertex, final StrategyWrappedGraph strategyWrappedGraph) {
        super(baseVertex, strategyWrappedGraph);
        this.strategyContext = new Strategy.Context<>(strategyWrappedGraph.getBaseGraph(), this);
        this.baseVertex = baseVertex;
        this.iterators = new StrategyWrappedVertexIterators();
    }

    @Override
    public Vertex.Iterators iterators() {
        return this.iterators;
    }

    @Override
    public Vertex getBaseVertex() {
        return this.baseVertex;
    }

    @Override
    public Edge addEdge(final String label, final Vertex inVertex, final Object... keyValues) {
        final Vertex baseInVertex = (inVertex instanceof StrategyWrappedVertex) ? ((StrategyWrappedVertex) inVertex).getBaseVertex() : inVertex;
        return new StrategyWrappedEdge(this.strategyWrappedGraph.strategy().compose(
                s -> s.getAddEdgeStrategy(strategyContext),
                this.baseVertex::addEdge)
                .apply(label, baseInVertex, keyValues), this.strategyWrappedGraph);
    }

    @Override
    public <V> MetaProperty<V> property(final String key, final V value) {
        // todo: explicit vertex/edge strategy methods
        return new StrategyWrappedMetaProperty<V>((MetaProperty<V>) this.strategyWrappedGraph.strategy().compose(
                s -> s.<V>getElementProperty(strategyContext),
                this.baseVertex::property).apply(key, value), this.strategyWrappedGraph);
    }

    @Override
    public <V> MetaProperty<V> property(final String key) {
        // todo: explicit vertex/edge strategy methods
        return new StrategyWrappedMetaProperty<V>((MetaProperty<V>) this.strategyWrappedGraph.strategy().compose(
                s -> s.<V>getElementGetProperty(strategyContext),
                this.baseVertex::property).apply(key), this.strategyWrappedGraph);
    }

    @Override
    public GraphTraversal<Vertex, Vertex> start() {
        return applyStrategy(this.baseVertex.start());
    }

    public class StrategyWrappedVertexIterators extends StrategyWrappedElementIterators implements Vertex.Iterators {
        @Override
        public Iterator<Edge> edges(final Direction direction, final int branchFactor, final String... labels) {
            return new StrategyWrappedEdge.StrategyWrappedEdgeIterator(baseVertex.iterators().edges(direction, branchFactor, labels), strategyWrappedGraph);
        }

        @Override
        public Iterator<Vertex> vertices(final Direction direction, final int branchFactor, final String... labels) {
            return new StrategyWrappedVertexIterator(baseVertex.iterators().vertices(direction, branchFactor, labels), strategyWrappedGraph);
        }

        @Override
        public <V> Iterator<MetaProperty<V>> properties(final String... propertyKeys) {
            // todo: explicit vertex/edge strategy methods
            // todo: wrap it up
            return StreamFactory.stream(strategyWrappedGraph.strategy().compose(
                    s -> s.<V>getElementPropertiesGetter(elementStrategyContext),
                    (String[] pks) -> ((Vertex) baseElement).iterators().properties(pks)).apply(propertyKeys))
                    .map(property -> (MetaProperty<V>) new StrategyWrappedMetaProperty<>((MetaProperty<V>) property, strategyWrappedGraph)).iterator();
        }

        @Override
        public <V> Iterator<MetaProperty<V>> hiddens(final String... propertyKeys) {
            // todo: explicit vertex/edge strategy methods
            // todo: wrap it up
            return StreamFactory.stream(strategyWrappedGraph.strategy().compose(
                    s -> s.<V>getElementHiddens(elementStrategyContext),
                    (String[] pks) -> ((Vertex) baseElement).iterators().hiddens(pks)).apply(propertyKeys))
                    .map(property -> (MetaProperty<V>) new StrategyWrappedMetaProperty<>((MetaProperty<V>) property, strategyWrappedGraph)).iterator();
        }
    }

    public static class StrategyWrappedVertexIterator implements Iterator<Vertex> {
        private final Iterator<Vertex> vertices;
        private final StrategyWrappedGraph strategyWrappedGraph;

        public StrategyWrappedVertexIterator(final Iterator<Vertex> itty,
                                             final StrategyWrappedGraph strategyWrappedGraph) {
            this.vertices = itty;
            this.strategyWrappedGraph = strategyWrappedGraph;
        }

        @Override
        public boolean hasNext() {
            return this.vertices.hasNext();
        }

        @Override
        public Vertex next() {
            return new StrategyWrappedVertex(this.vertices.next(), this.strategyWrappedGraph);
        }
    }
}
