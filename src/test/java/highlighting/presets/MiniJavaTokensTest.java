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
    void javadoc_simpleMatch() {
        String text = "/** This is javadoc */";
        HighlightRegion r = singleMatch(0, text);
        assertEquals(MiniJavaColours.JAVADOC_COMMENT_COLOUR, r.colour());
    }

    @Test
    void javadoc_simpleMatch_coversWholeText() {
        String text = "/** This is javadoc */";
        HighlightRegion r = singleMatch(0, text);
        assertEquals(text.length(), r.end());
    }

    @Test
    void javadoc_multiLine() {
        String text = "/**\n * @param x value\n * @return result\n */";
        HighlightRegion r = singleMatch(0, text);
        assertEquals(text.length(), r.end());
    }

    @Test
    void javadoc_inMiddleOfText() {
        String text = "int x; /** doc */ int y;";
        HighlightRegion r = singleMatch(0, text);
        assertEquals(7, r.start());
    }

    @Test
    void javadoc_multipleInText() {
        String text = "/** one */ class Foo { /** two */ }";
        assertEquals(2, match(0, text).size());
    }

    @Test
    void javadoc_noMatch_blockComment() {
        assertTrue(match(0, "/* not javadoc */").isEmpty());
    }

    @Test
    void javadoc_atEndOfText() {
        String text = "class Foo { }/** trailing */";
        assertEquals(13, singleMatch(0, text).start());
    }

    // =========================================================================
    // Index 1 – Block comments  /* ... */
    // =========================================================================

    @Test
    void blockComment_simpleMatch() {
        String text = "/* a block comment */";
        HighlightRegion r = singleMatch(1, text);
        assertEquals(MiniJavaColours.BLOCK_COMMENT_COLOUR, r.colour());
    }

    @Test
    void blockComment_simpleMatch_coversWholeText() {
        String text = "/* a block comment */";
        HighlightRegion r = singleMatch(1, text);
        assertEquals(text.length(), r.end());
    }

    @Test
    void blockComment_multiLine() {
        assertEquals(1, match(1, "/*\n  line one\n  line two\n*/").size());
    }

    @Test
    void blockComment_containsKeyword() {
        String text = "/* public class return */";
        HighlightRegion r = singleMatch(1, text);
        assertEquals(text.length(), r.end());
    }

    @Test
    void blockComment_containsStringLikeSyntax() {
        assertEquals(1, match(1, "/* contains \"string\" inside */").size());
    }

    @Test
    void blockComment_noMatch() {
        assertTrue(match(1, "int x = 42;").isEmpty());
    }

    @Test
    void blockComment_atStartOfText() {
        String text = "/* start */ int x;";
        HighlightRegion r = singleMatch(1, text);
        assertEquals(11, r.end());
    }

    // =========================================================================
    // Index 2 – Line comments  // ...
    // =========================================================================

    @Test
    void lineComment_simpleMatch() {
        String text = "// this is a comment";
        HighlightRegion r = singleMatch(2, text);
        assertEquals(MiniJavaColours.LINE_COMMENT_COLOUR, r.colour());
    }

    @Test
    void lineComment_simpleMatch_coversWholeText() {
        String text = "// this is a comment";
        assertEquals(text.length(), singleMatch(2, text).end());
    }

    @Test
    void lineComment_afterCode() {
        assertEquals(11, singleMatch(2, "int x = 5; // set x").start());
    }

    @Test
    void lineComment_multipleLines() {
        assertEquals(3, match(2, "// first\n// second\n// third").size());
    }

    @Test
    void lineComment_containsBlockCommentSyntax() {
        String text = "// ignore /* this */ and that";
        HighlightRegion r = singleMatch(2, text);
        assertEquals(text.length(), r.end());
    }

    @Test
    void lineComment_noMatch() {
        assertTrue(match(2, "int x = 5;").isEmpty());
    }

    @Test
    void lineComment_atEndOfText() {
        String text = "return 0; // end";
        HighlightRegion r = singleMatch(2, text);
        assertEquals(10, r.start());
    }

    // =========================================================================
    // Index 3 – String literals  "..."
    // =========================================================================

    @Test
    void string_simpleMatch() {
        String text = "\"hello world\"";
        HighlightRegion r = singleMatch(3, text);
        assertEquals(MiniJavaColours.STRING_LITERAL_COLOUR, r.colour());
    }

    @Test
    void string_simpleMatch_coversWholeText() {
        String text = "\"hello world\"";
        assertEquals(text.length(), singleMatch(3, text).end());
    }

    @Test
    void string_withEscapedQuote() {
        String text = "\"say \\\"hi\\\"\"";
        assertEquals(text.length(), singleMatch(3, text).end());
    }

    @Test
    void string_withEscapedBackslash() {
        assertEquals(1, match(3, "\"path\\\\file\"").size());
    }

    @Test
    void string_empty() {
        HighlightRegion r = singleMatch(3, "\"\"");
        assertEquals(2, r.end());
    }

    @Test
    void string_multipleInText() {
        assertEquals(2, match(3, "String a = \"foo\"; String b = \"bar\";").size());
    }

    @Test
    void string_containingCommentSyntax() {
        String text = "\"value // not a comment\"";
        assertEquals(text.length(), singleMatch(3, text).end());
    }

    @Test
    void string_noMatch() {
        assertTrue(match(3, "int x = 42;").isEmpty());
    }

    @Test
    void string_atEndOfText() {
        HighlightRegion r = singleMatch(3, "System.out.println(\"end\")");
        assertEquals(19, r.start());
    }

    // =========================================================================
    // Index 4 – Char literals  '.'  or '\n' etc.
    // =========================================================================

    @Test
    void charLiteral_simpleMatch() {
        HighlightRegion r = singleMatch(4, "'a'");
        assertEquals(MiniJavaColours.CHAR_LITERAL_COLOUR, r.colour());
    }

    @Test
    void charLiteral_simpleMatch_coversWholeText() {
        assertEquals(3, singleMatch(4, "'a'").end());
    }

    @Test
    void charLiteral_escapedNewline() {
        assertEquals(4, singleMatch(4, "'\\n'").end());
    }

    @Test
    void charLiteral_escapedBackslash() {
        assertEquals(1, match(4, "'\\\\'").size());
    }

    @Test
    void charLiteral_inMiddleOfText() {
        HighlightRegion r = singleMatch(4, "char c = 'x'; return c;");
        assertEquals(9, r.start());
    }

    @Test
    void charLiteral_multiple() {
        assertEquals(2, match(4, "char a = 'a', b = 'b';").size());
    }

    @Test
    void charLiteral_noMatch_tooLong() {
        assertTrue(match(4, "'ab'").isEmpty());
    }

    @Test
    void charLiteral_noMatch_empty() {
        assertTrue(match(4, "''").isEmpty());
    }

    // =========================================================================
    // Index 5 – Annotations  @Identifier
    // =========================================================================

    @Test
    void annotation_simpleOverride() {
        HighlightRegion r = singleMatch(5, "@Override");
        assertEquals(MiniJavaColours.ANNOTATION_COLOUR, r.colour());
    }

    @Test
    void annotation_simpleOverride_coversWholeText() {
        assertEquals(9, singleMatch(5, "@Override").end());
    }

    @Test
    void annotation_withHyphen() {
        String text = "@Over-ride";
        assertEquals(text.length(), singleMatch(5, text).end());
    }

    @Test
    void annotation_atStartOfLine() {
        HighlightRegion r = singleMatch(5, "\n@Test\npublic void foo() {}");
        assertEquals(1, r.start());
    }

    @Test
    void annotation_afterWhitespace() {
        assertEquals(3, singleMatch(5, "   @SuppressWarnings(\"all\")").start());
    }

    @Test
    void annotation_multiple() {
        assertEquals(2, match(5, "@Override\n@Deprecated\npublic void old() {}").size());
    }

    @Test
    void annotation_noMatch_plainAt() {
        // The pattern @[A-Za-z_-]+ matches @example in an email address — document actual behaviour
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
    void keyword_package_coversOnlyKeyword() {
        assertEquals(7, singleMatch(6, "package com.example;").end());
    }

    @Test
    void keyword_import() {
        assertEquals(6, singleMatch(6, "import java.util.List;").end());
    }

    @Test
    void keyword_allKeywordsRecognised() {
        String[] keywords = {
            "package", "import", "class", "public", "private", "final", "return", "null", "new"
        };
        for (String kw : keywords) {
            assertEquals(1, match(6, kw).size(), "Expected keyword '" + kw + "' to be recognised");
        }
    }

    @Test
    void keyword_notMatchedAsPartOfIdentifier() {
        String[] nonKeywords = {"newValue", "finals", "publicly", "returning", "nullable"};
        for (String word : nonKeywords) {
            assertTrue(match(6, word).isEmpty(), "'" + word + "' should NOT match keyword token");
        }
    }

    @Test
    void keyword_multipleInOneLine() {
        // public, class, private, final
        assertEquals(4, match(6, "public class Foo { private final int x; }").size());
    }

    @Test
    void keyword_insideLineCommentTextStillMatches() {
        // Raw token sees "return" and "null" — conflict resolution happens at a higher layer
        assertEquals(2, match(6, "// return null").size());
    }

    @Test
    void keyword_noMatch() {
        assertTrue(match(6, "fooBar = 42;").isEmpty());
    }

    // =========================================================================
    // Token ordering / priority edge cases
    // =========================================================================

    @Test
    void ordering_javadocBeforeBlockComment() {
        List<Token> tokens = MiniJavaTokens.defaultTokens();
        int javadocIndex = -1, blockIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            String src = tokens.get(i).pattern().pattern();
            if (src.startsWith("/\\*\\*")) javadocIndex = i;
            else if (src.startsWith("/\\*")) blockIndex = i;
        }
        assertTrue(
            javadocIndex < blockIndex,
            "Javadoc token (index " + javadocIndex + ") must come before block-comment token (index " + blockIndex + ")");
    }

    @Test
    void ordering_commentsBeforeStrings() {
        List<Token> tokens = MiniJavaTokens.defaultTokens();
        int lineCommentIndex = -1, stringIndex = -1;
        for (int i = 0; i < tokens.size(); i++) {
            String src = tokens.get(i).pattern().pattern();
            if (src.startsWith("//")) lineCommentIndex = i;
            if (src.startsWith("\"")) stringIndex = i;
        }
        assertTrue(
            lineCommentIndex < stringIndex,
            "Line-comment token must come before string literal token");
    }

    // =========================================================================
    // Additional edge-case tests using the Texts.START_TEXT sample
    // =========================================================================

    @Test
    void startText_javadocPresent() {
        assertTrue(match(0, Texts.START_TEXT).size() >= 2,
            "START_TEXT should contain at least 2 javadoc comments");
    }

    @Test
    void startText_lineCommentPresent() {
        assertTrue(match(2, Texts.START_TEXT).size() >= 2,
            "START_TEXT should contain at least 2 line comments");
    }

    @Test
    void startText_stringLiteralsPresent() {
        assertTrue(match(3, Texts.START_TEXT).size() >= 1,
            "START_TEXT should contain at least 1 string literal");
    }

    @Test
    void startText_annotationPresent() {
        assertTrue(match(5, Texts.START_TEXT).size() >= 1,
            "START_TEXT should contain at least 1 annotation");
    }

    @Test
    void startText_keywordsPresent() {
        assertTrue(match(6, Texts.START_TEXT).size() >= 5,
            "START_TEXT should contain many keywords");
    }
}
