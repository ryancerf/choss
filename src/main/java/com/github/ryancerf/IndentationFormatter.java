package com.github.ryancerf;

import java.util.List;

class IndentationFormatter {

  /**
   * Normalize Indentation in source code. Finds min indentation in the block and removes that
   * amount from every line.
   */
  List<String> normalizeIndentation(List<String> lines) {
    int sharedIndentation = Integer.MAX_VALUE;
    for (String line : lines) {
      int indentation = 0;
      for (int i = 0; i < line.length(); i++) {
        if (Character.isWhitespace(line.charAt(i))) {
          indentation++;
        } else {
          break;
        }
      }
      if (!line.isEmpty()) {
        sharedIndentation = Math.min(sharedIndentation, indentation);
      }
    }

    if (sharedIndentation > 0) {
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        if (line.isEmpty()) {
          continue;
        }
        String normalizedLine = line.substring(sharedIndentation);
        lines.set(i, normalizedLine);
      }
    }
    return lines;
  }
}
