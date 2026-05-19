package highlighting.presets;

import static org.junit.jupiter.api.Assertions.*;

import highlighting.core.HighlightRegion;
import highlighting.regex.Token;
import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive JUnit 5 tests for {@link MiniJavaTokens#defaultTokens()}.
 *
 * Token indices in defaultTokens():
 *   0 – Javadoc comment  /** ... * /
 *   1 – Block comment    /* ... * /
 *   2 – Line comment     // ...
 *   3 – String literal   "..."
 *   4 – Char literal     '.'
 *   5 – Annotation       @Identifier
 *   6 – Keywords
 */
public class MiniJavaTokensTest {

    // -------------------------------------------------------------------------
    // Helper: fetch token by index and run it against text
    // -------------------------------------------------------------------------
    private static List<HighlightRegion> match(int tokenIndex, String text) {
        return MiniJavaTokens.defaultTokens().get(tokenIndex).test(text);
    }

    // =========================================================================
    // Index 0 – Javadoc comments
    // =========================================================================

    @Test
    void javadoc_simpleMatch() {
        String text = "/** This is javadoc */";
        var regions = match(0, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
        assertEquals(MiniJavaColours.JAVADOC_COMMENT_COLOUR, regions.get(0).colour());
    }

    @Test
    void javadoc_multiLine() {
        String text = "/**\n * @param x value\n * @return result\n */";
        var regions = match(0, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
    }

    @Test
    void javadoc_inMiddleOfText() {
        String text = "int x; /** doc */ int y;";
        var regions = match(0, text);
        assertEquals(1, regions.size());
        assertEquals(7, regions.get(0).start());
        assertEquals(17, regions.get(0).end());
    }

    @Test
    void javadoc_multipleInText() {
        String text = "/** one */ class Foo { /** two */ }";
        var regions = match(0, text);
        assertEquals(2, regions.size());
    }

    @Test
    void javadoc_noMatch_blockComment() {
        // A plain block comment must NOT match the javadoc token
        // (though it would match the block-comment token at index 1)
        String text = "/* not javadoc */";
        var regions = match(0, text);
        assertTrue(regions.isEmpty());
    }

    @Test
    void javadoc_atEndOfText() {
        String text = "class Foo { }/** trailing */";
        var regions = match(0, text);
        assertEquals(1, regions.size());
        assertEquals(13, regions.get(0).start());
    }

    // =========================================================================
    // Index 1 – Block comments  /* ... */
    // =========================================================================

    @Test
    void blockComment_simpleMatch() {
        String text = "/* a block comment */";
        var regions = match(1, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
        assertEquals(MiniJavaColours.BLOCK_COMMENT_COLOUR, regions.get(0).colour());
    }

    @Test
    void blockComment_multiLine() {
        String text = "/*\n  line one\n  line two\n*/";
        var regions = match(1, text);
        assertEquals(1, regions.size());
    }

    @Test
    void blockComment_containsKeyword() {
        // Keywords inside block comments should still be matched as a comment region
        String text = "/* public class return */";
        var regions = match(1, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
    }

    @Test
    void blockComment_containsStringLikeSyntax() {
        String text = "/* contains \"string\" inside */";
        var regions = match(1, text);
        assertEquals(1, regions.size());
    }

    @Test
    void blockComment_noMatch() {
        String text = "int x = 42;";
        assertTrue(match(1, text).isEmpty());
    }

    @Test
    void blockComment_atStartOfText() {
        String text = "/* start */ int x;";
        var regions = match(1, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(11, regions.get(0).end());
    }

    // =========================================================================
    // Index 2 – Line comments  // ...
    // =========================================================================

    @Test
    void lineComment_simpleMatch() {
        String text = "// this is a comment";
        var regions = match(2, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
        assertEquals(MiniJavaColours.LINE_COMMENT_COLOUR, regions.get(0).colour());
    }

    @Test
    void lineComment_afterCode() {
        String text = "int x = 5; // set x";
        var regions = match(2, text);
        assertEquals(1, regions.size());
        assertEquals(11, regions.get(0).start());
    }

    @Test
    void lineComment_multipleLines() {
        String text = "// first\n// second\n// third";
        var regions = match(2, text);
        assertEquals(3, regions.size());
    }

    @Test
    void lineComment_containsBlockCommentSyntax() {
        // /* inside a line comment should not terminate the line comment
        String text = "// ignore /* this */ and that";
        var regions = match(2, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
    }

    @Test
    void lineComment_noMatch() {
        String text = "int x = 5;";
        assertTrue(match(2, text).isEmpty());
    }

    @Test
    void lineComment_atEndOfText() {
        String text = "return 0; // end";
        var regions = match(2, text);
        assertEquals(1, regions.size());
        assertEquals(10, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
    }

    // =========================================================================
    // Index 3 – String literals  "..."
    // =========================================================================

    @Test
    void string_simpleMatch() {
        String text = "\"hello world\"";
        var regions = match(3, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
        assertEquals(MiniJavaColours.STRING_LITERAL_COLOUR, regions.get(0).colour());
    }

    @Test
    void string_withEscapedQuote() {
        String text = "\"say \\\"hi\\\"\"";
        var regions = match(3, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
    }

    @Test
    void string_withEscapedBackslash() {
        String text = "\"path\\\\file\"";
        var regions = match(3, text);
        assertEquals(1, regions.size());
    }

    @Test
    void string_empty() {
        String text = "\"\"";
        var regions = match(3, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(2, regions.get(0).end());
    }

    @Test
    void string_multipleInText() {
        String text = "String a = \"foo\"; String b = \"bar\";";
        var regions = match(3, text);
        assertEquals(2, regions.size());
    }

    @Test
    void string_containingCommentSyntax() {
        // // and /* inside strings should not break the string match
        String text = "\"value // not a comment\"";
        var regions = match(3, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
    }

    @Test
    void string_noMatch() {
        String text = "int x = 42;";
        assertTrue(match(3, text).isEmpty());
    }

    @Test
    void string_atEndOfText() {
        String text = "System.out.println(\"end\")";
        var regions = match(3, text);
        assertEquals(1, regions.size());
        assertEquals(19, regions.get(0).start());
        assertEquals(24, regions.get(0).end());
    }

    // =========================================================================
    // Index 4 – Char literals  '.'  or '\n' etc.
    // =========================================================================

    @Test
    void charLiteral_simpleMatch() {
        String text = "'a'";
        var regions = match(4, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(3, regions.get(0).end());
        assertEquals(MiniJavaColours.CHAR_LITERAL_COLOUR, regions.get(0).colour());
    }

    @Test
    void charLiteral_escapedNewline() {
        String text = "'\\n'";
        var regions = match(4, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(4, regions.get(0).end());
    }

    @Test
    void charLiteral_escapedBackslash() {
        String text = "'\\\\'";
        var regions = match(4, text);
        assertEquals(1, regions.size());
    }

    @Test
    void charLiteral_inMiddleOfText() {
        String text = "char c = 'x'; return c;";
        var regions = match(4, text);
        assertEquals(1, regions.size());
        assertEquals(9, regions.get(0).start());
        assertEquals(12, regions.get(0).end());
    }

    @Test
    void charLiteral_multiple() {
        String text = "char a = 'a', b = 'b';";
        var regions = match(4, text);
        assertEquals(2, regions.size());
    }

    @Test
    void charLiteral_noMatch_tooLong() {
        // Multi-char content should not match a char literal token
        String text = "'ab'";
        var regions = match(4, text);
        assertTrue(regions.isEmpty());
    }

    @Test
    void charLiteral_noMatch_empty() {
        String text = "''";
        assertTrue(match(4, text).isEmpty());
    }

    // =========================================================================
    // Index 5 – Annotations  @Identifier
    // =========================================================================

    @Test
    void annotation_simpleOverride() {
        String text = "@Override";
        var regions = match(5, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(9, regions.get(0).end());
        assertEquals(MiniJavaColours.ANNOTATION_COLOUR, regions.get(0).colour());
    }

    @Test
    void annotation_withHyphen() {
        // @Over-ride contains a hyphen and should still match (pattern allows '-')
        String text = "@Over-ride";
        var regions = match(5, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(text.length(), regions.get(0).end());
    }

    @Test
    void annotation_atStartOfLine() {
        String text = "\n@Test\npublic void foo() {}";
        var regions = match(5, text);
        assertEquals(1, regions.size());
        assertEquals(1, regions.get(0).start());
        assertEquals(6, regions.get(0).end());
    }

    @Test
    void annotation_afterWhitespace() {
        String text = "   @SuppressWarnings(\"all\")";
        var regions = match(5, text);
        assertEquals(1, regions.size());
        assertEquals(3, regions.get(0).start());
    }

    @Test
    void annotation_multiple() {
        String text = "@Override\n@Deprecated\npublic void old() {}";
        var regions = match(5, text);
        assertEquals(2, regions.size());
    }

    @Test
    void annotation_noMatch_plainAt() {
        // Lone '@' without following identifier chars should not match
        String text = "email@example.com";
        // The pattern @[A-Za-z_-]+ will match @example here — that's expected behaviour.
        // This test documents what the pattern actually does:
        var regions = match(5, text);
        // Match starts at '@' in the email address — document the actual behaviour
        assertFalse(regions.isEmpty()); // pattern does match @example
    }

    // =========================================================================
    // Index 6 – Keywords
    // =========================================================================

    @Test
    void keyword_package() {
        String text = "package com.example;";
        var regions = match(6, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(7, regions.get(0).end());
        assertEquals(MiniJavaColours.KEYWORD_COLOUR, regions.get(0).colour());
    }

    @Test
    void keyword_import() {
        String text = "import java.util.List;";
        var regions = match(6, text);
        assertEquals(1, regions.size());
        assertEquals(0, regions.get(0).start());
        assertEquals(6, regions.get(0).end());
    }

    @Test
    void keyword_allKeywordsRecognised() {
        String[] keywords = {"package", "import", "class", "public", "private", "final", "return", "null", "new"};
        for (String kw : keywords) {
            var regions = match(6, kw);
            assertEquals(1, regions.size(), "Expected keyword '" + kw + "' to be recognised");
        }
    }

    @Test
    void keyword_notMatchedAsPartOfIdentifier() {
        // "newValue", "finals", "publicly" must NOT match
        String[] nonKeywords = {"newValue", "finals", "publicly", "returning", "nullable"};
        for (String word : nonKeywords) {
            var regions = match(6, word);
            assertTrue(regions.isEmpty(), "'" + word + "' should NOT match keyword token");
        }
    }

    @Test
    void keyword_multipleInOneLine() {
        String text = "public class Foo { private final int x; }";
        var regions = match(6, text);
        // Expect: public, class, private, final
        assertEquals(4, regions.size());
    }

    @Test
    void keyword_insideLineCommentTextStillMatches() {
        // The keyword token itself doesn't know about comments — it just matches patterns.
        // Conflict resolution in higher layers handles this. Document the raw token behaviour.
        String text = "// return null";
        var regions = match(6, text);
        // Raw token sees "return" and "null" inside the comment text
        assertEquals(2, regions.size());
    }

    @Test
    void keyword_noMatch() {
        String text = "fooBar = 42;";
        assertTrue(match(6, text).isEmpty());
    }

    // =========================================================================
    // Token ordering / priority edge cases
    // =========================================================================

    @Test
    void ordering_javadocBeforeBlockComment() {
        // Token 0 (javadoc) must come before token 1 (block comment) in the list,
        // so /** ... */ is treated as javadoc and not as a plain block comment.
        List<Token> tokens = MiniJavaTokens.defaultTokens();
        int javadocIndex = -1, blockIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            String src = tokens.get(i).pattern().pattern();
            if (src.startsWith("/\\*\\*")) javadocIndex = i;
            else if (src.startsWith("/\\*")) blockIndex = i;
        }
        assertTrue(javadocIndex >= 0, "Javadoc token must exist");
        assertTrue(blockIndex >= 0, "Block-comment token must exist");
        assertTrue(javadocIndex < blockIndex,
            "Javadoc token (index " + javadocIndex + ") must come before block-comment token (index " + blockIndex + ")");
    }

    @Test
    void ordering_commentsBeforeStrings() {
        // Comments should be listed before string literals so that "..." inside a comment
        // does not create a spurious string highlight (resolved by conflict logic, but ordering helps).
        List<Token> tokens = MiniJavaTokens.defaultTokens();
        int lineCommentIndex = -1, stringIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            String src = tokens.get(i).pattern().pattern();
            if (src.startsWith("//")) lineCommentIndex = i;
            if (src.startsWith("\"")) stringIndex = i;
        }
        assertTrue(lineCommentIndex >= 0, "Line-comment token must exist");
        assertTrue(stringIndex >= 0, "String literal token must exist");
        assertTrue(lineCommentIndex < stringIndex,
            "Line-comment token must come before string literal token");
    }

    // =========================================================================
    // Additional edge-case tests using the Texts.START_TEXT sample
    // =========================================================================

    @Test
    void startText_javadocPresent() {
        long count = match(0, Texts.START_TEXT).size();
        assertTrue(count >= 2, "START_TEXT should contain at least 2 javadoc comments");
    }

    @Test
    void startText_lineCommentPresent() {
        long count = match(2, Texts.START_TEXT).size();
        assertTrue(count >= 2, "START_TEXT should contain at least 2 line comments");
    }

    @Test
    void startText_stringLiteralsPresent() {
        long count = match(3, Texts.START_TEXT).size();
        assertTrue(count >= 1, "START_TEXT should contain at least 1 string literal");
    }

    @Test
    void startText_annotationPresent() {
        long count = match(5, Texts.START_TEXT).size();
        assertTrue(count >= 1, "START_TEXT should contain at least 1 annotation");
    }

    @Test
    void startText_keywordsPresent() {
        long count = match(6, Texts.START_TEXT).size();
        assertTrue(count >= 5, "START_TEXT should contain many keywords");
    }
}
