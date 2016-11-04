package alice.tuprolog;

import alice.tuprolog.Prolog;

public class TestEinsteinRiddle {
    
    public static void main(String[] args) {
        String[] str = new String[2];
        str[0] = "./alice/tuprolog/einsteinsRiddle.pl";
        str[1] = "einstein(_,X), write(X).";
        Prolog.main(str);
    }
    
}
