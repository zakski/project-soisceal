package alice.tuprolog.lib.sharedclasses;

import alice.tuprolog.Term;

public interface iOSAppLibraryInterface 
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	boolean mailTo_1(Term mailTo);
	boolean faceTime_1(Term toVideoCall);
	boolean faceTimeAudio_1(Term toCall);
	boolean sms_1(Term toText);
	boolean phoneCall_1(Term numberToCall);
	
}
