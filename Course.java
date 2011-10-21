package alexchantavy;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains data fields of a typical college course.  Members include 
 * department, focus, course number, course abbreviation, section number,
 * course title, instructor name, number of available seats, number of credits,
 * and meeting times.
 * @see alexchantavy.MeetingTime
 * @author Alex Chantavy
 *
 */
public class Course {
	private String department, focus, crn, course_abbreviation, section,
			title, instructor, seats, credits;
	private LinkedList<MeetingTime> meetingTimes;
	
	/**
	 * Determines whether the given course is an online course by
	 * checking each of the course's MeetingTime.location strings
	 * for the words "WWW" or "ONLINE".
	 * @param c The given course
	 * @return Whether or not Course c is online.
	 */
	public static boolean isOnline(Course c) {
		for (int i = 0; i < c.meetingTimes.size(); i++) {
			MeetingTime temp = c.meetingTimes.get(i);
			if (temp.getLocation().contains("ONLINE") ||
					temp.getLocation().contains("WWW")) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a course object without the need for a MeetingTime object.
	 * @param department The department of the course (e.g, ICS)
	 * @param focus The course's focus (e.g., OC, WI)
	 * @param crn The course number (e.g., 123456)
	 * @param course The course abbreviation (e.g., ICS 111)
	 * @param section The section number (e.g., 001)
	 * @param title The course title (e.g., "Introduction to Computer Science")
	 * @param instructor The instructor's name (e.g., R. Narayan)
	 * @param seatsAvail Number of seats available (e.g., 10) 
	 * @param ncredits The number of credits (e.g., 4)
	 */
	public Course(String department, String focus, String crn, String course,
			String section, String title, String instructor, String seatsAvail,
			String ncredits)  {
		this.department = department;
		this.focus = focus;
		this.crn = crn;
		this.course_abbreviation = course;
		this.section = section;
		this.title = title;
		this.instructor = instructor;
		this.seats = seatsAvail;
		this.credits = ncredits;
		meetingTimes = new LinkedList<MeetingTime>();
	}

	/**
	 * Returns the course abbreviation
	 * @return The course abbreviation
	 */
	public String getCourseAbbrev () {
		return this.course_abbreviation;
	}
	
	/**
	 * Creates a course object with a single MeetimgTime object
	 * @param department The department of the course (e.g, ICS)
	 * @param focus The course's focus (e.g., OC, WI)
	 * @param crn The course number (e.g., 123456)
	 * @param course The course abbreviation (e.g., ICS 111)
	 * @param section The section number (e.g., 001)
	 * @param title The course title (e.g., "Introduction to Computer Science")
	 * @param instructor The instructor's name (e.g., R. Narayan)
	 * @param seats Number of seats available (e.g., 10) 
	 * @param credits The number of credits (e.g., 4)
	 * @param m The MeetingTime object 
	 */
	public Course(String department, String focus, String crn, String course,
			String section, String title, String instructor, String seats,
			String credits, MeetingTime m)  {
		this.department = department;
		this.focus = focus;
		this.crn = crn;
		this.course_abbreviation = course;
		this.section = section;
		this.title = title;
		this.instructor = instructor;
		this.seats = seats;
		this.credits = credits;
		meetingTimes = new LinkedList<MeetingTime>();
		meetingTimes.add(m);
	}

	/**
	 * Creates a Course object with the given LinkedList of MeetingTimes
	 * @param department The department of the course (e.g, ICS)
	 * @param focus The course's focus (e.g., OC, WI)
	 * @param crn The course number (e.g., 123456)
	 * @param course The course abbreviation (e.g., ICS 111)
	 * @param section The section number (e.g., 001)
	 * @param title The course title (e.g., "Introduction to Computer Science")
	 * @param instructor The instructor's name (e.g., R. Narayan)
	 * @param seats Number of seats available (e.g., 10) 
	 * @param credits The number of credits (e.g., 4)
	 * @param meetingTimes The linked list of meeting times
	 */
	public Course(String department, String focus, String crn, String course,
			String section, String title, String instructor, String seats,
			String credits, LinkedList<MeetingTime> meetingTimes) {
		this.department = department;
		this.focus = focus;
		this.crn = crn;
		this.course_abbreviation = course;
		this.section = section;
		this.title = title;
		this.instructor = instructor;
		this.seats = seats;
		this.credits = credits;
		this.meetingTimes = meetingTimes;
	}

	/**
	 * Returns the department of the course (e.g ICS)
	 * @return String of the course's department
	 */
    public String getDepartment() {
        return this.department;
    }

    /**
     * Precondition: <code>this.seats</code> is parsable to a String<br>
     * Returns the number of seats available in the course
     * @return The number of seats open
     */
    public int getSeats() {
        return Integer.parseInt(this.seats);
    }

    /**
     * Determines whether or not the course has open seats, i.e., there
     * the number of seats is greater than 0
     * @return True of the number of seats is greater than 0, false otherwise.
     */
    public boolean hasOpenSeats() {
        return this.getSeats() > 0;
    }

    /**
     * *In progress*:
     * Returns the start time of the course.<br>
     * @return Start time of the course
     */
	public int getStartTime() {
		MeetingTime m = this.meetingTimes.getFirst();
		Pattern numbers = Pattern.compile("^\\d{4}");
		Matcher match = numbers.matcher(m.getStartTime());
		while (match.find()) {
			return Integer.parseInt(match.group());	
		}
		return 0;
	}
	
	/**
	 * *In progress*:
	 * Return the days the course is offered
	 * @return Days the course is offered
	 */
	public String getDays() {
		return this.meetingTimes.getFirst().getDays();
	}
	
	/**
	 * Returns this course's focus requirement, if any.
	 * @return The course's focus requirement, if any.
	 */
	public String getFocus() {
		return this.focus;
	}
	
	/**
	 * Adds the given MeetingTime object to this course's 
	 * meetingtimes linked list. 
	 * @param m
	 */
	protected void addMeetingTime (MeetingTime m) {
		this.meetingTimes.add(m);
	}
	
	/**
	 * Returns a nicely formatted string for this course
	 * @return a nicely formatted string for this course
	 */
	public String prettyString() {
		return "Department: " + department + "\n" + 
		"Focus: " + focus + "\n" +
		crn + "\n" +
		course_abbreviation + "\n" +
		"Section: " + section + "\n" +
		title + "\n" + 
		"Credits: " + credits + "\n" +
		"Instructor: "+ instructor + "\n" +
		"Seats: "+ seats + "\n" + meetingTimesString();
	}
	
    @Override
	public String toString() {
		return
        crn + "\n" +
        course_abbreviation + ": " +
		title + "\n" +
        "Gen. Ed./Div./Focus: " + focus + "\n" +
		"Section: " + section + "\n" +
		"Credits: " + credits + "\n" +
		instructor + "\n" +
		"Seats: " + seats + "\n" + meetingTimesOutput();
	}
	
    /**
     * Returns a 'pretty' string of the meetingTimes linked list.
     * Used for debugging
     * @return 'Pretty' string containing information of the meetingTimes
     * linked list
     */
	private String meetingTimesString() {
		String s ="" ;
		for (MeetingTime m : meetingTimes) {
			s += m.prettyString() + "\n";
		}
		return s;
	}
	
	/**
	 * Returns a string of the meetingTimes linked list
	 * Used for debugging
	 * @return String containing information of the meetingTimes
	 */
	private String meetingTimesOutput() {
		String s ="" ;
		for (MeetingTime m : meetingTimes) {
			s += m.toString() + "\n";
		}
		return s;
	}
	
}
