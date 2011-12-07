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

import java.util.List;

import org.sonar.duplications.block.Block;

import com.google.common.collect.Lists;

/**
 * Simplifies construction of <a href="http://en.wikipedia.org/wiki/Generalised_suffix_tree">generalised suffix-tree</a>.
 */
public class TextSet extends AbstractText {

  public static class Builder {

    private List<Object> symbols = Lists.newArrayList();
    private List<Integer> sizes = Lists.newArrayList();

    private Builder() {
    }

    public void add(List<Block> list) {
      symbols.addAll(list);
      symbols.add(new Terminator(sizes.size()));
      sizes.add(symbols.size());
    }

    public TextSet build() {
      int[] lens = new int[sizes.size()];
      for (int i = 0; i < sizes.size(); i++) {
        lens[i] = sizes.get(i);
      }
      return new TextSet(symbols, lens);
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  private int[] lens;

  private TextSet(List<Object> symbols, int[] lens) {
    super(symbols);
    this.lens = lens;
  }

  public int[] getLens() {
    return lens;
  }

  @Override
  public Object symbolAt(int index) {
    Object obj = super.symbolAt(index);
    if (obj instanceof Block) {
      return ((Block) obj).getBlockHash();
    }
    return obj;
  }

  public Block getBlock(int index) {
    return (Block) super.symbolAt(index);
  }

  public static class Terminator {

    private final int stringNumber;

    public Terminator(int i) {
      this.stringNumber = i;
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Terminator) && (((Terminator) obj).stringNumber == stringNumber);
    }

    @Override
    public int hashCode() {
      return stringNumber;
    }

    public int getStringNumber() {
      return stringNumber;
    }

    @Override
    public String toString() {
      return "$" + stringNumber;
    }

  }

}
