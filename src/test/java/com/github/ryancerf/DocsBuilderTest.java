package com.github.ryancerf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Integration test. */
class DocsBuilderTest extends FileSystemTest {

  private LogMock log;
  private DocsBuilder docsBuilder;

  @Override
  @BeforeEach
  void setUp() throws IOException {
    super.setUp();
    SnippetDirectiveParser snippetDirectiveParser = new SnippetDirectiveParser(pathFactory);
    ContentFileParser contentFileParser = new ContentFileParser(snippetDirectiveParser);
    SourceFileParser sourceFileParser = new SourceFileParser();
    SnippetCache snippetCache = new SnippetCache(sourceFileParser);
    IndentationFormatter indentationFormatter = new IndentationFormatter();
    SnippetInjector snippetInjector = new SnippetInjector(snippetDirectiveParser, indentationFormatter);
    this.log = new LogMock();

    this.docsBuilder =
        new DocsBuilder(
            FileSystems::getDefault,
            pathFactory,
            contentFileParser,
            snippetCache,
            snippetInjector,
            log);
  }

  @Test
  void testSuccessfulParse() throws IOException {
    // Add Source file
    Path srcFile = sourceDirectory.resolve("test.java");
    Files.write(srcFile, Arrays.asList("//@@ snip_one", "code", "//@@ snip_one"));

    // Add Content file.
    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(
        contentFile, java.util.Collections.singletonList(" @@snip [snip_one](" + srcFile + ")"));
    // Add other non content file
    Path otherFile = contentDirectory.resolve("other.yaml");
    Files.write(otherFile, java.util.Collections.singletonList("config"));

    docsBuilder.build(contentDirectory.toString(), "./target/docs/main/", "glob:**/*.md");

    List<String> contentLines =
        Files.lines(targetDirectory.resolve("content.md")).collect(Collectors.toList());
    assertEquals(Collections.singletonList("code"), contentLines);

    List<String> otherLines =
        Files.lines(targetDirectory.resolve("other.yaml")).collect(Collectors.toList());
    assertEquals(Collections.singletonList("config"), otherLines);
  }

  @Test
  void testMissingSnippet() throws IOException {
    Path srcFile = sourceDirectory.resolve("test.java");
    Files.write(srcFile, Arrays.asList("//@@ snip_one", "code", "//@@ snip_one"));

    Path contentFile = contentDirectory.resolve("content.md");
    // snip_two does not match.
    Files.write(
        contentFile, java.util.Collections.singletonList(" @@snip [snip_two](" + srcFile + ")"));

    docsBuilder.build(contentDirectory.toString(), "./target/docs/main/", "glob:**/*.md");
    assertThat(String.join("", log.error)).contains("Unable to find Snippet Block tagged snip_two");
  }

  @Test
  void duplicateTag() throws IOException {
    Path srcFile = sourceDirectory.resolve("test.java");
    Files.write(
        srcFile,
        Arrays.asList(
            "//@@ snip_one", "code", "//@@ snip_one", "//@@ snip_one", "code", "//@@ snip_one"));

    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(
        contentFile, java.util.Collections.singletonList(" @@snip [snip_one](" + srcFile + ")"));

    docsBuilder.build(contentDirectory.toString(), "./target/docs/main/", "glob:**/*.md");
    assertThat(String.join("", log.error)).contains("snip_one appeared more than once");
  }

  @Test
  void missingFile() throws IOException {
    Path srcFile = sourceDirectory.resolve("test.java");
    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(
        contentFile, java.util.Collections.singletonList(" @@snip [snip_one](" + srcFile + ")"));

    docsBuilder.build(contentDirectory.toString(), "./target/docs/main/", "glob:**/*.md");
    assertThat(String.join("", log.error)).contains("Cannot locate source file");
  }

  @Test
  void invalidIdentifier() throws IOException {
    Path srcFile = sourceDirectory.resolve("test.java");
    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(
        contentFile, java.util.Collections.singletonList(" @@snip [&invalid](" + srcFile + ")"));

    docsBuilder.build(contentDirectory.toString(), "./target/docs/main/", "glob:**/*.md");
    assertThat(String.join("", log.error)).contains("Invalid snippet tag:");
  }

  @Test
  void emptySourceDirectory() {
    Throwable thrown =
        assertThrows(
            IOException.class,
            () ->
                docsBuilder.build(
                    contentDirectory.toString(), "./target/docs/main/", "glob:**/*.md"));

    assertThat(thrown.getMessage()).contains("No files in docsSourceDirectory");
  }

  @Test
  void multipleCallsSameSourceFileDifferentSnippet() throws IOException {
    // Add Source file
    Path srcFile = sourceDirectory.resolve("test.java");
    Files.write(srcFile, Arrays.asList("//@@ snip_one", "code", "//@@ snip_one"));

    // Add Content files.
    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(
        contentFile, java.util.Collections.singletonList(" @@snip [snip_one](" + srcFile + ")"));
    Path contentFileTwo = contentDirectory.resolve("contentTwo.md");
    Files.write(
        contentFileTwo, java.util.Collections.singletonList(" @@snip [snip_two](" + srcFile + ")"));

    docsBuilder.build(contentDirectory.toString(), "./target/docs/main/", "glob:**/*.md");

    assertThat(String.join("", log.error)).contains("Unable to find Snippet Block tagged snip_two");
  }

  @Test
  void multipleContentMissingFile() throws IOException {
    Path srcFile = sourceDirectory.resolve("test.java");

    // Add Content files.
    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(
        contentFile, java.util.Collections.singletonList(" @@snip [snip_one](" + srcFile + ")"));
    Path contentFileTwo = contentDirectory.resolve("contentTwo.md");
    Files.write(
        contentFileTwo, java.util.Collections.singletonList(" @@snip [snip_two](" + srcFile + ")"));

    docsBuilder.build(contentDirectory.toString(), "./target/docs/main/", "glob:**/*.md");

    assertEquals(2, log.error.size());
  }

  @Test
  void something() throws IOException {
    // Add Source file
    Path srcFile = sourceDirectory.resolve("test.java");
    Files.write(srcFile, Arrays.asList("//@@ snip_one", "code", "//@@ snip_one"));

    // Add Content file.
    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(contentFile, Arrays.asList("1", " @@snip [snip_one](" + srcFile + ")"));
    Path contentFileTwo = contentDirectory.resolve("content2.md");
    Files.write(contentFileTwo, Arrays.asList("2", " @@snip [snip_one](" + srcFile + ")"));

    docsBuilder.build(contentDirectory.toString(), "./target/docs/main/", "glob:**/*.md");

    List<String> contentLines =
        Files.lines(targetDirectory.resolve("content.md")).collect(Collectors.toList());
    assertEquals(Arrays.asList("1", "code"), contentLines);

    List<String> contentLinesTwo =
        Files.lines(targetDirectory.resolve("content2.md")).collect(Collectors.toList());
    assertEquals(Arrays.asList("2", "code"), contentLinesTwo);
  }
}
