package com.github.ryancerf;

import java.nio.file.Path;
import java.util.Objects;

/** DataClass that contains information about where a Snippet Directive was defined. */
class SnippetDirectiveDefinition {
  private final SnippetDirective directive;
  private final int lineNumber;
  private final Path definitionPath;

  private SnippetDirectiveDefinition(
      SnippetDirective directive, int lineNumber, Path definitionPath) {
    this.directive = directive;
    this.lineNumber = lineNumber;
    this.definitionPath = definitionPath;
  }

  static SnippetDirectiveDefinition create(
      SnippetDirective directive, int lineNumber, Path definitionPath) {
    return new SnippetDirectiveDefinition(directive, lineNumber, definitionPath);
  }

  SnippetDirective getDirective() {
    return directive;
  }

  int getLineNumber() {
    return lineNumber;
  }

  Path getDefinitionPath() {
    return definitionPath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SnippetDirectiveDefinition that = (SnippetDirectiveDefinition) o;
    return lineNumber == that.lineNumber
        && Objects.equals(directive, that.directive)
        && Objects.equals(definitionPath, that.definitionPath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(directive, lineNumber, definitionPath);
  }

  @Override
  public String toString() {
    return directive.toString() + "Defined in file: " + definitionPath + " line: " + lineNumber;
  }
}
