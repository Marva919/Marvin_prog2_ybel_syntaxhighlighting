package highlighting.presets;

import static org.junit.jupiter.api.Assertions.*;

import highlighting.core.HighlightRegion;
import highlighting.regex.Token;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive JUnit 5 tests for {@link MiniJavaTokens#defaultTokens()}.
 *
 * <p>Token indices in defaultTokens(): 0 – Javadoc comment /** ... * / 1 – Block comment /* ... * /
 * 2 – Line comment // ... 3 – String literal "..." 4 – Char literal '.' 5 – Annotation @Identifier
 * 6 – Keywords
 */
public class MiniJavaTokensTest {

  // -------------------------------------------------------------------------
  // Helper - Methods
  // -------------------------------------------------------------------------
  private static List<HighlightRegion> match(int tokenIndex, String text) {
    return MiniJavaTokens.defaultTokens().get(tokenIndex).test(text);
  }

  private static HighlightRegion singleMatch(int tokenIndex, String text) {
    List<HighlightRegion> regions = match(tokenIndex, text);
    assertEquals(1, regions.size());
    return regions.get(0);
  }

  // =========================================================================
  // Index 0 – Javadoc comments
  // =========================================================================

  @Test
  void javadoc_simple_match() {
    String text = "/** This is javadoc */";
    HighlightRegion r = singleMatch(0, text);
    assertEquals(MiniJavaColours.JAVADOC_COMMENT_COLOUR, r.colour());
  }

  @Test
  void javadoc_simple_match_covers_whole_text() {
    String text = "/** This is javadoc */";
    HighlightRegion r = singleMatch(0, text);
    assertEquals(text.length(), r.end());
  }

  @Test
  void javadoc_multi_line() {
    String text = "/**\n * @param x value\n * @return result\n */";
    HighlightRegion r = singleMatch(0, text);
    assertEquals(text.length(), r.end());
  }

  @Test
  void javadoc_in_middle_of_text() {
    String text = "int x; /** doc */ int y;";
    HighlightRegion r = singleMatch(0, text);
    assertEquals(7, r.start());
  }

  @Test
  void javadoc_multiple_in_text() {
    String text = "/** one */ class Foo { /** two */ }";
    assertEquals(2, match(0, text).size());
  }

  @Test
  void javadoc_no_match_block_comment() {
    assertTrue(match(0, "/* not javadoc */").isEmpty());
  }

  @Test
  void javadoc_at_end_of_text() {
    String text = "class Foo { }/** trailing */";
    assertEquals(13, singleMatch(0, text).start());
  }

  // =========================================================================
  // Index 1 – Block comments  /* ... */
  // =========================================================================

  @Test
  void block_comment_simple_match() {
    String text = "/* a block comment */";
    HighlightRegion r = singleMatch(1, text);
    assertEquals(MiniJavaColours.BLOCK_COMMENT_COLOUR, r.colour());
  }

  @Test
  void block_comment_simple_match_covers_whole_text() {
    String text = "/* a block comment */";
    HighlightRegion r = singleMatch(1, text);
    assertEquals(text.length(), r.end());
  }

  @Test
  void block_comment_multi_line() {
    assertEquals(1, match(1, "/*\n  line one\n  line two\n*/").size());
  }

  @Test
  void block_comment_contains_keyword() {
    String text = "/* public class return */";
    HighlightRegion r = singleMatch(1, text);
    assertEquals(text.length(), r.end());
  }

  @Test
  void block_comment_contains_string_like_syntax() {
    assertEquals(1, match(1, "/* contains \"string\" inside */").size());
  }

  @Test
  void block_comment_no_match() {
    assertTrue(match(1, "int x = 42;").isEmpty());
  }

  @Test
  void block_comment_at_start_of_text() {
    String text = "/* start */ int x;";
    HighlightRegion r = singleMatch(1, text);
    assertEquals(11, r.end());
  }

  // =========================================================================
  // Index 2 – Line comments  // ...
  // =========================================================================

  @Test
  void line_comment_simple_match() {
    String text = "// this is a comment";
    HighlightRegion r = singleMatch(2, text);
    assertEquals(MiniJavaColours.LINE_COMMENT_COLOUR, r.colour());
  }

  @Test
  void line_comment_simple_match_covers_whole_text() {
    String text = "// this is a comment";
    assertEquals(text.length(), singleMatch(2, text).end());
  }

  @Test
  void line_comment_after_code() {
    assertEquals(11, singleMatch(2, "int x = 5; // set x").start());
  }

  @Test
  void line_comment_multiple_lines() {
    assertEquals(3, match(2, "// first\n// second\n// third").size());
  }

  @Test
  void line_comment_contains_block_comment_syntax() {
    String text = "// ignore /* this */ and that";
    HighlightRegion r = singleMatch(2, text);
    assertEquals(text.length(), r.end());
  }

  @Test
  void line_comment_no_match() {
    assertTrue(match(2, "int x = 5;").isEmpty());
  }

  @Test
  void line_comment_at_end_of_text() {
    String text = "return 0; // end";
    HighlightRegion r = singleMatch(2, text);
    assertEquals(10, r.start());
  }

  // =========================================================================
  // Index 3 – String literals  "..."
  // =========================================================================

  @Test
  void string_simple_match() {
    String text = "\"hello world\"";
    HighlightRegion r = singleMatch(3, text);
    assertEquals(MiniJavaColours.STRING_LITERAL_COLOUR, r.colour());
  }

  @Test
  void string_simple_match_covers_whole_text() {
    String text = "\"hello world\"";
    assertEquals(text.length(), singleMatch(3, text).end());
  }

  @Test
  void string_with_escaped_quote() {
    String text = "\"say \\\"hi\\\"\"";
    assertEquals(text.length(), singleMatch(3, text).end());
  }

  @Test
  void string_with_escaped_backslash() {
    assertEquals(1, match(3, "\"path\\\\file\"").size());
  }

  @Test
  void string_empty() {
    HighlightRegion r = singleMatch(3, "\"\"");
    assertEquals(2, r.end());
  }

  @Test
  void string_multiple_in_text() {
    assertEquals(2, match(3, "String a = \"foo\"; String b = \"bar\";").size());
  }

  @Test
  void string_containing_comment_syntax() {
    String text = "\"value // not a comment\"";
    assertEquals(text.length(), singleMatch(3, text).end());
  }

  @Test
  void string_no_match() {
    assertTrue(match(3, "int x = 42;").isEmpty());
  }

  @Test
  void string_at_end_of_text() {
    HighlightRegion r = singleMatch(3, "System.out.println(\"end\")");
    assertEquals(19, r.start());
  }

  // =========================================================================
  // Index 4 – Char literals  '.'  or '\n' etc.
  // =========================================================================

  @Test
  void char_literal_simple_match() {
    HighlightRegion r = singleMatch(4, "'a'");
    assertEquals(MiniJavaColours.CHAR_LITERAL_COLOUR, r.colour());
  }

  @Test
  void char_literal_simple_match_covers_whole_text() {
    assertEquals(3, singleMatch(4, "'a'").end());
  }

  @Test
  void char_literal_escaped_newline() {
    assertEquals(4, singleMatch(4, "'\\n'").end());
  }

  @Test
  void char_literal_escaped_backslash() {
    assertEquals(1, match(4, "'\\\\'").size());
  }

  @Test
  void char_literal_in_middle_of_text() {
    HighlightRegion r = singleMatch(4, "char c = 'x'; return c;");
    assertEquals(9, r.start());
  }

  @Test
  void char_literal_multiple() {
    assertEquals(2, match(4, "char a = 'a', b = 'b';").size());
  }

  @Test
  void char_literal_no_match_too_long() {
    assertTrue(match(4, "'ab'").isEmpty());
  }

  @Test
  void char_literal_no_match_empty() {
    assertTrue(match(4, "''").isEmpty());
  }

  // =========================================================================
  // Index 5 – Annotations  @Identifier
  // =========================================================================

  @Test
  void annotation_simple_override() {
    HighlightRegion r = singleMatch(5, "@Override");
    assertEquals(MiniJavaColours.ANNOTATION_COLOUR, r.colour());
  }

  @Test
  void annotation_simple_override_covers_whole_text() {
    assertEquals(9, singleMatch(5, "@Override").end());
  }

  @Test
  void annotation_with_hyphen() {
    String text = "@Over-ride";
    assertEquals(text.length(), singleMatch(5, text).end());
  }

  @Test
  void annotation_at_start_of_line() {
    HighlightRegion r = singleMatch(5, "\n@Test\npublic void foo() {}");
    assertEquals(1, r.start());
  }

  @Test
  void annotation_after_whitespace() {
    assertEquals(3, singleMatch(5, "   @SuppressWarnings(\"all\")").start());
  }

  @Test
  void annotation_multiple() {
    assertEquals(2, match(5, "@Override\n@Deprecated\npublic void old() {}").size());
  }

  @Test
  void annotation_no_match_plain_at() {
    assertFalse(match(5, "email@example.com").isEmpty());
  }

  // =========================================================================
  // Index 6 – Keywords
  // =========================================================================

  @Test
  void keyword_package() {
    HighlightRegion r = singleMatch(6, "package com.example;");
    assertEquals(MiniJavaColours.KEYWORD_COLOUR, r.colour());
  }

  @Test
  void keyword_package_covers_only_keyword() {
    assertEquals(7, singleMatch(6, "package com.example;").end());
  }

  @Test
  void keyword_import() {
    assertEquals(6, singleMatch(6, "import java.util.List;").end());
  }

  @Test
  void keyword_all_keywords_recognised() {
    String[] keywords = {
      "package", "import", "class", "public", "private", "final", "return", "null", "new"
    };
    for (String kw : keywords) {
      assertEquals(1, match(6, kw).size(), "Expected keyword '" + kw + "' to be recognised");
    }
  }

  @Test
  void keyword_not_matched_as_part_of_identifier() {
    String[] nonKeywords = {"newValue", "finals", "publicly", "returning", "nullable"};
    for (String word : nonKeywords) {
      assertTrue(match(6, word).isEmpty(), "'" + word + "' should NOT match keyword token");
    }
  }

  @Test
  void keyword_multiple_in_one_line() {
    assertEquals(4, match(6, "public class Foo { private final int x; }").size());
  }

  @Test
  void keyword_inside_line_comment_text_still_matches() {
    assertEquals(2, match(6, "// return null").size());
  }

  @Test
  void keyword_no_match() {
    assertTrue(match(6, "fooBar = 42;").isEmpty());
  }

  // =========================================================================
  // Token ordering / priority edge cases
  // =========================================================================

  @Test
  void ordering_javadoc_before_block_comment() {
    List<Token> tokens = MiniJavaTokens.defaultTokens();
    int javadocIndex = -1, blockIndex = -1;
    for (int i = 0; i < tokens.size(); i++) {
      String src = tokens.get(i).pattern().pattern();
      if (src.startsWith("/\\*\\*")) javadocIndex = i;
      else if (src.startsWith("/\\*")) blockIndex = i;
    }
    assertTrue(
        javadocIndex < blockIndex,
        "Javadoc token (index "
            + javadocIndex
            + ") must come before block-comment token (index "
            + blockIndex
            + ")");
  }

  @Test
  void ordering_comments_before_strings() {
    List<Token> tokens = MiniJavaTokens.defaultTokens();
    int lineCommentIndex = -1, stringIndex = -1;
    for (int i = 0; i < tokens.size(); i++) {
      String src = tokens.get(i).pattern().pattern();
      if (src.startsWith("//")) lineCommentIndex = i;
      if (src.startsWith("\"")) stringIndex = i;
    }
    assertTrue(
        lineCommentIndex < stringIndex, "Line-comment token must come before string literal token");
  }

  // =========================================================================
  // Additional edge-case tests using the Texts.START_TEXT sample
  // =========================================================================

  @Test
  void start_text_javadoc_present() {
    assertTrue(
        match(0, Texts.START_TEXT).size() >= 2,
        "START_TEXT should contain at least 2 javadoc comments");
  }

  @Test
  void start_text_line_comment_present() {
    assertTrue(
        match(2, Texts.START_TEXT).size() >= 2,
        "START_TEXT should contain at least 2 line comments");
  }

  @Test
  void start_text_string_literals_present() {
    assertTrue(
        match(3, Texts.START_TEXT).size() >= 1,
        "START_TEXT should contain at least 1 string literal");
  }

  @Test
  void start_text_annotation_present() {
    assertTrue(
        match(5, Texts.START_TEXT).size() >= 1, "START_TEXT should contain at least 1 annotation");
  }

  @Test
  void start_text_keywords_present() {
    assertTrue(match(6, Texts.START_TEXT).size() >= 5, "START_TEXT should contain many keywords");
  }
}
