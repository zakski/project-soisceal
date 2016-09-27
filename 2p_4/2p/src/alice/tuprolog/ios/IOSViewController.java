package alice.tuprolog.ios;

import java.util.ArrayList;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.foundation.NSSet;
import org.robovm.apple.uikit.NSTextAlignment;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIButton;
import org.robovm.apple.uikit.UIButtonType;
import org.robovm.apple.uikit.UIColor;
import org.robovm.apple.uikit.UIControlState;
import org.robovm.apple.uikit.UIEvent;
import org.robovm.apple.uikit.UIFont;
import org.robovm.apple.uikit.UILabel;
import org.robovm.apple.uikit.UITextAutocapitalizationType;
import org.robovm.apple.uikit.UITextAutocorrectionType;
import org.robovm.apple.uikit.UITextSpellCheckingType;
import org.robovm.apple.uikit.UITextView;
import org.robovm.apple.uikit.UITouch;
import org.robovm.apple.uikit.UIView;
import org.robovm.apple.uikit.UIViewController;

import alice.tuprolog.exceptions.InvalidTheoryException;
import alice.tuprolog.exceptions.MalformedGoalException;
import alice.tuprolog.exceptions.NoMoreSolutionException;
import alice.tuprolog.exceptions.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Theory;

public class IOSViewController extends UIViewController 
{
	
	/**
     * @author Alberto Sita
     * 
     */
	
    private final UITextView inputTextView;
    private final UITextView solutionTextView;
    private final UITextView errorsTextView;
    private final UIButton solve;
    private final UIButton clear;
    private final UIButton set;
    private final UIButton next;
    private final UILabel title;
    private final UILabel solution;
    private final UILabel errors;

    private final Prolog tuProlog;
	private SolveInfo info;
	private String result;
	private String incipit = "tuProlog Mobile - " + Prolog.getVersion();
	
    public IOSViewController() 
    {
    	Main application = (Main)UIApplication.getSharedApplication().getDelegate();
    	tuProlog = application.getTuProlog();
    	
        UIView view = getView();
        view.setBackgroundColor(UIColor.white());
        
        inputTextView = new UITextView(new CGRect(20,74,280,175));
        inputTextView.setFont(UIFont.getSystemFont(12));
        inputTextView.setTextAlignment(NSTextAlignment.Left);
        inputTextView.setAutocorrectionType(UITextAutocorrectionType.No);
        inputTextView.setAutocapitalizationType(UITextAutocapitalizationType.None);
        inputTextView.setSpellCheckingType(UITextSpellCheckingType.No);
        inputTextView.setEnablesReturnKeyAutomatically(true);
        basicTextViewLayout(inputTextView, UIColor.black(), true, true);
        
        solutionTextView = new UITextView(new CGRect(20,307,280,141));
        solutionTextView.setFont(UIFont.getSystemFont(12));
        solutionTextView.setTextAlignment(NSTextAlignment.Left);
        basicTextViewLayout(solutionTextView, UIColor.black(), true, false);
        
        errorsTextView = new UITextView(new CGRect(20,488,280,51.5));
        errorsTextView.setFont(UIFont.getSystemFont(12));
        errorsTextView.setTextAlignment(NSTextAlignment.Left);
        basicTextViewLayout(errorsTextView, UIColor.red(), true, false);
        
        solution = new UILabel(new CGRect(20,277,280,24));
        solution.setFont(UIFont.getSystemFont(17));
        solution.setText("Solution");
        solution.setTextAlignment(NSTextAlignment.Center);
        solution.setEnabled(true);
        
        errors = new UILabel(new CGRect(20,456,280,24));
        errors.setFont(UIFont.getSystemFont(17));
        errors.setText("Errors");
        errors.setTextColor(UIColor.red());
        errors.setTextAlignment(NSTextAlignment.Center);
        
        title = new UILabel(new CGRect(20,38,280,24));
        title.setFont(UIFont.getSystemFont(20));
        title.setText("tuProlog Mobile");
        title.setTextColor(UIColor.black());
        title.setTextAlignment(NSTextAlignment.Center);
        
        next = new UIButton(UIButtonType.RoundedRect);
        next.setFrame(new CGRect(257, 252, 43, 30));
        next.setTitle("Next", UIControlState.Normal);
        next.getTitleLabel().setFont(UIFont.getSystemFont(15));
        next.setEnabled(false);
        next.addOnTouchUpInsideListener((control, event) -> {
        	if(inputTextView.isFirstResponder())
        	{
        		inputTextView.resignFirstResponder();
        	}
        	getNextPrologSolution();
        });
        
        clear = new UIButton(UIButtonType.RoundedRect);
        clear.setFrame(new CGRect(20, 252, 40, 30));
        clear.setTitle("Clear", UIControlState.Normal);
        clear.getTitleLabel().setFont(UIFont.getSystemFont(15));
        clear.addOnTouchUpInsideListener((control, event) -> {
        	inputTextView.setText("");
        	solutionTextView.setText(incipit);
    		errorsTextView.setText("");
    		next.setEnabled(false);
    		if(inputTextView.isFirstResponder())
        	{
        		inputTextView.resignFirstResponder();
        	}
        });
        
        set = new UIButton(UIButtonType.RoundedRect);
        set.setFrame(new CGRect(68, 252, 45, 30));
        set.setTitle("Set", UIControlState.Normal);
        set.getTitleLabel().setFont(UIFont.getSystemFont(15));
        set.addOnTouchUpInsideListener((control, event) -> {
        	String input = inputTextView.getText();
        	setTheory(input);
        	if(inputTextView.isFirstResponder())
        	{
        		inputTextView.resignFirstResponder();
        	}
        });
        
        solve = new UIButton(UIButtonType.RoundedRect);
        solve.setFrame(new CGRect(205, 252, 44, 30));
        solve.setTitle("Solve", UIControlState.Normal);
        solve.getTitleLabel().setFont(UIFont.getSystemFont(15));
        solve.addOnTouchUpInsideListener((control, event) -> {
        	String input = inputTextView.getText();
        	solve(input);
        	if(inputTextView.isFirstResponder())
        	{
        		inputTextView.resignFirstResponder();
        	}
        });
        
        solutionTextView.setText(incipit);
        
        view.addSubview(inputTextView);
        view.addSubview(solutionTextView);
        view.addSubview(errorsTextView);
        
        view.addSubview(title);
        view.addSubview(errors);
        view.addSubview(solution);
        
        view.addSubview(clear);
        view.addSubview(set);
        view.addSubview(solve);
        view.addSubview(next);
    }
    
