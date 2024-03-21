package edu.guc.mind_graf.components;

public class CustomConstructor {
	private String name ; 
	private Class<?>[] constructorParams; 
	private Object[] constructorArgs;
	public CustomConstructor(String name , Class<?>[] constructorParams, String[] constructorArgs) {
		this.name = name ; 
		this.constructorArgs = constructorArgs;
		this.constructorParams = constructorParams;
	}
	
	public  String constructorToString(){
		String Params = "";
		String Args = "";
		for (int i = 0; i < constructorArgs.length; i++) {
			Params+= constructorParams[i].getSimpleName() + " " + constructorArgs[i] + 
					(i == constructorArgs.length -1 ? " " : ",") ;  
		}
		int k = 0  ;
		for (Object object : constructorArgs) {
			Args += object + (k == constructorArgs.length -1 ? " " : ",") ; 
			k++;
		}
		String result = 
		"\n" + " public " + name + "("
		+Params +  ")" + "{" + "\n" + "super("
		+Args + ");" + "\n" + "}";
		return result ; 
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?>[] getConstructorParams() {
		return constructorParams;
	}

	public void setConstructorParams(Class<?>[] constructorParams) {
		this.constructorParams = constructorParams;
	}

	public Object[] getConstructorArgs() {
		return constructorArgs;
	}

	public void setConstructorArgs(Object[] constructorArgs) {
		this.constructorArgs = constructorArgs;
	}

}
