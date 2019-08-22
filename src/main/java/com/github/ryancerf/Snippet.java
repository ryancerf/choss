package com.github.ryancerf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A snippet of code from a source file.
 *
 * <p>Ided by the snippet tag and the file's canonical file path.
 */
class Snippet {
  private final SnippetDirective snippetDirective;
  private final int lineNumber;
  private final List<String> lines;

  Snippet(SnippetDirective snippetDirective, int lineNumber, List<String> lines) {
    this.snippetDirective = snippetDirective;
    this.lineNumber = lineNumber;
    this.lines = lines;
  }

  String getTag() {
    return snippetDirective.getTag();
  }

  List<String> getLines() {
    return lines;
  }

  int getLineNumber() {
    return lineNumber;
  }

  SnippetDirective getDirective() {
    return snippetDirective;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Snippet snippet = (Snippet) o;
    return lineNumber == snippet.lineNumber
        && Objects.equals(snippetDirective, snippet.snippetDirective)
        && Objects.equals(lines, snippet.lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(snippetDirective, lineNumber, lines);
  }

  @Override
  public String toString() {
    return "Snippet{" + "snippetDirective=" + snippetDirective + ", lineNumber=" + lineNumber + '}';
  }

  static class Builder {
    private final SnippetDirective snippetDirective;
    private final int lineNumber;
    private final List<String> lines = new ArrayList<>();

    Builder(SnippetDirective snippetDirective, int lineNumber) {
      this.snippetDirective = snippetDirective;
      this.lineNumber = lineNumber;
    }

    void addLine(String line) {
      this.lines.add(line);
    }

    String getTag() {
      return snippetDirective.getTag();
    }

    Snippet build() {
      return new Snippet(snippetDirective, lineNumber, lines);
    }
  }
}
