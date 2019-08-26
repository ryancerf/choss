package com.github.ryancerf;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class DocsBuilder {

  // Dependencies. Would use DI Framework, but do not want the dependency.
  private final Supplier<FileSystem> fileSystemSupplier;
  private final Function<String, Path> pathFactory;
  private final ContentFileParser contentFileParser;
  private final SnippetCache snippetCache;
  private final SnippetInjector snippetInjector;

  private final Log log;

  // State.
  private final List<CopyOperation> copyOperations = new ArrayList<>();
  private final List<BuildError> buildErrors = new ArrayList<>();
  private final Set<SnippetDirective> seenSnippetDirectives = new HashSet<>();
  private PathMatcher contentMatcher;

  // Stats to show user.
  private int filesCopiedWithSnippets = 0;
  private int filesCopiedWithoutSnippets = 0;

  DocsBuilder(
      Supplier<FileSystem> fileSystemSupplier,
      Function<String, Path> pathFactory,
      ContentFileParser contentFileParser,
      SnippetCache snippetCache,
      SnippetInjector snippetInjector,
      Log log) {
    this.fileSystemSupplier = fileSystemSupplier;
    this.pathFactory = pathFactory;
    this.contentFileParser = contentFileParser;
    this.snippetCache = snippetCache;
    this.snippetInjector = snippetInjector;
    this.log = log;
  }

  boolean build(String docsSourceDirectory, String docsTargetDirectory, String contentGlob)
      throws IOException {
    this.contentMatcher = fileSystemSupplier.get().getPathMatcher(contentGlob);
    this.log.info(
        "Building Documentation from: " + docsSourceDirectory + " to: " + docsTargetDirectory);
    boolean successful =
        processDirectory(
            pathFactory.apply(docsSourceDirectory), pathFactory.apply(docsTargetDirectory));
    if (successful) {
      warningForUnusedSourceSnippets();
    }
    return successful;
  }

  private boolean processDirectory(Path sourceDirectory, Path targetDirectory) throws IOException {
    if (Files.list(sourceDirectory).count() == 0) {
      throw new IOException("No files in docsSourceDirectory to copy: " + sourceDirectory);
    }
    createOrCleanTargetDirectory(targetDirectory);
    // Walk content directory, resolve snippets, queue copy operations;
    Files.walk(sourceDirectory)
        .forEach(
            src ->
                processContentFile(src, targetDirectory.resolve(sourceDirectory.relativize(src))));

    // All snippets have been resolved. Can show build errors.
    if (!this.buildErrors.isEmpty()) {
      buildErrors.forEach(e -> log.error(e.errorLocation() + " " + e.toString()));
      return false;
    }
    executeCopyOperations();
    log.info("Copied " + filesCopiedWithSnippets + " files with snippets");
    log.info("Copied " + filesCopiedWithoutSnippets + " files without snippets");
    return true;
  }

  private void processContentFile(Path source, Path dest) {
    if (isContentFile(source)) {
      resolveAndCacheSourceSnippets(source, dest);
    } else {
      copyOperations.add(new CopyOperation(source, dest, false));
    }
  }

  private void resolveAndCacheSourceSnippets(Path source, Path destination) {
    ParseResult<List<SnippetDirectiveDefinition>> contentResults = contentFileParser.parse(source);
    buildErrors.addAll(contentResults.getBuildErrors());

    // Resolve all the snippets
    int snippetCount = 0;
    for (SnippetDirectiveDefinition directiveDefinition : contentResults.getResult()) {
      seenSnippetDirectives.add(directiveDefinition.getDirective());
      snippetCount++;
      ParseResult<Optional<Snippet>> snippetResults =
          snippetCache.parseOrGetSnippet(directiveDefinition);
      buildErrors.addAll(snippetResults.getBuildErrors());
    }

    if (snippetCount > 0) {
      copyOperations.add(new CopyOperation(source, destination, true));
    } else {
      copyOperations.add(new CopyOperation(source, destination, false));
    }
  }

  private void executeCopyOperations() throws IOException {
    for (CopyOperation copyOperation : copyOperations) {
      if (copyOperation.injectSnippets) {
        List<String> linesWithSnippets =
            snippetInjector.injectSnippets(copyOperation.source, snippetCache.getSnippetMap());
        Files.write(copyOperation.destination, linesWithSnippets, StandardCharsets.UTF_8);
        this.filesCopiedWithSnippets++;
      } else {
        Files.copy(copyOperation.source, copyOperation.destination, REPLACE_EXISTING);
        if (Files.isRegularFile(copyOperation.source)) {
          filesCopiedWithoutSnippets++;
        }
      }
    }
  }

  private boolean isContentFile(Path path) {
    return contentMatcher.matches(path);
  }

  private void createOrCleanTargetDirectory(Path destination) throws IOException {
    if (Files.exists(destination)) {
      // Delete destination.
      log.info("Emptying directory: " + destination);
      Files.walk(destination)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    } else {
      Files.createDirectories(destination);
    }
  }

  /**
   * Throw a warning if there are any unused snippets in a parsed source file.
   *
   * <p>These warnings are best effort. It does not attempt to find every unused snippet because it
   * is possible for a snippet to be located anywhere in the project.
   */
  private void warningForUnusedSourceSnippets() {
    List<Snippet> unUsedSnippets = new ArrayList<>();
    for (SnippetDirective snippetDirective : snippetCache.getSnippetMap().keySet()) {
      if (!seenSnippetDirectives.contains(snippetDirective)) {
        unUsedSnippets.add(snippetCache.getSnippetMap().get(snippetDirective));
      }
    }

    if (!unUsedSnippets.isEmpty()) {
      log.warn(
          "Ran into unused snippet in parsed source file. Note we do not check every possible"
              + " file for unused snippets");
    }

    for (Snippet unusedSnippet : unUsedSnippets) {
      log.warn(
          "Unused snippet (in source file): "
              + unusedSnippet.getDirective()
              + " line: "
              + unusedSnippet.getLineNumber());
    }
  }

  // Data class
  private static class CopyOperation {
    private final Path source;
    private final Path destination;
    private final boolean injectSnippets;

    CopyOperation(Path source, Path destination, boolean injectSnippets) {
      this.source = source;
      this.destination = destination;
      this.injectSnippets = injectSnippets;
    }
  }
}
