package alexchantavy;

/**
 * This class encapsulates fields to describe a Course Meeting Time, 
 * namely, days available, start time, end time, location, and dates.
 * Note that one course may have multiple MeetingTimes.  Such an occurrence
 * makes this class necessary.
 * @author Alex Chantavy
 *
 */
public class MeetingTime {

	private String days, startTime, endTime, location, dates;
	
	/**
	 * Creates a MeetingTime from the given strings
	 * @param days The days the course is offered
	 * @param startTime The time the course starts
	 * @param endTime The time the course ends
	 * @param location Where this meeting time is
	 * @param dates What dates this course applies for
	 */
	public MeetingTime(String days, String startTime, String endTime,
			String location, String dates) {
		super();
		this.days = days;
		this.startTime = startTime;
		this.endTime = endTime;
		this.location = location;
		this.dates = dates;
	}
	
	/**
	 * Returns the location of this MeetingTime
	 * @return String telling the location of the meeting time
	 */
	public String getLocation() {
		return this.location;
	}
	
	/**
	 * Returns the start time
	 * @return The start time of the course
	 */
	public String getStartTime() {
		return this.startTime;
	}
	
	/**
	 * Returns the days the course is offered
	 * @return String of the days the course is offered 
	 */
	public String getDays() {
		return this.days;
	}
	
	/**
	 * Returns a 'pretty' string of the meeting time
	 * @return A 'pretty' string of the meeting time
	 */
	public String prettyString() {
		return "Days: "+ days + "\n" +
				"Time: " + startTime + endTime +"\n" +
				"Location:" + location + "\n" + 
				"Dates:" + dates;
	}
	
	@Override
	public String toString() {
		return days + "\n" +
				startTime + "\n" + 
				endTime +"\n" +
				location + "\n" + 
				dates;
	}
}
