package com.github.ryancerf;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** Parses content files (.md) looking for snippet directives. */
class ContentFileParser {
  private final SnippetDirectiveParser snippetDirectiveParser;

  ContentFileParser(SnippetDirectiveParser snippetDirectiveParser) {
    this.snippetDirectiveParser = snippetDirectiveParser;
  }

  /**
   * Parse a content file for snippet directives. Return errors as data.
   *
   * @param path the content file to parse.
   * @return A result of errors and snippet dir
   */
  ParseResult<List<SnippetDirectiveDefinition>> parse(Path path) {
    List<SnippetDirectiveDefinition> directives = new ArrayList<>();
    List<BuildError> errors = new ArrayList<>();
    Scanner scanner = initScanner(path);
    int lineNumber = 0;
    while (scanner.hasNextLine()) {
      lineNumber++;
      String line = scanner.nextLine();
      Optional<SnippetDirectiveDefinition> optionalSnippetDirective =
          snippetDirectiveParser.createFromLine(line, lineNumber, path);
      if (optionalSnippetDirective.isPresent()) {
        if (!isValidIdentifier(optionalSnippetDirective.get().getDirective().getTag())) {
          errors.add(new BuildError.InvalidIdentifierError(optionalSnippetDirective.get()));
        } else {
          directives.add(optionalSnippetDirective.get());
        }
      }
    }
    return new ParseResult<>(errors, directives);
  }

  private boolean isValidIdentifier(String tag) {
    return SnippetDirective.VALID_SNIPPET_IDENTIFIER.matcher(tag).matches();
  }

  private Scanner initScanner(Path path) {
    try {
      return new Scanner(path);
    } catch (IOException e) {
      // By the Content file parser is called the path should already be resolved.
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
