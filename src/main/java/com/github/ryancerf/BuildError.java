package com.github.ryancerf;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

abstract class BuildError {

  abstract String errorLocation();

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(Object other);

  static class DuplicateTagError extends BuildError {
    private final SnippetDirective directive;
    private final List<Integer> lineNumbers;

    DuplicateTagError(SnippetDirective directive, List<Integer> lineNumbers) {
      this.directive = directive;
      this.lineNumbers = lineNumbers;
    }

    @Override
    public String toString() {
      return "Tag: "
          + directive.getTag()
          + " appeared more than once in source file:"
          + directive.getPath()
          + ". Snippet tags must be unique in source files.";
    }

    String errorLocation() {
      return directive.getPath()
          + " lines: "
          + lineNumbers.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DuplicateTagError that = (DuplicateTagError) o;
      return Objects.equals(lineNumbers, that.lineNumbers)
          && Objects.equals(directive, that.directive);
    }

    @Override
    public int hashCode() {
      return Objects.hash(lineNumbers, directive);
    }
  }

  static class InvalidIdentifierError extends BuildError {
    private final SnippetDirectiveDefinition definition;

    InvalidIdentifierError(SnippetDirectiveDefinition definition) {
      this.definition = definition;
    }

    @Override
    public String toString() {
      return "Invalid snippet tag: "
          + definition.getDirective().getTag()
          + " in file: "
          + definition.getDefinitionPath()
          + " line: "
          + definition.getLineNumber()
          + ". Tags must be in the form: "
          + SnippetDirective.VALID_SNIPPET_IDENTIFIER.toString();
    }

    String errorLocation() {
      return definition.getDefinitionPath() + " line: " + definition.getLineNumber();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      InvalidIdentifierError that = (InvalidIdentifierError) o;
      return Objects.equals(definition, that.definition);
    }

    @Override
    public int hashCode() {
      return Objects.hash(definition);
    }
  }

  static class SnippetFileNotFoundError extends BuildError {
    private final SnippetDirectiveDefinition directiveDefinition;

    public SnippetFileNotFoundError(SnippetDirectiveDefinition directiveDefinition) {
      this.directiveDefinition = directiveDefinition;
    }

    @Override
    public String toString() {
      return "Cannot locate source file: " + directiveDefinition.getDirective().getPath();
    }

    @Override
    String errorLocation() {
      return directiveDefinition.getDefinitionPath()
          + " line: "
          + directiveDefinition.getLineNumber();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SnippetFileNotFoundError that = (SnippetFileNotFoundError) o;
      return Objects.equals(directiveDefinition, that.directiveDefinition);
    }

    @Override
    public int hashCode() {
      return Objects.hash(directiveDefinition);
    }
  }

  static class SnippetTagNotFoundError extends BuildError {
    private final SnippetDirectiveDefinition directiveDefinition;

    public SnippetTagNotFoundError(SnippetDirectiveDefinition directiveDefinition) {
      this.directiveDefinition = directiveDefinition;
    }

    @Override
    public String toString() {
      return "Unable to find Snippet Block tagged "
          + directiveDefinition.getDirective().getTag()
          + " In file "
          + directiveDefinition.getDirective().getPath();
    }

    @Override
    String errorLocation() {
      return directiveDefinition.getDefinitionPath()
          + " line: "
          + directiveDefinition.getLineNumber();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SnippetTagNotFoundError that = (SnippetTagNotFoundError) o;
      return Objects.equals(directiveDefinition, that.directiveDefinition);
    }

    @Override
    public int hashCode() {
      return Objects.hash(directiveDefinition);
    }
  }
}
