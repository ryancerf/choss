package com.github.ryancerf;

import java.util.List;
import java.util.Objects;

public class ParseResult<T> {
  private final List<BuildError> buildErrors;
  private final T result;

  ParseResult(List<BuildError> buildErrors, T result) {
    this.buildErrors = buildErrors;
    this.result = result;
  }

  List<BuildError> getBuildErrors() {
    return buildErrors;
  }

  T getResult() {
    return result;
  }

  boolean hasErrors() {
    return !buildErrors.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ParseResult<?> that = (ParseResult<?>) o;
    return Objects.equals(buildErrors, that.buildErrors) && Objects.equals(result, that.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(buildErrors, result);
  }

  @Override
  public String toString() {
    return "ParseResult{" + "buildErrors=" + buildErrors + ", result=" + result + '}';
  }
}
