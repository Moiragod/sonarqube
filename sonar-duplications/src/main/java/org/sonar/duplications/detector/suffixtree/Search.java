/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.duplications.detector.suffixtree;

import java.util.*;

public final class Search {

  private final SuffixTree tree;
  private final int[] lens;
  private final Collector reporter;

  private final List<Integer> list = new ArrayList<Integer>();
  private final List<Node> innerNodes = new ArrayList<Node>();

  public static void perform(TextSet text, Collector reporter) {
    new Search(SuffixTree.create(text), text.getLens(), reporter).compute();
  }

  private Search(SuffixTree tree, int[] lens, Collector reporter) {
    this.tree = tree;
    this.lens = lens;
    this.reporter = reporter;
  }

  private void compute() {
    // O(N)
    computeDepth();

    // O(N * log(N))
    Collections.sort(innerNodes, DEPTH_COMPARATOR);

    // O(N), recursive
    createListOfLeafs(tree.getRootNode());

    // O(N)
    visitInnerNodes();
  }

  private void computeDepth() {
    Queue<Node> queue = new LinkedList<Node>();
    queue.add(tree.getRootNode());
    tree.getRootNode().depth = 0;
    while (!queue.isEmpty()) {
      Node node = queue.remove();
      if (!node.getEdges().isEmpty()) {
        if (node != tree.getRootNode()) { // inner node = not leaf and not root
          innerNodes.add(node);
        }
        for (Edge edge : node.getEdges()) {
          Node endNode = edge.getEndNode();
          endNode.depth = node.depth + edge.getSpan() + 1;
          queue.add(endNode);
        }
      }
    }
  }

  private static final Comparator<Node> DEPTH_COMPARATOR = new Comparator<Node>() {
    public int compare(Node o1, Node o2) {
      return o2.depth - o1.depth;
    }
  };

  private void createListOfLeafs(Node node) {
    node.startSize = list.size();
    if (node.getEdges().isEmpty()) { // leaf
      list.add(node.depth);
    } else {
      for (Edge edge : node.getEdges()) {
        createListOfLeafs(edge.getEndNode());
      }
      node.endSize = list.size();
    }
  }

  /**
   * Each inner-node represents prefix of some suffixes, thus substring of text.
   */
  private void visitInnerNodes() {
    for (Node node : innerNodes) {
      if (containsOrigin(node)) {
        report(node);
      }
    }
  }

  private boolean containsOrigin(Node node) {
    for (int i = node.startSize; i < node.endSize; i++) {
      int start = tree.text.length() - list.get(i);
      int end = start + node.depth;
      if (end < lens[0]) {
        return true;
      }
    }
    return false;
  }

  private void report(Node node) {
    for (int i = node.startSize; i < node.endSize; i++) {
      int start = tree.text.length() - list.get(i);
      int end = start + node.depth;
      reporter.part(start, end, node.depth);
    }
    reporter.endOfGroup();
  }

  public interface Collector {

    /**
     * @param start start position in generalised text
     * @param end end position in generalised text
     */
    void part(int start, int end, int len);

    void endOfGroup();

  }

}
