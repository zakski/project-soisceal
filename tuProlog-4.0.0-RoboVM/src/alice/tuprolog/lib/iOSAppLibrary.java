package  alice.tuprolog.lib;

import org.robovm.apple.dispatch.DispatchQueue;
import org.robovm.apple.foundation.NSStringEncoding;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIApplication;

import alice.tuprolog.Library;
import alice.tuprolog.Term;

/*
 * -> Mail
 * 		- mailTo('foo@foo.it').
 * 
 * -> FaceTime
 * 		- faceTime('foo@foo.it').
 * 		- faceTime(3920000000).
 *      - faceTime('+39 3920000000').
 *      - faceTimeAudio('foo@foo.it').
 * 		- faceTimeAudio(3920000000).
 *      - faceTimeAudio('+39 3920000000').
 *      
 * -> SMS
 * 		- sms('foo@foo.it'). //if iMessage is on, is automatically selected by the iPhone as sending option
 * 		- sms(3920000000).
 * 		- sms('+39 3920000000').
 * 
 * -> Phone
 * 		- phoneCall(3920000000). //cellular call
 *      - phoneCall('+39 3920000000'). //cellular call
 *      
 */

public class iOSAppLibrary extends Library
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	private static final long serialVersionUID = 1L;
	
	private static NSURL URL = null;
	
	public boolean mailTo_1(Term mailTo)
	{
		String URL = NSURL.encodeURLString("mailto:"+mailTo.toString().substring(1, mailTo.toString().length()-1), NSStringEncoding.UTF8);
		return Call(URL);
	}
	
	public boolean faceTime_1(Term toVideoCall)
	{
		String URL = "";
		if(toVideoCall.toString().startsWith("'"))
			URL = NSURL.encodeURLString("facetime://"+toVideoCall.toString().substring(1, toVideoCall.toString().length()-1), NSStringEncoding.UTF8);
		else
			URL = NSURL.encodeURLString("facetime://"+toVideoCall, NSStringEncoding.UTF8);
		return Call(URL);
	}
	
	public boolean faceTimeAudio_1(Term toCall)
	{
		String URL = "";
		if(toCall.toString().startsWith("'"))
			URL = NSURL.encodeURLString("facetime-audio://"+toCall.toString().substring(1, toCall.toString().length()-1), NSStringEncoding.UTF8);
		else
			URL = NSURL.encodeURLString("facetime-audio://"+toCall, NSStringEncoding.UTF8);
		return Call(URL);
	}
	
	public boolean sms_1(Term toText)
	{
		String URL = "";
		if(toText.toString().startsWith("'"))
			URL = NSURL.encodeURLString("sms:"+toText.toString().substring(1, toText.toString().length()-1), NSStringEncoding.UTF8);
		else
			URL = NSURL.encodeURLString("sms:"+toText, NSStringEncoding.UTF8);
		return Call(URL);
	}
	
	public boolean phoneCall_1(Term numberToCall)
	{
		String URL = "";
		if(numberToCall.toString().startsWith("'"))
			URL = NSURL.encodeURLString("tel:"+numberToCall.toString().substring(1,numberToCall.toString().length()-1), NSStringEncoding.UTF8);
		else
			URL = NSURL.encodeURLString("tel:"+numberToCall, NSStringEncoding.UTF8);
		return Call(URL);
	}
	
	private boolean Call(String url) 
	{
		String uRL = NSURL.encodeURLString(url, NSStringEncoding.UTF8);
		iOSAppLibrary.URL = new NSURL(uRL);

		DispatchQueue.getMainQueue().async(new Runnable() 
		{
			@SuppressWarnings("deprecation")
			@Override
		    public void run() 
		    {
		        UIApplication.getSharedApplication().openURL(iOSAppLibrary.URL);
		    }
		});
		return true;
	}
}
