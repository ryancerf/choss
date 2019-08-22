package com.github.ryancerf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentFileParserTest extends FileSystemTest {

  private SnippetDirectiveParser snippetDirectiveParser;
  private ContentFileParser contentFileParser;

  @Override
  @BeforeEach
  void setUp() throws IOException {
    super.setUp();
    this.snippetDirectiveParser = new SnippetDirectiveParser(pathFactory);
    this.contentFileParser = new ContentFileParser(snippetDirectiveParser);
  }

  @Test
  void testExtractingSnippetDirectives() throws IOException {

    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(
        contentFile,
        Arrays.asList(
            "",
            "# Chossy",
            "",
            "### Java Example",
            "",
            "```java",
            "@@snip [snip_one](./some/file/path1.java)",
            "```",
            "### Python example",
            "",
            "```python",
            "@@snip [snip_one](./some/file/path1.java)",
            "```",
            "### Ruby example",
            "```ruby",
            "@@snip [snip_two](./some/file/path2.java)",
            "```"));

    assertEquals(
        Arrays.asList(
            SnippetDirectiveDefinition.create(
                SnippetDirective.create("snip_one", pathFactory.apply("./some/file/path1.java")),
                7,
                contentFile),
            SnippetDirectiveDefinition.create(
                SnippetDirective.create("snip_one", pathFactory.apply("./some/file/path1.java")),
                12,
                contentFile),
            SnippetDirectiveDefinition.create(
                SnippetDirective.create("snip_two", pathFactory.apply("./some/file/path2.java")),
                16,
                contentFile)),
        contentFileParser.parse(contentFile).getResult());
  }

  @Test
  void testInvalidIdentifier() throws IOException {
    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(contentFile, Collections.singletonList(" @@snip [&invalid](./some/file/path)"));

    ParseResult<List<SnippetDirectiveDefinition>> result = contentFileParser.parse(contentFile);
    assertEquals(
        Collections.singletonList(
            new BuildError.InvalidIdentifierError(
                SnippetDirectiveDefinition.create(
                    SnippetDirective.create("&invalid", pathFactory.apply("./some/file/path")),
                    1,
                    contentFile))),
        result.getBuildErrors());
  }
}
