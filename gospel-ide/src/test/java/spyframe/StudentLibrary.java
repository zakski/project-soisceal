package spyframe;
// franz.beslmeisl at googlemail.com

import com.szadowsz.gospel.core.db.Library;
import alice.tuprologx.spyframe.TermFrame;
import com.szadowsz.gospel.core.data.Term;

// import in a prolog session by
// :-load_library('StudentLibrary').
class StudentLibrary extends Library {
    public boolean termframe_1(Term term) {
        new TermFrame(term);
        return true;
    }
}
