package com.github.ryancerf;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

@Mojo(name = "build-docs")
public class BuildDocsMojo extends AbstractMojo {

  @Parameter(property = "build-docs.docsSourceDirectory", defaultValue = "./docs/main/")
  private String docsSourceDirectory;

  @Parameter(property = "build-docs.docsTargetDirectory", defaultValue = "./target/docs/main/")
  private String docsTargetDirectory;

  @Parameter(property = "build-docs.contentGlob", defaultValue = "glob:**/*.md")
  private String contentGlob;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    // Do DI manually, Do not want the bloat of a framework.
    Function<String, Path> pathFactory = s -> Paths.get(s);
    SnippetDirectiveParser snippetDirectiveParser = new SnippetDirectiveParser(pathFactory);
    ContentFileParser contentFileParser = new ContentFileParser(snippetDirectiveParser);
    SourceFileParser sourceFileParser = new SourceFileParser();
    SnippetCache snippetCache = new SnippetCache(sourceFileParser);
    IndentationFormatter indentationFormatter = new IndentationFormatter();
    SnippetInjector snippetInjector = new SnippetInjector(snippetDirectiveParser, indentationFormatter);

    DocsBuilder docsBuilder =
        new DocsBuilder(
            FileSystems::getDefault,
            pathFactory,
            contentFileParser,
            snippetCache,
            snippetInjector,
            getLog());

    try {
      boolean success =
          docsBuilder.build(this.docsSourceDirectory, this.docsTargetDirectory, this.contentGlob);
      if (!success) {
        throw new MojoExecutionException("Failed to Build Docs. See errors above for more details");
      }
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
}
