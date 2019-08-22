package com.github.ryancerf;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SourceFileParserTest extends FileSystemTest {

  private SnippetDirectiveDefinition createSnippetDirectiveHelper(String path) {
    return SnippetDirectiveDefinition.create(
        SnippetDirective.create("some_tag", pathFactory.apply(path)),
        1,
        pathFactory.apply("./some/content/file"));
  }

  @Test
  void testClosedSnippet() throws Exception {
    Path srcFile = sourceDirectory.resolve("test.java");
    Files.write(
        srcFile,
        Arrays.asList(
            "1",
            "2",
            "3",
            "//@@ snip_one",
            "5",
            "6",
            "7",
            "// @@ snip_one",
            "9",
            "10",
            "11",
            "//@@ snip_two",
            "13",
            "14",
            "//@@ snip_two",
            "16",
            "17",
            "18",
            "19",
            "// @@ snip_three",
            "21",
            "22",
            "23",
            "24",
            "25",
            "//   @@ end_of_file_snip",
            "27",
            "28"),
        StandardCharsets.UTF_8);

    ParseResult<List<Snippet>> parseResult =
        sourceFileParser.extractSnippetBlocks(createSnippetDirectiveHelper(srcFile.toString()));

    List<Snippet> foundSnippets = parseResult.getResult();

    assertEquals("snip_one", foundSnippets.get(0).getTag());
    assertEquals(Arrays.asList("5", "6", "7"), foundSnippets.get(0).getLines());
    assertEquals(4, foundSnippets.get(0).getLineNumber());

    assertEquals("snip_two", foundSnippets.get(1).getTag());
    assertEquals(Arrays.asList("13", "14"), foundSnippets.get(1).getLines());
    assertEquals(12, foundSnippets.get(1).getLineNumber());

    assertEquals("snip_three", foundSnippets.get(2).getTag());
    assertEquals(Arrays.asList("21", "22", "23", "24", "25"), foundSnippets.get(2).getLines());
    assertEquals(20, foundSnippets.get(2).getLineNumber());

    // Snippet at end of file
    assertEquals("end_of_file_snip", foundSnippets.get(3).getTag());
    assertEquals(Arrays.asList("27", "28"), foundSnippets.get(3).getLines());
    assertEquals(26, foundSnippets.get(3).getLineNumber());
  }

  @Test
  void testDuplicateSnippet() throws Exception {
    Path srcFile = sourceDirectory.resolve("test.java");
    Files.write(
        srcFile,
        Arrays.asList(
            " // @@ snip_one",
            "//@@ snip_two",
            "// @@ snip_one cannot have duplicated snip in same file "),
        StandardCharsets.UTF_8);
    ParseResult<List<Snippet>> result =
        sourceFileParser.extractSnippetBlocks(createSnippetDirectiveHelper(srcFile.toString()));
    assertEquals(1, result.getBuildErrors().size());
    assertThat(
        result.getBuildErrors().get(0).toString().contains("snip_one appeared more than once"));
  }

  @Test
  void testManyFormulationsOfSnippetTag() throws IOException {
    Path srcFile = sourceDirectory.resolve("test.java");
    Files.write(
        srcFile,
        Arrays.asList(
            "//@@snip",
            "//@@  snip",
            "// @@  snip123_0House  GO!!",
            "// @@  !#@snip123_0House  GO!!"),
        StandardCharsets.UTF_8);

    ParseResult<List<Snippet>> parseResult =
        sourceFileParser.extractSnippetBlocks(createSnippetDirectiveHelper(srcFile.toString()));

    List<Snippet> foundSnippets = parseResult.getResult();
    assertEquals(
        Arrays.asList("snip", "snip123_0house"),
        foundSnippets.stream().map(Snippet::getTag).collect(Collectors.toList()));
  }
}
