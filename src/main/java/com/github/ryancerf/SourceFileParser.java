package com.github.ryancerf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a source code file for all snippets.
 *
 * <p>Snippet in the form of: // @@ my_snippetz_tag
 *
 * <p>Throws if the same snippet tag is used more than once in the same file
 */
public class SourceFileParser {
  static final Pattern SNIPPET_TAG_REGEX =
      Pattern.compile("\\s*\\/\\/\\s*@@\\s*([A-Za-z][A-Za-z0-9_]*).*");

  /**
   * Parse a source file for all the snippets in the file not just the snippet contained in the
   * requesting directive.
   *
   * @param directiveDefinition the directive that contains the path to the source.
   * @return a list of all the snippets in the file.
   */
  ParseResult<List<Snippet>> extractSnippetBlocks(SnippetDirectiveDefinition directiveDefinition) {
    return new Parser().extractSnippetBlocks(directiveDefinition);
  }

  private static class Parser {
    final List<Snippet> foundSnippets = new ArrayList<>();
    final List<BuildError> buildErrors = new ArrayList<>();
    final Map<String, ArrayList<Integer>> seenTagLocations = new HashMap<>();

    private ParseResult<List<Snippet>> extractSnippetBlocks(
        SnippetDirectiveDefinition directiveDefinition) {
      final Path sourceFilePath = directiveDefinition.getDirective().getPath();

      if (!Files.exists(sourceFilePath)) {
        return new ParseResult<>(
            Collections.singletonList(new BuildError.SnippetFileNotFoundError(directiveDefinition)),
            new ArrayList<>());
      }

      Scanner scanner = initScanner(sourceFilePath);
      Snippet.Builder currentSnippet = null;

      int lineNumber = 0;
      while (scanner.hasNextLine()) {
        lineNumber++;
        String line = scanner.nextLine();
        Optional<String> snippetTag = parseSnippetIdentifier(line);

        if (!snippetTag.isPresent()) {
          if (currentSnippet != null) {
            currentSnippet.addLine(line);
          }
          continue;
        }
        if (currentSnippet == null) {
          currentSnippet =
              new Snippet.Builder(
                  SnippetDirective.create(snippetTag.get(), sourceFilePath), lineNumber);
        } else {
          addFoundSnippet(currentSnippet.build());
          if (snippetTag.get().equals(currentSnippet.getTag())) {
            currentSnippet = null;
          } else {
            currentSnippet =
                new Snippet.Builder(
                    SnippetDirective.create(snippetTag.get(), sourceFilePath), lineNumber);
          }
        }
        // No active snippet no snippet found do nothing.
      }
      // Reached end of the file. Add a snippet if there is one.
      if (currentSnippet != null) {
        addFoundSnippet(currentSnippet.build());
      }
      return new ParseResult<>(buildErrors, foundSnippets);
    }

    private void addFoundSnippet(Snippet snippet) {
      if (seenTagLocations.containsKey(snippet.getTag())) {
        this.buildErrors.add(
            new BuildError.DuplicateTagError(
                snippet.getDirective(), seenTagLocations.get(snippet.getTag())));
      } else {
        foundSnippets.add(snippet);
      }
      ArrayList<Integer> lines = seenTagLocations.getOrDefault(snippet.getTag(), new ArrayList<>());
      lines.add(snippet.getLineNumber());
      seenTagLocations.put(snippet.getTag(), lines);
    }

    private Optional<String> parseSnippetIdentifier(String line) {
      Matcher matcher = SNIPPET_TAG_REGEX.matcher(line);
      if (matcher.matches()) {
        return Optional.of(matcher.group(1));
      }
      return Optional.empty();
    }

    private Scanner initScanner(Path path) {
      try {
        return new Scanner(path);
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }
}
