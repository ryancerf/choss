package com.github.ryancerf;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

class BuildErrorTest extends FileSystemTest {

  @Test
  void duplicateTagError() {
    BuildError buildError =
        new BuildError.DuplicateTagError(
            SnippetDirective.create("my_tag", pathFactory.apply("./docs/src/hello.java")),
            Arrays.asList(1, 2, 3));
    assertThat(buildError.errorLocation()).contains("./docs/src/hello.java lines: 1, 2, 3");
    assertThat(buildError.toString()).contains("my_tag appeared more");
  }

  @Test
  void InvalidIdentifierError() {
    BuildError buildError =
        new BuildError.InvalidIdentifierError(
            SnippetDirectiveDefinition.create(
                SnippetDirective.create("&my_tag", pathFactory.apply("./docs/src/hello.java")),
                1,
                pathFactory.apply("./docs/main/content.md")));
    assertThat(buildError.errorLocation()).contains("./docs/main/content.md line: 1");
    assertThat(buildError.toString())
        .contains("Invalid snippet tag: &my_tag in file: ./docs/main/content.md line: 1.");
  }

  @Test
  void snippetFileNotFoundError() {
    BuildError buildError =
        new BuildError.SnippetFileNotFoundError(
            SnippetDirectiveDefinition.create(
                SnippetDirective.create("my_tag", pathFactory.apply("./docs/src/hello.java")),
                1,
                pathFactory.apply("./docs/main/content.md")));
    assertThat(buildError.errorLocation()).contains("./docs/main/content.md line: 1");
    assertThat(buildError.toString()).contains("Cannot locate source file: ./docs/src/hello.java");
  }

  @Test
  void snippetTagNotFoundError() {
    BuildError buildError =
        new BuildError.SnippetTagNotFoundError(
            SnippetDirectiveDefinition.create(
                SnippetDirective.create("my_tag", pathFactory.apply("./docs/src/hello.java")),
                1,
                pathFactory.apply("./docs/main/content.md")));
    assertThat(buildError.errorLocation()).contains("./docs/main/content.md line: 1");
    assertThat(buildError.toString())
        .contains("Unable to find Snippet Block tagged my_tag In file ./docs/src/hello.java");
  }
}
