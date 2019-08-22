package com.github.ryancerf;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SnippetDirectiveParser {
  static final Pattern SNIPPET_DIRECTIV_REGEX =
      Pattern.compile("\\s*@@[s|S]nip\\s*\\[(.+)\\]\\((.+)\\).*");

  private final Function<String, Path> pathFactory;

  SnippetDirectiveParser(Function<String, Path> pathFactory) {
    this.pathFactory = pathFactory;
  }

  Optional<SnippetDirectiveDefinition> createFromLine(
      String line, int lineNumber, Path definitionFilePath) {
    Matcher matcher = SNIPPET_DIRECTIV_REGEX.matcher(line);
    if (matcher.matches()) {
      return Optional.of(
          SnippetDirectiveDefinition.create(
              SnippetDirective.create(matcher.group(1), pathFactory.apply(matcher.group(2))),
              lineNumber,
              definitionFilePath));
    }
    return Optional.empty();
  }
}
