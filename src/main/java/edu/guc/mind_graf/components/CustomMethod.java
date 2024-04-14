package edu.guc.mind_graf.components;

import java.util.ArrayList;

public class CustomMethod {
	private String methodName;
	private String methodCode;
	private Class<?> returnType;
	private ArrayList<Class<?>> methodParams;
	private ArrayList<String> methodArgs;

	public CustomMethod(String methodName, String methodCode, Class<?> returnType,
			ArrayList<Class<?>> methodParams, ArrayList<String> methodArgs) {
		super();
		this.methodName = methodName;
		this.methodCode = methodCode;
		if (returnType == null)
			this.returnType = void.class;
		else
			this.returnType = returnType;
		if (methodParams != null)
			this.methodParams = methodParams;
		else
			methodParams = new ArrayList<>();
		if (methodArgs != null)
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

	public CustomMethod(String methodName, String methodCode, Class<?> returnType) {
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
		this.methodParams = methodParams;
	}

	public ArrayList<String> getMethodArgs() {
		return methodArgs;
	}

	public void setMethodArgs(ArrayList<String> methodArgs) {
		this.methodArgs = methodArgs;
	}

}
