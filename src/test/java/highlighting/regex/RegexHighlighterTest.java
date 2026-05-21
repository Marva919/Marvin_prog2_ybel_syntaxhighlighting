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
  void collect_matches_empty_text_returns_empty_list() {
    assertTrue(raw("").isEmpty());
  }

  @Test
  void collect_matches_no_token_match_returns_empty_list() {
    assertTrue(raw("fooBar").isEmpty());
  }

  @Test
  void collect_matches_single_keyword_contains_keyword_region() {
    assertTrue(
        raw("public").stream().anyMatch(r -> r.colour().equals(MiniJavaColours.KEYWORD_COLOUR)));
  }

  @Test
  void collect_matches_keyword_inside_line_comment_both_present_before_resolution() {
    var regions = raw("// public");
    assertTrue(
        regions.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.LINE_COMMENT_COLOUR)));
    assertTrue(regions.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.KEYWORD_COLOUR)));
  }

  @Test
  void collect_matches_javadoc_matched_by_both_tokens_both_present_before_resolution() {
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
  void resolve_conflicts_keyword_inside_line_comment_comment_wins() {
    var result = compute("// public");
    assertEquals(1, result.size());
    assertEquals(MiniJavaColours.LINE_COMMENT_COLOUR, result.get(0).colour());
  }

  @Test
  void resolve_conflicts_keyword_inside_block_comment_comment_wins() {
    var result = compute("/* return null */");
    assertEquals(1, result.size());
    assertEquals(MiniJavaColours.BLOCK_COMMENT_COLOUR, result.get(0).colour());
  }

  @Test
  void resolve_conflicts_javadoc_vs_block_comment_javadoc_wins() {
    var result = compute("/** This is javadoc */");
    assertEquals(1, result.size());
    assertEquals(MiniJavaColours.JAVADOC_COMMENT_COLOUR, result.get(0).colour());
  }

  @Test
  void resolve_conflicts_keyword_inside_string_string_wins() {
    var result = compute("\"return\"");
    assertEquals(1, result.size());
    assertEquals(MiniJavaColours.STRING_LITERAL_COLOUR, result.get(0).colour());
  }

  @Test
  void resolve_conflicts_manual_adjacent_intervals_both_kept() {
    var input =
        List.of(new HighlightRegion(0, 5, Color.BLUE), new HighlightRegion(5, 10, Color.RED));
    var result = highlighter.resolveConflicts(input);
    assertEquals(2, result.size());
    assertEquals(Color.BLUE, result.get(0).colour());
  }

  @Test
  void resolve_conflicts_manual_overlapping_intervals_first_wins() {
    var input =
        List.of(new HighlightRegion(0, 10, Color.BLUE), new HighlightRegion(5, 15, Color.RED));
    var result = highlighter.resolveConflicts(input);
    assertEquals(1, result.size());
    assertEquals(Color.BLUE, result.get(0).colour());
  }

  @Test
  void resolve_conflicts_contained_interval_outer_wins() {
    var input =
        List.of(new HighlightRegion(0, 20, Color.BLUE), new HighlightRegion(5, 10, Color.RED));
    var result = highlighter.resolveConflicts(input);
    assertEquals(1, result.size());
    assertEquals(Color.BLUE, result.get(0).colour());
  }

  @Test
  void resolve_conflicts_three_regions_overlapping_middle_discarded() {
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
  void end_to_end_empty_string_no_regions() {
    assertTrue(compute("").isEmpty());
  }

  @Test
  void end_to_end_two_separate_keywords_both_coloured() {
    var result = compute("public class");
    assertEquals(2, result.size());
    assertEquals(MiniJavaColours.KEYWORD_COLOUR, result.get(0).colour());
  }

  @Test
  void end_to_end_annotation_and_keyword_both_coloured() {
    var result = compute("@Override\npublic void foo() {}");
    assertTrue(result.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.ANNOTATION_COLOUR)));
    assertTrue(result.stream().anyMatch(r -> r.colour().equals(MiniJavaColours.KEYWORD_COLOUR)));
  }

  @Test
  void end_to_end_no_regions_overlap_in_final_result() {
    var result = compute("public class Foo { /* block */ private final String s = \"hello\"; }");
    for (int i = 0; i < result.size() - 1; i++) {
      assertTrue(result.get(i).end() <= result.get(i + 1).start());
    }
  }

  @Test
  void end_to_end_result_is_sorted_by_start() {
    var result = compute("import java.util.List;\npublic class Foo {}");
    for (int i = 0; i < result.size() - 1; i++) {
      assertTrue(result.get(i).start() <= result.get(i + 1).start());
    }
  }
}
