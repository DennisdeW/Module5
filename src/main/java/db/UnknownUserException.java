package db;

public class UnknownUserException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnknownUserException() {
		super();
	}

	public UnknownUserException(String message) {
		super(message);
	}
}
