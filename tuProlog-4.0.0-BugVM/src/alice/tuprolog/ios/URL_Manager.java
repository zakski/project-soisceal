package  alice.tuprolog.ios;

import java.util.ArrayList;
import java.util.HashMap;

import com.bugvm.apple.dispatch.DispatchQueue;
import com.bugvm.apple.foundation.NSStringEncoding;
import com.bugvm.apple.foundation.NSURL;
import com.bugvm.apple.uikit.UIApplication;

import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Theory;
import alice.tuprolog.exceptions.InvalidTheoryException;
import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoMoreSolutionException;

public class URL_Manager implements I_URL_Manager
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	private static NSURL currentUrl = null;
	private HashMap<String, Prolog> urlServersEngineMap = null;
	private IOSViewController viewController = null;
	
	public URL_Manager(IOSViewController viewController)
	{
		this.urlServersEngineMap = new HashMap<String, Prolog>();
		this.viewController = viewController;
	}
	
	@Override
	public void handleRequest(String urlString) 
	{
		ArrayList<String> tokens = tokenizer(urlString);
		
		if(isParsableURL(tokens))
		{
			String sourceApp = tokens.get(0).split("\\=")[1];
			String action = tokens.get(1).split("\\=")[0];
			String value = tokens.get(1).subSequence(action.length()+1, tokens.get(1).length()).toString();
			
			value = value.replace("&&", "&"); // && replaced with &
			
			String destinationApp = tokens.get(2).split("\\=")[1];
			
			if(!this.urlServersEngineMap.containsKey(sourceApp))
			{
				if(!action.equals("theory") && !action.equals("query"))
					errorReply(sourceApp, action+" is not valid.");
				else 
				{
					this.urlServersEngineMap.put(sourceApp, createURLServer());
					if(action.equals("theory"))
						setTheoryOnURLServer(sourceApp, destinationApp, value, "new");
					else
						solveQueryOnURLServer(sourceApp, destinationApp, value, "new");
				}	
			}
			else
			{
				if(action.equals("theory"))
					setTheoryOnURLServer(sourceApp, destinationApp, value, "still");
				else if(action.equals("query"))
					solveQueryOnURLServer(sourceApp, destinationApp, value, "still");
				else if(action.equals("nextSol"))
					getNextSolOnUrlServer(sourceApp, destinationApp, value);
				else if(action.equals("reset"))
				{
					this.urlServersEngineMap.put(sourceApp, createURLServer());
					resetIsOk(destinationApp);
				}
				else
					errorReply(sourceApp, action+" is not valid.");
			}
		}
	}
	
	private Prolog createURLServer() 
	{
		return new Prolog();
	}
	
	private ArrayList<String> tokenizer(String url) 
	{
		ArrayList<String> tokens = new ArrayList<String>();
		String token = "";
		boolean significant = false;
		for(int i=0; i<url.length(); i++)
		{
			if(url.charAt(i) =='?')
			{
				significant = true;
				continue;
			}
				
			if(significant)
			{
				if(url.charAt(i) =='&')
				{
					if((i+1<url.length() && url.charAt(i+1) =='&')||url.charAt(i-1) =='&')
						token = token+url.charAt(i);
					else
					{
						tokens.add(token);
						token = "";
					}
				}
				else
					token = token+url.charAt(i);
			}
		}
		if(!token.isEmpty())
			tokens.add(token);
		return tokens;
	}
	
	private boolean isParsableURL(ArrayList<String> urlSubParts)
	{
		String url = "";
		if(urlSubParts.size()!=3)
		{
			url = "null://?error=url format&incident=URL length not valid.";
			decide("null", url);
			return false;
		}
			
		if(!(urlSubParts.get(0).contains("src=") && urlSubParts.get(0).split("\\=").length==2))
		{
			url = "null://?error=url format&incident=sourceApp is not well formatted.";
			decide("null", url);
			return false;
		}
			
		if(!(urlSubParts.get(1).contains("theory=") || urlSubParts.get(1).contains("query=") || urlSubParts.get(1).contains("nextSol=") || urlSubParts.get(1).contains("reset=")))
		{
			url = "null://?error=url format&incident=action is not well formatted.";
			decide("null", url);
			return false;
		}
			
		if(!(urlSubParts.get(2).contains("dst=") && urlSubParts.get(2).split("\\=").length==2))
		{
			url = "null://?error=url format&incident=destinationApp is not well formatted.";
			decide("null", url);
			return false;
		}
			
		String destApp = urlSubParts.get(2).split("\\=")[1];
		String sourceApp = urlSubParts.get(0).split("\\=")[1];
			
		if(!destApp.equals("null") && !testURL(destApp))
			return false;
			
		if(!sourceApp.equals("null") && !testURL(sourceApp))
			return false;
			
		return true;
	}
	
	private boolean testURL(String app) 
	{
		String url = "";
		String URL = NSURL.encodeURLString(app+"://", NSStringEncoding.UTF8);
		NSURL nsURL = new NSURL(URL);
		if(!UIApplication.getSharedApplication().canOpenURL(nsURL))
		{
			url = "null://?error=cannot open url&incident="+app+"://";
			decide("null", url);
			return false;
		}
		return true;
	}
		
	private void resetIsOk(String destinationApp)
	{
		String url = destinationApp+"://?reset=OK";
		decide(destinationApp, url);
	}
	
	private void errorReply(String sourceApp, String error) 
	{
		String url = sourceApp+"://?error="+error;
		decide(sourceApp, url);
	}
	
	private void consoleDisplay(String url) 
	{
    	DispatchQueue.getMainQueue().async(new Runnable() 
    	{
    		@Override
    		public void run() 
    		{
    	    	viewController.show(url);
    		}
    	});
	}
	
	private void getNextSolOnUrlServer(String sourceApp, String destinationApp, String value) 
	{
		Prolog prolog = this.urlServersEngineMap.get(sourceApp);
		SolveInfo info;
		String result="";
		try 
		{
			info = prolog.solveNext();
			result = info.toString();
		}
		catch (NoMoreSolutionException e) 
		{
			result = "no.";
		}
		String engineVersion = "tuProlog Mobile v. "+Prolog.getVersion();
		String url = destinationApp+"://?nextSol="+result.replace("&", "&&")+"&engine="+engineVersion;
		decide(destinationApp, url);
	}
		
	private void decide(String destinationApp, String url)
	{
		if(destinationApp.equals("null"))
			consoleDisplay(url);
		else
			URLResponse(url);
	}
	
	private void solveQueryOnURLServer(String sourceApp, String destinationApp, String value, String age) 
	{
		Prolog prolog = this.urlServersEngineMap.get(sourceApp);
		String solution = "";
		String errors = "";
		String engineVersion = "tuProlog Mobile v. "+Prolog.getVersion();
		SolveInfo info;
		try 
		{
			info = prolog.solve(value);
			solution = info.toString();
		} 
		catch (MalformedGoalException e) 
		{
			errors = e.getMessage();
		}
		
		String url = destinationApp+"://?solution="+solution.replace("&", "&&")+"&errors="+errors+"&engine="+engineVersion+"&engineAge="+age;
		decide(destinationApp, url);
	}
	
	private void setTheoryOnURLServer(String sourceApp, String destinationApp, String value, String age) 
	{
		String result;
		Prolog prolog = this.urlServersEngineMap.get(sourceApp);
		try 
		{
			prolog.setTheory(new Theory(value));
			result="Theory set! :-)";
		} 
		catch (InvalidTheoryException e) 
		{
			result="Error is setting the theory: "+e.getMessage();
		}
		String engineVersion = "tuProlog Mobile v. "+Prolog.getVersion();
		String url = destinationApp+"://?result="+result+"&engine="+engineVersion+"&engineAge="+age;
		decide(destinationApp, url);
	}
	
	private void URLResponse(String url) 
	{
		String URL = NSURL.encodeURLString(url, NSStringEncoding.UTF8);
		URL_Manager.currentUrl = new NSURL(URL);
		
		DispatchQueue.getMainQueue().async(new Runnable() 
		{
			@Override
	        public void run() 
	        {
				UIApplication.getSharedApplication().openURL(URL_Manager.currentUrl);
	        }
	    });
	}
}