package highlighting.regex;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaTokens;
import java.util.ArrayList;
import java.util.List;

public class RegexHighlighter extends SyntaxHighlighter {

    // -------------------------------------------------------------------------
    // Helper - Methods
    // -------------------------------------------------------------------------
    private boolean overlapsAny(HighlightRegion candidate, List<HighlightRegion> accepted) {
        for (HighlightRegion kept : accepted) {
            if (candidate.start() < kept.end() && kept.start() < candidate.end()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<HighlightRegion> collectMatches(String text) {
        List<HighlightRegion> all = new ArrayList<>();
        for (Token token : MiniJavaTokens.defaultTokens()) {
            all.addAll(token.test(text));
        }
        return all;
    }

    @Override
    public List<HighlightRegion> resolveConflicts(List<HighlightRegion> regions) {
        List<HighlightRegion> result = new ArrayList<>();
        for (HighlightRegion candidate : regions) {
            if (!overlapsAny(candidate, result)) {
                result.add(candidate);
            }
        }
        return result;
    }
}
