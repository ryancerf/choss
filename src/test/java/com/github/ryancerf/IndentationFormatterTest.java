package com.github.ryancerf;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndentationFormatterTest {

  @Test
  void noIndentation() {
    IndentationFormatter indentationFormatter = new IndentationFormatter();
    List<String> input = Arrays.asList("a", "b", "", "c");
    List<String> expected = Arrays.asList("a", "b", "", "c");
    assertEquals(expected, indentationFormatter.normalizeIndentation(input));
  }

  @Test
  void indentationShared() {
    IndentationFormatter indentationFormatter = new IndentationFormatter();
    List<String> input = Arrays.asList("  a", " b", "  c");
    List<String> expected = Arrays.asList(" a", "b", " c");
    assertEquals(expected, indentationFormatter.normalizeIndentation(input));
  }

  @Test
  void indentationSharedWithBlankLines() {
    IndentationFormatter indentationFormatter = new IndentationFormatter();
    List<String> input = Arrays.asList("  a", " b", "  c", "");
    List<String> expected = Arrays.asList(" a", "b", " c", "");
    assertEquals(expected, indentationFormatter.normalizeIndentation(input));
  }
}
