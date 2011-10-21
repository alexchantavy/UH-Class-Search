/**
 * 
 */
package alexchantavy;

/**
 * This exception is thrown when CourseDatabase tries to create a new
 * instance from an unrecognized department.
 * @see alexchantavy.CourseDatabase#CourseDatabase(String)
 * @author Alex Chantavy
 */
@SuppressWarnings("serial")
public class NoSuchDepartmentException extends Exception {

	/**
	 * Default constructor
	 */
	public NoSuchDepartmentException() {
		System.out.println("No such department");
	}

	/**
	 * @param message
	 */
	public NoSuchDepartmentException(String message) {
		System.out.println("No such deparment:"+ message);
	}

	/**
	 * @param cause
	 */
	public NoSuchDepartmentException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoSuchDepartmentException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
