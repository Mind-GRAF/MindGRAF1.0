package exceptions;

public class DirectCycleException extends Exception{

	public DirectCycleException() {
		super();
	}
	
	public DirectCycleException(String s) {
		super(s);
	}
}
