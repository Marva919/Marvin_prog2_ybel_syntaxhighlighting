package highlighting.regex;

import static org.junit.jupiter.api.Assertions.*;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaColours;
import java.awt.Color;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegexHighlighterTest {

  private SyntaxHighlighter highlighter;

  @BeforeEach
  void setUp() {
    highlighter = new RegexHighlighter();
  }

  private List<HighlightRegion> compute(String text) {
    return highlighter.computeRegions(text);
  }

  private List<HighlightRegion> raw(String text) {
    return highlighter.collectMatches(text);
  }

  // -------------------------------------------------------------------------
  // collectMatches
  // -------------------------------------------------------------------------

  @Test
  void collectMatches_emptyText_returnsEmptyList() {
    assertTrue(raw("").isEmpty());
  }

  @Test
  void collectMatches_noTokenMatch_returnsEmptyList() {
    assertTrue(raw("fooBar").isEmpty());
  }

  @Test
  void collectMatches_singleKeyword_containsKeywordRegion() {
    assertTrue(
        raw("public").stream().anyMatch(r -> r.colour().equals(MiniJavaColours.KEYWORD_COLOUR)));
  }

  @Test
  void collectMatches_keywordInsideLineComment_bothPresentBeforeResolution() {
    var regions = raw("// public");
    assertTrue(
        regions.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.LINE_COMMENT_COLOUR)));
    assertTrue(regions.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.KEYWORD_COLOUR)));
  }

  @Test
  void collectMatches_javadocMatchedByBothTokens_bothPresentBeforeResolution() {
    var regions = raw("/** javadoc */");
    assertTrue(
        regions.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.JAVADOC_COMMENT_COLOUR)));
    assertTrue(
        regions.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.BLOCK_COMMENT_COLOUR)));
  }

  // -------------------------------------------------------------------------
  // resolveConflicts – overlap handling
  // -------------------------------------------------------------------------

  @Test
  void resolveConflicts_keywordInsideLineComment_commentWins() {
    var result = compute("// public");
    assertEquals(1, result.size());
    assertEquals(MiniJavaColours.LINE_COMMENT_COLOUR, result.get(0).colour());
  }

  @Test
  void resolveConflicts_keywordInsideBlockComment_commentWins() {
    var result = compute("/* return null */");
    assertEquals(1, result.size());
    assertEquals(MiniJavaColours.BLOCK_COMMENT_COLOUR, result.get(0).colour());
  }

  @Test
  void resolveConflicts_javadocVsBlockComment_javadocWins() {
    var result = compute("/** This is javadoc */");
    assertEquals(1, result.size());
    assertEquals(MiniJavaColours.JAVADOC_COMMENT_COLOUR, result.get(0).colour());
  }

  @Test
  void resolveConflicts_keywordInsideString_stringWins() {
    var result = compute("\"return\"");
    assertEquals(1, result.size());
    assertEquals(MiniJavaColours.STRING_LITERAL_COLOUR, result.get(0).colour());
  }

  @Test
  void resolveConflicts_manualAdjacentIntervals_bothKept() {
    var input =
        List.of(new HighlightRegion(0, 5, Color.BLUE), new HighlightRegion(5, 10, Color.RED));
    var result = highlighter.resolveConflicts(input);
    assertEquals(2, result.size());
    assertEquals(Color.BLUE, result.get(0).colour());
  }

  @Test
  void resolveConflicts_manualOverlappingIntervals_firstWins() {
    var input =
        List.of(new HighlightRegion(0, 10, Color.BLUE), new HighlightRegion(5, 15, Color.RED));
    var result = highlighter.resolveConflicts(input);
    assertEquals(1, result.size());
    assertEquals(Color.BLUE, result.get(0).colour());
  }

  @Test
  void resolveConflicts_containedInterval_outerWins() {
    var input =
        List.of(new HighlightRegion(0, 20, Color.BLUE), new HighlightRegion(5, 10, Color.RED));
    var result = highlighter.resolveConflicts(input);
    assertEquals(1, result.size());
    assertEquals(Color.BLUE, result.get(0).colour());
  }

  @Test
  void resolveConflicts_threeRegions_overlappingMiddleDiscarded() {
    // [0,5) kept; [4,8) overlaps → discarded; [5,10) adjacent to first → kept
    var input =
        List.of(
            new HighlightRegion(0, 5, Color.BLUE),
            new HighlightRegion(4, 8, Color.GREEN),
            new HighlightRegion(5, 10, Color.RED));
    var result = highlighter.resolveConflicts(input);
    assertEquals(2, result.size());
    assertEquals(Color.BLUE, result.get(0).colour());
    assertEquals(Color.RED, result.get(1).colour());
  }

  // -------------------------------------------------------------------------
  // End-to-end
  // -------------------------------------------------------------------------

  @Test
  void endToEnd_emptyString_noRegions() {
    assertTrue(compute("").isEmpty());
  }

  @Test
  void endToEnd_twoSeparateKeywords_bothColoured() {
    var result = compute("public class");
    assertEquals(2, result.size());
    assertEquals(MiniJavaColours.KEYWORD_COLOUR, result.get(0).colour());
  }

  @Test
  void endToEnd_annotationAndKeyword_bothColoured() {
    var result = compute("@Override\npublic void foo() {}");
    assertTrue(result.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.ANNOTATION_COLOUR)));
    assertTrue(result.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.KEYWORD_COLOUR)));
  }

  @Test
  void endToEnd_noRegionsOverlapInFinalResult() {
    var result = compute("public class Foo { /* block */ private final String s = \"hello\"; }");
    for (int i = 0; i < result.size() - 1; i++) {
      assertTrue(result.get(i).end() <= result.get(i + 1).start());
    }
  }

  @Test
  void endToEnd_resultIsSortedByStart() {
    var result = compute("import java.util.List;\npublic class Foo {}");
    for (int i = 0; i < result.size() - 1; i++) {
      assertTrue(result.get(i).start() <= result.get(i + 1).start());
    }
  }
}
