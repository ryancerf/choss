package com.github.ryancerf;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

class SnippetInjector {
  private final SnippetDirectiveParser parser;
  private final IndentationFormatter indentationFormatter;

  SnippetInjector(SnippetDirectiveParser parser, IndentationFormatter indentationFormatter) {
    this.parser = parser;
    this.indentationFormatter = indentationFormatter;
  }

  /**
   * Inject snippet into content file. This method will fail loudly if the snippetMap does not
   * contain all the needed snippets to parse the source file.
   *
   * @param contentFile content file with snippet directives.
   * @param snippetMap map of snippets.
   * @return list of lines representing the content file, with the snippets injected.
   */
  List<String> injectSnippets(Path contentFile, Map<SnippetDirective, Snippet> snippetMap) {
    Scanner scanner = initScanner(contentFile);
    List<String> copiedLines = new ArrayList<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      Optional<SnippetDirectiveDefinition> optionalSnippetDirective =
          parser.createFromLine(line, 0, contentFile);
      // Add the snippet instead of the snippet directive.
      if (optionalSnippetDirective.isPresent()) {
        // Snippet should already be in the map, if not, fail loudly.
        Snippet snippet = snippetMap.get(optionalSnippetDirective.get().getDirective());
        copiedLines.addAll(indentationFormatter.normalizeIndentation(snippet.getLines()));
      } else {
        copiedLines.add(line);
      }
    }
    return copiedLines;
  }

  private Scanner initScanner(Path path) {
    try {
      return new Scanner(path);
    } catch (IOException e) {
      // By the time the snippet injector is called, paths should have already been resolved.
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