    private void setTheory(String theory) 
    {
    	if (theory != null && theory != "") 
    	{
			try 
			{
				tuProlog.setTheory(new Theory(theory));
		    	solutionTextView.setText("Theory set!");
				clearErrors();
			} 
			catch (InvalidTheoryException e) 
			{
				errorsTextView.setText("Error setting theory: Syntax Error at/before line " + e.line);
			}
			catch (Exception e)
			{
				errorsTextView.setText("Error: "+theory+" is an invalid theory!");
			}
    	} 
    	else
    	{
    		errorsTextView.setText("WARNING: Theory is empty");
    	}
    }
    
    private void clearErrors() 
    {
    	errorsTextView.setText("");
	}

	public void solve(String goal)
    {	
    	result = "";
    	clearErrors();
        if (!goal.equals(""))
        {
            try
            {
                solveGoal(goal);
            } 
            catch (Exception e) 
            {
                errorsTextView.setText("Error: " + e);
                if(next.isEnabled())
                {
                	next.setEnabled(false);
                }
            }   
        }
        else if (goal.equals(""))
        {
            result += "Ready.";
        }
        
        solutionTextView.setText(result);
        solutionTextView.scrollRangeToVisible(new NSRange(solutionTextView.getText().length(), 0));
    }
    
    private void solveGoal(String goal)
    {
    	result = "";
    	clearErrors();
    	try 
    	{
        	info = tuProlog.solve(goal);
         	
        	if (tuProlog.isHalted())
        	{
        		result += "halt.";
        	}
            if (!tuProlog.isHalted() && !info.isSuccess()) 
            {      		
        		if(info.isHalted())
        		{
        			result += "halt.";
        		}
        		else
        		{
	                result += "no.";
        		}
            } 
            else
            {
                if (!tuProlog.hasOpenAlternatives()) 
                {
                    String binds = info.toString();
                    if (binds.equals("")) 
                    {
                        result += "yes.";
                    } 
                    else
                    {
                    	result += solveInfoToString(info);
                    }
                    next.setEnabled(false);
                } 
                else 
                {
                	result += solveInfoToString(info);
                	next.setEnabled(true);
                }
            }
    	} 
    	catch (MalformedGoalException ex) 
    	{
    		errorsTextView.setText("Syntax Error: malformed goal.");
    		if(next.isEnabled())
            {
            	next.setEnabled(false);
            }
    	}
    	catch (NoSolutionException ex)
    	{
    		errorsTextView.setText("No solution is available!");
    	}
    }  
    
    private String solveInfoToString(SolveInfo result) throws NoSolutionException 
    {
    	return result.toString() + "\n" + result.getSolution().toString() + "\n\n";
    }
    
    public void getNextPrologSolution()
    {
    	if (info !=null && info.hasOpenAlternatives()) 
    	{
    		try 
    		{
		        info = tuProlog.solveNext();
		   
		        if (!info.isSuccess()) 
		        {
		            result += "no.\n";
		        } 
		        else
		        {
		        	result += solveInfoToString(info);
		        }
		    } 
    		catch (NoMoreSolutionException ex) 
    		{
		        result += "no.";
		    }
    		catch (NoSolutionException ex)
        	{
    			errorsTextView.setText("No solution is available!");
        	}
    		finally
    		{
    			if(!info.hasOpenAlternatives())
		        {
		        	next.setEnabled(false);
		        }
    		}
    	}
    			
    	solutionTextView.setText(result);
    	solutionTextView.scrollRangeToVisible(new NSRange(solutionTextView.getText().length(), 0));
    }
    
  	public void basicTextViewLayout(UITextView textView, UIColor color, boolean interaction, boolean editable)
    {
  		textView.setSelectable(true);
  		textView.setScrollEnabled(true);
  		textView.setEditable(editable);
      	textView.setTextColor(color);
      	textView.setUserInteractionEnabled(interaction);
  		textView.setHidden(false);
      	textView.getLayer().setCornerRadius(Main.cornerRadius);
  		textView.getLayer().setBorderWidth(Main.borderWidth);
    }
  	
  	@Override
    public void touchesBegan(NSSet<UITouch> touches, UIEvent event)
    {
  		testKeyboard();
  		super.touchesBegan(touches, event);
    }
  	
  	private void testKeyboard()
	{
		if(inputTextView.isFirstResponder())
    	{
    		inputTextView.resignFirstResponder();
    		solutionTextView.setText("");
    		errorsTextView.setText("");
    	}
	}
  	
  	private String parseURL(String url)
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

	public void show(String url) 
	{
		solutionTextView.setText(parseURL(url));
	}
}
