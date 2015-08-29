package alice.tuprolog;

public class TestEinsteinRiddle {
    
    public static void main(String[] args) {
        String[] str = new String[2];
        str[0] = "./alice/tuprolog/einsteinsRiddle.pl";
        str[1] = "einstein(_,X), write(X).";
//        if (args.length==1 || args.length==2){
//
//            //FileReader fr;
//            try {
//                String text = Tools.loadText(args[0]);
//                if (args.length==1){
//                    new Agent(text).spawn();
//                } else {
//                    new Agent(text,args[1]).spawn();
//                }
//            } catch (Exception ex){
//                System.err.println("invalid theory.");
//            }
//        } else {
//            System.err.println("args: <theory file> { goal }");
//            System.exit(-1);
//        }
//        alice.tuprolog.core.Agent.main(str);
    }
    
}
