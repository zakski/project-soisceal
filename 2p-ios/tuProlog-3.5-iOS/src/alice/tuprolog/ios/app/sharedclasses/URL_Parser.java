package alice.tuprolog.ios.app.sharedclasses;

import java.util.ArrayList;

public class URL_Parser implements I_URL_Parser 
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
	public String parseURL(String url)
	{
		ArrayList<String> tokens = tokenizer(url);
		String incipit = "External invocation";
		String action = tokens.get(0).split("\\=")[0];
		
		if(tokens.size() == 1)
		{
			if(action.equals("error"))
			{
				String why = tokens.get(0).subSequence(action.length()+1, tokens.get(0).length()).toString();
				return incipit+"\n\nError: "+why;
			}
			else
				return incipit+"\n\nServer Engine resetted.";
		}
		else if (tokens.size() == 2)
		{
			if(action.equals("nextSol"))
			{
				String nextSol = tokens.get(0).subSequence(action.length()+1, tokens.get(0).length()).toString();
				nextSol = nextSol.replace("&&", "&");
				String engine = tokens.get(1).split("\\=")[1];
				
				return incipit+"\n\nNext solution: "+nextSol.trim()+"\n\nServer Engine Version:\n"+engine;
			}
			else
			{
				String error = tokens.get(0).subSequence(action.length()+1, tokens.get(0).length()).toString();
				String incident  = tokens.get(1).split("\\=")[1];
				
				if (error.equals("url format"))
					return incipit+"\n\nParsing error: "+incident;
				else
					return incipit+"\n\nURL error: "+incident+" is not a valid URL scheme.";
			}
		}
		else if (tokens.size() == 3)
		{
			String result = tokens.get(0).subSequence(action.length()+1, tokens.get(0).length()).toString();
			String engine = tokens.get(1).split("\\=")[1];
			String age = tokens.get(2).split("\\=")[1];
			
			if(age.equals("new"))
				return incipit+"\n\n"+result.trim()+"\n\nServer Engine Version:\n"+engine;
			else
				return incipit+"\n\n"+result.trim()+"\n\nServer Engine Version:\n"+engine+"\n\n(Server Engine is in cache).";
		}
		else if (tokens.size() == 4)
		{
			String solution = tokens.get(0).subSequence(action.length()+1, tokens.get(0).length()).toString();
			solution = solution.replace("&&", "&");
			String[] errors = tokens.get(1).split("\\=");
			String engine = tokens.get(2).split("\\=")[1];
			String age = tokens.get(3).split("\\=")[1];
			
			if(age.equals("new"))
			{
				if(errors.length==1)
					return incipit+"\n\n"+solution.trim()+"\n\nServer Engine Version:\n"+engine;
				else
					return incipit+"\n\n"+errors[1].trim()+"\n\nServer Engine Version:\n"+engine;
			}
			else
			{
				if(errors.length==1)
					return incipit+"\n\n"+solution.trim()+"\n\nServer Engine Version:\n"+engine+"\n\n(Server Engine is in cache).";
				else
					return incipit+"\n\n"+errors[1].trim()+"\n\nServer Engine Version:\n"+engine+"\n\n(Server Engine is in cache).";
			}
		}
		return "INTERNAL ERROR!";
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
					token = token +url.charAt(i);
			}
		}
		if(!token.isEmpty())
			tokens.add(token);
		return tokens;
	}
}
