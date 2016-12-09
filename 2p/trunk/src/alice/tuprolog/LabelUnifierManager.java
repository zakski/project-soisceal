package alice.tuprolog;

import java.util.Hashtable;


public class LabelUnifierManager implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	
	
	private static Hashtable<String, LabelUnifier> labelUnifiers;
	private static String currentLabelUnifierName;

	public void initialize() {
		
		labelUnifiers=new Hashtable<String, LabelUnifier>();
	}
	
	public static LabelUnifier getLabelUnifier(){
		if(currentLabelUnifierName != null && labelUnifiers.containsKey(currentLabelUnifierName)){
			synchronized(labelUnifiers){
				return labelUnifiers.get(currentLabelUnifierName);
			}
		}
		//System.out.println("LABEL UNIFIER MANAGER currentLabelUnifierName "+currentLabelUnifierName);
		try{
			Class<?> clazz = Class.forName(currentLabelUnifierName);
			LabelUnifier lu = (LabelUnifier) clazz.newInstance();
			synchronized (labelUnifiers){
				labelUnifiers.put(currentLabelUnifierName, lu);
				return lu;
			}
		}catch(Exception e){
			System.out.println("LABEL UNIFIER MANAGER eccezione");
		}
		return null;
	}
	
//	public static LabelUnifier getLabelUnifier(){
//		if(!runnersUnifiers.containsKey(tid))
//			return runnersUnifiers.elements().nextElement();
//		synchronized(runnersUnifiers){
//			return runnersUnifiers.get(tid);
//		}
//	}
	
	public String getCurrentLabelUnifierName() {
		return currentLabelUnifierName;
	}
	

	public void setCurrentLabelUnifierName(String currentLabelUnifierName) {
		LabelUnifierManager.currentLabelUnifierName = currentLabelUnifierName;
		//System.out.println("Ho settatto  il nome del currentLabelUnifier "+currentLabelUnifierName);
	}
}
