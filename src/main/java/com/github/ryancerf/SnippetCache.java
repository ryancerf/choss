package com.github.ryancerf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Loads and caches code snippets from source files.
 *
 * <p>Returns errors as data.
 */
class SnippetCache {

  private final SourceFileParser sourceFileParser;

  private final Map<SnippetDirective, Snippet> snippetMap = new HashMap<>();
  private final Set<Path> seenSourcePaths = new HashSet<>();
  private final Set<Path> missingSourcePaths = new HashSet<>();

  SnippetCache(SourceFileParser sourceFileParser) {
    this.sourceFileParser = sourceFileParser;
  }

  Map<SnippetDirective, Snippet> getSnippetMap() {
    return snippetMap;
  }

  /** Fetch the snippet from the source file. */
  ParseResult<Optional<Snippet>> parseOrGetSnippet(SnippetDirectiveDefinition directiveDefinition) {

    Optional<Snippet> optionalSnippet =
        Optional.ofNullable(snippetMap.get(directiveDefinition.getDirective()));

    if (optionalSnippet.isPresent()) {
      return new ParseResult<>(Collections.emptyList(), optionalSnippet);
    }

    // Not in cache but have seen file. SnippetTagNotFoundError.
    if (seenSourcePaths.contains(directiveDefinition.getDirective().getPath())) {
      return new ParseResult<>(
          Collections.singletonList(new BuildError.SnippetTagNotFoundError(directiveDefinition)),
          Optional.empty());
    }

    // Seen and missing.
    if (missingSourcePaths.contains(directiveDefinition.getDirective().getPath())) {
      return new ParseResult<>(
          Collections.singletonList(new BuildError.SnippetFileNotFoundError(directiveDefinition)),
          Optional.empty());
    }

    List<BuildError> buildErrors = parseFileAndCacheSnippets(directiveDefinition);
    if (!missingSourcePaths.contains(directiveDefinition.getDirective().getPath())
        && !snippetMap.containsKey(directiveDefinition.getDirective())) {
      buildErrors = new ArrayList<>(buildErrors);
      buildErrors.add(new BuildError.SnippetTagNotFoundError(directiveDefinition));
    }

    return new ParseResult<>(
        buildErrors, Optional.ofNullable(snippetMap.get(directiveDefinition.getDirective())));
  }

  private List<BuildError> parseFileAndCacheSnippets(
      SnippetDirectiveDefinition directiveDefinition) {
    if (Files.exists(directiveDefinition.getDirective().getPath())) {
      seenSourcePaths.add(directiveDefinition.getDirective().getPath());
    } else {
      missingSourcePaths.add(directiveDefinition.getDirective().getPath());
    }

    ParseResult<List<Snippet>> result = sourceFileParser.extractSnippetBlocks(directiveDefinition);
    for (Snippet snippet : result.getResult()) {
      snippetMap.put(snippet.getDirective(), snippet);
    }
    return result.getBuildErrors();
  }
}
