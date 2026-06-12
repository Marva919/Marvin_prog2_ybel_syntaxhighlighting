package highlighting.antlr;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaColours;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.*;

public class AntlrTokenCollector extends SyntaxHighlighter {

  @Override
  public List<HighlightRegion> collectMatches(String text) {
    CharStream input = CharStreams.fromString(text);
    MiniJavaLexer lexer = new MiniJavaLexer(input);
    lexer.removeErrorListeners();

    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    tokenStream.fill();

    List<Token> tokens = tokenStream.getTokens();
    List<HighlightRegion> regions = new ArrayList<>();

    for (int i = 0; i < tokens.size(); i++) {
      Token t = tokens.get(i);

      if (t.getType() == Token.EOF) continue;

      Color colour = colourFor(t);

      if (colour != null) {
        int start = t.getStartIndex();
        int end = t.getStopIndex() + 1;
        regions.add(new HighlightRegion(start, end, colour));
      }

      if (t.getType() == MiniJavaLexer.AT) {
        if (i + 1 < tokens.size()) {
          Token next = tokens.get(i + 1);
          if (next.getType() == MiniJavaLexer.IDENTIFIER) {
            regions.remove(regions.size() - 1);
            int start = t.getStartIndex();
            int end = next.getStopIndex() + 1;
            regions.add(new HighlightRegion(start, end, MiniJavaColours.ANNOTATION_COLOUR));
            i++;
          }
        }
      }
    }

    return regions;
  }

  private static Color colourFor(Token t) {
    return switch (t.getType()) {
      case MiniJavaLexer.STRING_LITERAL -> MiniJavaColours.STRING_LITERAL_COLOUR;
      case MiniJavaLexer.CHAR_LITERAL -> MiniJavaColours.CHAR_LITERAL_COLOUR;
      case MiniJavaLexer.JAVADOC_COMMENT -> MiniJavaColours.JAVADOC_COMMENT_COLOUR;
      case MiniJavaLexer.BLOCK_COMMENT -> MiniJavaColours.BLOCK_COMMENT_COLOUR;
      case MiniJavaLexer.LINE_COMMENT -> MiniJavaColours.LINE_COMMENT_COLOUR;
      case MiniJavaLexer.AT -> MiniJavaColours.ANNOTATION_COLOUR;
      case MiniJavaLexer.PACKAGE,
          MiniJavaLexer.IMPORT,
          MiniJavaLexer.CLASS,
          MiniJavaLexer.PUBLIC,
          MiniJavaLexer.PRIVATE,
          MiniJavaLexer.FINAL,
          MiniJavaLexer.RETURN,
          MiniJavaLexer.NULL,
          MiniJavaLexer.NEW,
          MiniJavaLexer.IF,
          MiniJavaLexer.ELSE,
          MiniJavaLexer.WHILE,
          MiniJavaLexer.EXTENDS,
          MiniJavaLexer.IMPLEMENTS ->
          MiniJavaColours.KEYWORD_COLOUR;
      default -> null;
    };
  }
}
