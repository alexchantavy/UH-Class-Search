package alexchantavy;



/**
 * This exception is thrown when a parsing error is met from the 
 * CourseDatabase class.
 * @author Alex Chantavy
 */
@SuppressWarnings("serial")
public class InvalidFileFormatException extends Exception {

	public InvalidFileFormatException() {
		// TODO Auto-generated constructor stub
		System.out.println("Invalid file type exception");
	}

	public InvalidFileFormatException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	public InvalidFileFormatException(String failureType, String prevLine, String currentLine, int lineNum) {
		System.out.println("Failure to parse " + failureType + " at line " + lineNum+ "\n" +
			"Previous line: "+ prevLine + "\n" + 
			"Current line: "+ currentLine);
	}
	
	public InvalidFileFormatException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidFileFormatException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
