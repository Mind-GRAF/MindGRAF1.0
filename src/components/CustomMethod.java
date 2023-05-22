package components;

import java.util.ArrayList;

public class CustomMethod {
	private String methodName ;
	private String methodCode ;
	private Class<?> returnType ;
	private ArrayList<Class<?>> methodParams;
	private ArrayList<Object> methodArgs ; 
	
	public CustomMethod(String methodName, String methodCode, Class<?> returnType,
			ArrayList<Class<?>> methodParams, ArrayList<Object> methodArgs) {
		super();
		this.methodName = methodName;
		this.methodCode = methodCode;
		this.returnType = returnType;
		if(methodParams!=null)
			this.methodParams = methodParams;
		else
			methodParams = new ArrayList<>();
		if(methodArgs!=null)
			this.methodArgs = methodArgs;
		else
			methodArgs = new ArrayList<>();
	}
	public CustomMethod(String methodName, String methodCode) {
		super();
		this.methodName = methodName;
		this.methodCode = methodCode;
		this.returnType = void.class;
		this.methodParams = new ArrayList<>();
		this.methodArgs = new ArrayList<>();
	}
	public CustomMethod(String methodName, String methodCode,Class<?> returnType) {
		super();
		this.methodName = methodName;
		this.methodCode = methodCode;
		this.returnType = returnType;
		this.methodParams = new ArrayList<>();
		this.methodArgs = new ArrayList<>();
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getMethodCode() {
		return methodCode;
	}
	public void setMethodCode(String methodCode) {
		this.methodCode = methodCode;
	}
	public Class<?> getReturnType() {
		return returnType;
	}
	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}
	public ArrayList<Class<?>> getMethodParams() {
		return methodParams;
	}
	public void setMethodParams(ArrayList<Class<?>> methodParams) {
		methodParams = methodParams;
	}
	public ArrayList<Object> getMethodArgs() {
		return methodArgs;
	}
	public void setMethodArgs(ArrayList<Object> methodArgs) {
		methodArgs = methodArgs;
	}

}
