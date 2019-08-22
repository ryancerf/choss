package com.github.ryancerf;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class SnippetDirectiveTest {

  @Test
  void testToString() {
    assertEquals("@@snip [houser](./a/path)",
        SnippetDirective.create("Houser", Paths.get("./a/path")).toString());

  }
}