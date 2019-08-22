package com.github.ryancerf;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

abstract class FileSystemTest {

  FileSystem fileSystem;
  Function<String, Path> pathFactory;
  Path sourceDirectory;
  Path contentDirectory;
  Path targetDirectory;
  SourceFileParser sourceFileParser;

  @BeforeEach
  void setUp() throws IOException {
    this.fileSystem = Jimfs.newFileSystem(Configuration.unix());
    this.pathFactory = s -> fileSystem.getPath(s);
    this.contentDirectory = pathFactory.apply("./docs/main/");
    this.sourceDirectory = pathFactory.apply("./docs/src/main/resources");
    this.targetDirectory = pathFactory.apply("./target/docs/main/");
    this.sourceFileParser = new SourceFileParser();

    Files.createDirectories(sourceDirectory);
    Files.createDirectories(contentDirectory);
  }
}
