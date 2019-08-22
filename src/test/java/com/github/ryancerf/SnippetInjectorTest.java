package com.github.ryancerf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnippetInjectorTest extends FileSystemTest {

  private SnippetDirectiveParser snippetDirectiveParser;
  private IndentationFormatter indentationFormatter;
  private SnippetInjector snippetInjector;

  @Override
  @BeforeEach
  void setUp() throws IOException {
    super.setUp();
    this.snippetDirectiveParser = new SnippetDirectiveParser(pathFactory);
    this.indentationFormatter = new IndentationFormatter();
    this.snippetInjector = new SnippetInjector(snippetDirectiveParser, indentationFormatter);
  }

  @Test
  void snippetInjector() throws IOException {

    Path contentFile = contentDirectory.resolve("content.md");
    Files.write(
        contentFile,
        Arrays.asList("a", "@@snip [snip_one](./some/file/path)", "b"),
        StandardCharsets.UTF_8);

    Map<SnippetDirective, Snippet> snippetCache = new HashMap<>();
    SnippetDirective directive =
        SnippetDirective.create("snip_one", pathFactory.apply("./some/file/path"));

    // Put a parsed snippet into the cache.
    snippetCache.put(
        directive, new Snippet(directive, 0, Collections.singletonList("InjectedLine")));

    Path path = pathFactory.apply(contentFile.toString());
    List<String> injected = snippetInjector.injectSnippets(path, snippetCache);

    assertEquals(Arrays.asList("a", "InjectedLine", "b"), injected);
  }
}
