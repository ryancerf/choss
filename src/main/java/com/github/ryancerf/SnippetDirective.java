package com.github.ryancerf;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A directive in a content that will be replaced by an actual snippet of code from a source file.
 *
 * <p>: @@snip [snip_tag](path_to_file)
 *
 * <p>: @@snip [drop_duplicate_rows_snippet](/docs/src/main/core/table.java)
 */
class SnippetDirective {

  static final Pattern VALID_SNIPPET_IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

  private final String tag;
  private final Path path;

  private SnippetDirective(String tag, Path path) {
    this.tag = tag.toLowerCase();
    this.path = path;
  }

  static SnippetDirective create(String tag, Path path) {
    return new SnippetDirective(tag, path);
  }

  String getTag() {
    return tag;
  }

  public Path getPath() {
    return path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SnippetDirective that = (SnippetDirective) o;
    return Objects.equals(tag, that.tag) && Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tag, path);
  }

  @Override
  public String toString() {
    return "@@snip [" + tag + "](" + path + ")";
  }
}
