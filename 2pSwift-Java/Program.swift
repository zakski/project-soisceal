import java.util
import alice.tuprolog
import java.io

var Engine: Prolog
var Result: String
var Info: SolveInfo

func init_Prolog()
{
	if (Engine == nil)
	{
		Engine = Prolog()
		println("\ntuProlog system - release " + Prolog.getVersion() + "\n")
	}
}

func setTheory(theory: String) 
{
	if (theory != nil && theory != "")
	{
		Engine.setTheory(Theory(theory))
		println("Theory set!")
	} 
	else
	{
		println("WARNING: Theory is empty")
	}
}
 
func solve(goal: String)
{	
	Result = "";
	if (!goal.equals(""))
	{
		Result += "Solving..."
		solveGoal(goal)
	}
	else if (goal.equals(""))
	{
		Result += "Ready."
	}
}
	
func solveGoal(goal: String)
{
	Result = ""
	Info = Engine.solve(goal)
	if (Engine.isHalted())
	{
		System.exit(0)
	}
	if (!Info.isSuccess()) 
	{			  
		if(Info.isHalted())
		{
			Result += "halt."
		}
		else
		{
			Result += "no."
			println("Info Value: "+Info.isSuccess()) // attenzione! qualcosa non va xk esce qui...
		}
	} 
	else
	{
		if (!Engine.hasOpenAlternatives())
		{
			var binds: String = Info.toString()
			if (binds.equals("")) 
			{
				Result += "yes."
			} 
			else
			{
				Result += solveInfoToString(Info) + "\nyes."
			}
		} 
		else 
		{
			Result += solveInfoToString(Info) + " ? "
		}
	} 
} 

func solveInfoToString(result: SolveInfo) -> String
{
	var s: String = ""
	for v in result.getBindingVars()
	{
		if (!v.isAnonymous() && v.isBound())
		{
			let tmpV = v.getTerm() as! Var
			println(tmpV)
			if(!(v.getTerm() is Var) || !(tmpV.getName().startsWith("_")))
			{
				s += v.getName() + " / " + v.getTerm() + "\n"
			}
		}
	}
	if(s.length()>0)
	{
		s.substring(0,s.length()-1)
	} 
	return s
}

func getNextSolution()
{
	if (Info.hasOpenAlternatives()) 
	{
		Info = Engine.solveNext()
		if (!Info.isSuccess()) 
		{
			Result += "no.\n"
		} 
		else
		{
			Result += solveInfoToString(Info) + " ? "
		} 
	}
}

func Program()
{
	init_Prolog()
	
	var reader: InputStreamReader = InputStreamReader(System.`in`)
	var bReader: BufferedReader = BufferedReader(reader)
	
	var terminateApplication = false
	
	while(!terminateApplication)
	{
		print("Digita t per inserire una Teoria g per risolvere un Goal (e per terminare): ")
		var userInput = bReader.readLine()
		if(userInput.equalsIgnoreCase("t") || userInput.equalsIgnoreCase("g"))
		{
			if(userInput.equalsIgnoreCase("t"))
			{
				println("Inserisci la Teoria: ")
				var th = bReader.readLine()
				setTheory(th)
			}
			else
			{
				println("Inserisci un Goal: ")
				var go = bReader.readLine()
				solve(go)
				println(Result)
			}
		}
		else if (userInput.equalsIgnoreCase("e"))
		{
		   println("Termino applicazione!")
		   terminateApplication = true 
		   bReader.close()
		   reader.close()
		}
	}
}

Program()
