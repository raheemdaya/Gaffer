/*
 * Copyright 2016 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gaffer.accumulostore.key.impl.classic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gaffer.accumulostore.key.core.impl.classic.ClassicAccumuloElementConverter;
import gaffer.accumulostore.key.core.impl.classic.ClassicEdgeDirectedUndirectedFilterIterator;
import gaffer.accumulostore.key.exception.AccumuloElementConversionException;
import gaffer.accumulostore.utils.AccumuloStoreConstants;
import gaffer.accumulostore.utils.Pair;
import gaffer.commonutil.TestGroups;
import gaffer.data.element.Edge;
import gaffer.operation.OperationException;
import gaffer.serialisation.implementation.StringSerialiser;
import gaffer.store.schema.Schema;
import gaffer.store.schema.SchemaEdgeDefinition;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.junit.Test;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassicEdgeDirectedUndirectedFilterIteratorTest {
    private static final Schema SCHEMA = new Schema.Builder()
            .edge(TestGroups.EDGE, new SchemaEdgeDefinition.Builder()
                    .source(String.class)
                    .destination(String.class)
                    .directed(Boolean.class)
                    .build())
            .vertexSerialiser(new StringSerialiser())
            .build();

    private static final List<Edge> EDGES = Arrays.asList(
            new Edge(TestGroups.EDGE, "vertexA", "vertexB", true),
            new Edge(TestGroups.EDGE, "vertexD", "vertexC", true),
            new Edge(TestGroups.EDGE, "vertexE", "vertexE", true),
            new Edge(TestGroups.EDGE, "vertexF", "vertexG", false),
            new Edge(TestGroups.EDGE, "vertexH", "vertexH", false)
    );

    private final ClassicAccumuloElementConverter converter = new ClassicAccumuloElementConverter(SCHEMA);

    @Test
    public void shouldOnlyAcceptDeduplicatedEdges() throws OperationException, AccumuloElementConversionException {
        // Given
        final ClassicEdgeDirectedUndirectedFilterIterator filter = new ClassicEdgeDirectedUndirectedFilterIterator();
        final Map<String, String> options = new HashMap<String, String>() {{
            put(AccumuloStoreConstants.OUTGOING_EDGE_ONLY, "true");
            put(AccumuloStoreConstants.DEDUPLICATE_UNDIRECTED_EDGES, "true");
        }};
        filter.validateOptions(options);

        final Value value = null; // value should not be used

        // When / Then
        for (final Edge edge : EDGES) {
            final Pair<Key> keys = converter.getKeysFromEdge(edge);
            // First key is deduplicated
            assertTrue("Failed for edge: " + edge.toString(), filter.accept(keys.getFirst(), value));
            if (null != keys.getSecond()) {
                // self edges are not added the other way round
                assertFalse("Failed for edge: " + edge.toString(), filter.accept(keys.getSecond(), value));
            }
        }
    }

    @Test
    public void shouldOnlyAcceptDeduplicatedDirectedEdges() throws OperationException, AccumuloElementConversionException {
        // Given
        final ClassicEdgeDirectedUndirectedFilterIterator filter = new ClassicEdgeDirectedUndirectedFilterIterator();
        final Map<String, String> options = new HashMap<String, String>() {{
            put(AccumuloStoreConstants.OUTGOING_EDGE_ONLY, "true");
            put(AccumuloStoreConstants.DIRECTED_EDGE_ONLY, "true");
        }};
        filter.validateOptions(options);

        final Value value = null; // value should not be used

        // When / Then
        for (final Edge edge : EDGES) {
            final Pair<Key> keys = converter.getKeysFromEdge(edge);
            // First key is deduplicated
            assertEquals("Failed for edge: " + edge.toString(), edge.isDirected(), filter.accept(keys.getFirst(), value));
            if (null != keys.getSecond()) {
                // self edges are not added the other way round
                assertFalse("Failed for edge: " + edge.toString(), filter.accept(keys.getSecond(), value));
            }
        }
    }

    @Test
    public void shouldOnlyAcceptDeduplicatedUndirectedEdges() throws OperationException, AccumuloElementConversionException {
        // Given
        final ClassicEdgeDirectedUndirectedFilterIterator filter = new ClassicEdgeDirectedUndirectedFilterIterator();
        final Map<String, String> options = new HashMap<String, String>() {{
            put(AccumuloStoreConstants.DEDUPLICATE_UNDIRECTED_EDGES, "true");
            put(AccumuloStoreConstants.UNDIRECTED_EDGE_ONLY, "true");
        }};
        filter.validateOptions(options);

        final Value value = null; // value should not be used

        // When / Then
        for (final Edge edge : EDGES) {
            final Pair<Key> keys = converter.getKeysFromEdge(edge);
            // First key is deduplicated
            assertEquals("Failed for edge: " + edge.toString(), !edge.isDirected(), filter.accept(keys.getFirst(), value));
            if (null != keys.getSecond()) {
                // self edges are not added the other way round
                assertFalse("Failed for edge: " + edge.toString(), filter.accept(keys.getSecond(), value));
            }
        }
    }

    @Test
    public void shouldOnlyAcceptDirectedEdges() throws OperationException, AccumuloElementConversionException {
        // Given
        final ClassicEdgeDirectedUndirectedFilterIterator filter = new ClassicEdgeDirectedUndirectedFilterIterator();
        final Map<String, String> options = new HashMap<String, String>() {{
            put(AccumuloStoreConstants.DIRECTED_EDGE_ONLY, "true");
        }};
        filter.validateOptions(options);

        final Value value = null; // value should not be used

        // When / Then
        for (final Edge edge : EDGES) {
            final boolean expectedResult = edge.isDirected();
            final Pair<Key> keys = converter.getKeysFromEdge(edge);
            assertEquals("Failed for edge: " + edge.toString(), expectedResult, filter.accept(keys.getFirst(), value));
            if (null != keys.getSecond()) {
                // self edges are not added the other way round
                assertEquals("Failed for edge: " + edge.toString(), expectedResult, filter.accept(keys.getSecond(), value));
            }
        }
    }

    @Test
    public void shouldOnlyAcceptUndirectedEdges() throws OperationException, AccumuloElementConversionException {
        // Given
        final ClassicEdgeDirectedUndirectedFilterIterator filter = new ClassicEdgeDirectedUndirectedFilterIterator();
        final Map<String, String> options = new HashMap<String, String>() {{
            put(AccumuloStoreConstants.UNDIRECTED_EDGE_ONLY, "true");
        }};
        filter.validateOptions(options);

        final Value value = null; // value should not be used

        // When / Then
        for (final Edge edge : EDGES) {
            final boolean expectedResult = !edge.isDirected();
            final Pair<Key> keys = converter.getKeysFromEdge(edge);
            assertEquals("Failed for edge: " + edge.toString(), expectedResult, filter.accept(keys.getFirst(), value));
            if (null != keys.getSecond()) {
                // self edges are not added the other way round
                assertEquals("Failed for edge: " + edge.toString(), expectedResult, filter.accept(keys.getSecond(), value));
            }
        }
    }

    @Test
    public void shouldOnlyAcceptIncomingEdges() throws OperationException, AccumuloElementConversionException {
        // Given
        final ClassicEdgeDirectedUndirectedFilterIterator filter = new ClassicEdgeDirectedUndirectedFilterIterator();
        final Map<String, String> options = new HashMap<String, String>() {{
            put(AccumuloStoreConstants.DIRECTED_EDGE_ONLY, "true");
            put(AccumuloStoreConstants.INCOMING_EDGE_ONLY, "true");
        }};
        filter.validateOptions(options);

        final Value value = null; // value should not be used

        // When / Then
        for (final Edge edge : EDGES) {
            final Pair<Key> keys = converter.getKeysFromEdge(edge);
            assertEquals("Failed for edge: " + edge.toString(), false, filter.accept(keys.getFirst(), value));
            if (null != keys.getSecond()) {
                // self edges are not added the other way round
                final boolean expectedResult = edge.isDirected();
                assertEquals("Failed for edge: " + edge.toString(), expectedResult, filter.accept(keys.getSecond(), value));
            }
        }
    }

    @Test
    public void shouldOnlyAcceptOutgoingEdges() throws OperationException, AccumuloElementConversionException {
        // Given
        final ClassicEdgeDirectedUndirectedFilterIterator filter = new ClassicEdgeDirectedUndirectedFilterIterator();
        final Map<String, String> options = new HashMap<String, String>() {{
            put(AccumuloStoreConstants.DIRECTED_EDGE_ONLY, "true");
            put(AccumuloStoreConstants.OUTGOING_EDGE_ONLY, "true");
        }};
        filter.validateOptions(options);

        final Value value = null; // value should not be used

        // When / Then
        for (final Edge edge : EDGES) {
            final Pair<Key> keys = converter.getKeysFromEdge(edge);
            final boolean expectedResult = edge.isDirected();
            assertEquals("Failed for edge: " + edge.toString(), expectedResult, filter.accept(keys.getFirst(), value));
            if (null != keys.getSecond()) {
                // self edges are not added the other way round
                assertEquals("Failed for edge: " + edge.toString(), false, filter.accept(keys.getSecond(), value));
            }
        }
    }
}
