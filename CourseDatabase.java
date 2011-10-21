package alexchantavy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.ChangedCharSetException;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * This class contains methods to download and parse the UH Class Availability database website
 * and extract its table data as Course objects.  These objects are saved in a <code>course_list</code>
 * LinkedList.  To parse the HTML, we use an HTMLEditorkit.ParserCallback class, which saves each line
 * into the linked list of Strings <code>temp_ascii_storage</code>.
 * @author Alex Chantavy
 */
public class CourseDatabase {

    //////////////////////
    //  M e m b e r s   //
    //////////////////////
    /** Contains all Course objects of the database*/
    private LinkedList<Course> course_list;
    //////////////////////////
    //  C o n s t a n t s   //
    //////////////////////////
    /** Regex pattern for 5 digit course numbers (CRNs) */
    private static final Pattern CRN = Pattern.compile("\\d{5}");
    /** Regex pattern for course declarations, e.g., ENG 100, ICS 311, EE 160, etc.*/
    private static final Pattern COURSE = Pattern.compile("^[A-Z]{2,4} \\d{3}[A-Z]?$");
    /** Regex pattern for 3 digit section numbers*/
    private static final Pattern SECTION = Pattern.compile("^\\d{2,3}$");
    /** Regex pattern for course titles.  May contain letters, hyphens, parenthesis,
     * single quotes, ampersands and colons.  */
    private static final Pattern TITLE = Pattern.compile("^[A-Za-z,-\\\\(\\\\)\\\\'\\\\&: ]+$");
    /** Regex pattern for credits.  May contain decimal points and hyphens
     * for variable amounts of credits. */
    private static final Pattern CREDITS = Pattern.compile("^[\\d-./]+$");
    /** Regex pattern for instructor's name.  May be hyphenated */
    private static final Pattern INSTRUCTOR = Pattern.compile("^[\\w-' ]+$");
    /** Regex pattern for number of seats. */
    private static final Pattern SEATS = Pattern.compile("^\\d+$");
    /** Regex pattern for days, which may contain combinations of "M, T, W, R, F, S" */
    private static final Pattern DAYS = Pattern.compile("^[MTWRFS]+$");
    /** Regex pattern for start time.  Consists of four digits followed by a hyphen.*/
    private static final Pattern START_TIME = Pattern.compile("^\\d{4}-$");
    /** Regex pattern for end time.  Consists of four digits followed by an 'a' or 'p' */
    private static final Pattern END_TIME = Pattern.compile("^\\d{4}[a|p]$");
    /** Regex pattern  for room number */
    private static final Pattern ROOM = Pattern.compile("^[A-Z\\d/\\- ]+$");
    /** Regex pattern for dates */
    private static final Pattern DATES = Pattern.compile("^\\d{2}/\\d{2}-\\d{2}/\\d{2}$");
    /** Regex pattern for department name */
    private static final Pattern DEPARTMENT = Pattern.compile("[A-Z]{2,4}");
    /* root URL of the class availability website */
    public static final String CLASS_DB_URL = "http://www.sis.hawaii.edu/uhdad/avail.classes?i=MAN&t=201210&s=";
    /** Array containing four letter abbreviations of all departments at UHM as of Fall 2010 */
    public static final String[] DEPARTMENT_LIST = {"ACC", "ACM", "AMST", "ANAT",
        "ANSC", "ANTH", "APDM", "ARAB", "ARCH", "ART", "AS", "ASAN", "ASTR",
        "BE", "BIOC", "BIOL", "BIOM", "BLAW", "BOT", "BUS", "CAAM", "CAM",
        "CAS", "CEE", "CHAM", "CHEM", "CHN", "CIS", "CMB", "COM", "CSD",
        "CUL", "DH", "DIS", "DNCE", "DRB", "EALL", "ECON", "EDCS", "EDEA",
        "EDEF", "EDEP", "EE", "ELI", "ENG", "ENGR", "ES", "ETEC", "FAMR",
        "FIL", "FIN", "FMCH", "FR", "FSHN", "GEOG", "GER", "GERI", "GG",
        "GRK", "HAW", "HIST", "HNDI", "HON", "HRM", "HWST", "ICS", "ILO",
        "IND", "INS", "IP", "IS", "ITAL", "ITE", "ITM", "JOUR", "JPN", "KOR",
        "KRS", "LAIS", "LATN", "LAW", "LING", "LIS", "LLEA", "LLL", "LLM",
        "LWEV", "LWJT", "LWLW", "LWPA", "LWUL", "MAO", "MATH", "MBBE", "MDED",
        "ME", "MED", "MEDT", "MET", "MGT", "MICR", "MKT", "MSL", "MUS", "NHH",
        "NREM", "NURS", "OBGN", "OCN", "OEST", "ORE", "PACE", "PACS", "PATH",
        "PED", "PEPS", "PH", "PHIL", "PHRM", "PHYL", "PHYS", "PLAN", "POLS",
        "PORT", "PPC", "PPST", "PSTY", "PSY", "PUBA", "RE", "REL", "REPR",
        "RUS", "SAM", "SLS", "SNSK", "SOC", "SOCS", "SP", "SPAN", "SPED",
        "SURG", "SW", "TAHT", "THAI", "THEA", "TI", "TIM", "TONG", "TPSS",
        "TRMD", "VIET", "WS", "ZOOL"};
    /** temporary storage of the last text file parsed. **/
    private static LinkedList<String> temp_ascii_storage = new LinkedList<String>();

    ////////////////////////////////
    //  C o n s t r u c t o r s   //
    ////////////////////////////////
    public CourseDatabase() {
        this.course_list = new LinkedList<Course>();
    }

    /**
     * Creates a CourseDatabase from the given LinkedList of courses
     * @param course_list LinkedList of courses
     */
    public CourseDatabase(LinkedList<Course> course_list) {
        super();
        this.course_list = course_list;
    }

    /**
     * Creates a CourseDatabase from a department name
     * @param dept
     * @throws NoSuchDepartmentException
     * @throws InvalidFileFormatException
     */
    public CourseDatabase(String dept) throws NoSuchDepartmentException, InvalidFileFormatException, IOException {
        boolean isValidDepartment = false;
        for (String s : DEPARTMENT_LIST) {
            if (s.equals(dept)) {
                isValidDepartment = true;
            }
        }
        if (isValidDepartment) {
            this.course_list = new LinkedList<Course>();
            this.course_list = downloadAndSaveDepartment(dept);
        } else {
            throw new NoSuchDepartmentException(dept);
        }
    }

    //////////////////////////////////////
    // I n s t a n c e  M e t h o d s   //
    //////////////////////////////////////
    /**
     * Returns how many <code>Course</code>s the database contains
     * @return The size of the database
     */
    public int size() {
        return this.course_list.size();
    }

    /**
     * Returns the list of courses that the database contains
     * @return the list of courses contained by this database
     */
    public LinkedList<Course> getCourseList() {
        return this.course_list;
    }

    /**
     * Prints out all the Courses of a database to the console
     */
    public void printDatabase() {
        for (Course c : this.course_list) {
            System.out.println(c);
        }
    }

    /**
     * Returns a nicely formatted String of the given LinkedList of courses
     * @param courseList The list of courses to be concatenated to a string
     * @return A nicely formatted string of courses
     */
    public static String courseListToString(LinkedList<Course> courseList) {
        String s = "";
        for (Course c : courseList) {
            s += c.toString() + "\n\n";
        }
        return s;
    }

    @Override
    public String toString() {
        String s = "";
        for (Course c : this.course_list) {
            s += c.toString();
        }
        return s;
    }

    /**
     * Appends the given LinkedList of courses to this database's <code>course_list</code>
     * @param toBeAppended
     * @return true if successful, false if unsuccessful
     */
    public boolean append(LinkedList<Course> toBeAppended) {
        try {
            this.course_list.addAll(toBeAppended);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //////////////////////////////////
    // S t a t i c  M e t h o d s   //
    //////////////////////////////////
    /**
     * Connects to the webpage referenced by the given URL and uses a ParserDelgator
     * to extract the text from the page.  This text is then saved in memory as
     * temp_ascii_access
     * @param url The URL of the HTML file
     */
    protected static void downloadTextFromURL(String url) throws IOException {
        try {
            URL target = new URL(url);
            URLConnection connection = target.openConnection();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"));
            boolean ignoreCharset = true; //to prevent ChangedCharSetExceptions
            new ParserDelegator().parse(reader, new CallbackHandler(), ignoreCharset);
            reader.close();
        } catch (ChangedCharSetException e) {
            System.out.println(e.getCharSetSpec());
        } catch (IOException e) {
            throw new IOException();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Class used to parse HTML in order to extract text from a webpage.
     */
    private static class CallbackHandler extends HTMLEditorKit.ParserCallback {

        /** Determines whether to append to temp.txt.  Initally <code>false</code> to overwrite any possible old data.
         * Becomes <code>true</code> in parsing to append lines. */
        // No longer needed: private static boolean appendStatus = false; //initially false to overwrite any possible old data
        public CallbackHandler() {
        }
        Pattern allowedCharacters = Pattern.compile("^[A-Za-z0-9/:,-\\\\'&=() ]+$");

        /**
         * Takes text from a BufferedReader and appends it to the text file temp.txt.
         */
        @Override
        public void handleText(char[] data, int pos) {
            String line = String.valueOf(data); // convert char[] to String
            if (allowedCharacters.matcher(line).matches()) {
                temp_ascii_storage.add(line);
            }
        }
    }

    /**
     * The heart of parsing UH Class Availability data.
     * <p>Given a text file (that was created as a result of <code>downloadTextFromURL(String)</code>),
     * the method parses each line and saves the data as a LinkedList of Course objects.
     * <p>Matching of <code>Course</code> data fields is done with regular expressions to verify
     * correct input.  If abnormalities in the file are found, the method throws an InvalidFileFormatException
     * with line numbers.  There are many hardcoded segments to check for variations in table data, e.g., 'TBA'
     * present being present instead of room number, or multiple meeting times and room numbers.
     * <p>At the time of this implementation, this string processing method works on the 2011 Spring UH Manoa
     * Class Availability list.
     * @param filename The filename of the text file to read from
     * @return A LinkedList of Course objects from the file
     * @throws InvalidFileFormatException When the given text file is malformed
     */
    public static LinkedList<Course> parseCourses() throws InvalidFileFormatException {
        LinkedList<Course> courses = new LinkedList<Course>();
        String focus, courseNum, section, department, course, title, instructor, days,
                startTime, endTime, room, dates, credits, seatsAvail;
        //Store the lines of the text file in a LinkedList of Strings

        //old way: LinkedList<String> allLines = createLinkedListFromFile(filename);
        for (int i = 0; i < temp_ascii_storage.size(); i++) {
            String currentLine = temp_ascii_storage.get(i);
            String prevLine = i == 0 ? temp_ascii_storage.get(0) : temp_ascii_storage.get(i - 1);
            if (CRN.matcher(currentLine).matches()) {
                courseNum = currentLine;
                if (prevLine.contains("FGA") || prevLine.contains("DA")
                        || prevLine.contains("DP") || prevLine.contains("FGB") || prevLine.contains("FGC")
                        || prevLine.contains("DH") || prevLine.contains("DY") || prevLine.contains("FS")
                        || prevLine.contains("DL") || prevLine.contains("HSL") || prevLine.contains("FW")
                        || prevLine.contains("NI") || prevLine.contains("ETH") || prevLine.contains("HAP")
                        || prevLine.contains("OC") || prevLine.contains("WI")) {
                    focus = prevLine;
                } else {
                    focus = "none";
                }

                prevLine = currentLine;
                i++;
                currentLine = temp_ascii_storage.get(i);
                if (COURSE.matcher(currentLine).matches()) {
                    course = currentLine;
                    Matcher match = DEPARTMENT.matcher(currentLine);
                    if (match.find()) {
                        department = match.group();
                    } else {
                        department = "";
                    }
                } else {
                    throw new InvalidFileFormatException("Course", prevLine, currentLine, i + 1);
                }

                prevLine = currentLine;
                i++;
                currentLine = temp_ascii_storage.get(i);
                if (SECTION.matcher(currentLine).matches()) {
                    section = currentLine;
                } else {
                    throw new InvalidFileFormatException("Section", prevLine, currentLine, i + 1);
                }

                prevLine = currentLine;
                i++;
                currentLine = temp_ascii_storage.get(i);
                if (TITLE.matcher(currentLine).matches()) {
                    title = currentLine;
                } else {
                    throw new InvalidFileFormatException("Title", prevLine, currentLine, i + 1);
                }

                prevLine = currentLine;
                i++;
                currentLine = temp_ascii_storage.get(i);
                if (CREDITS.matcher(currentLine).matches()) {
                    credits = currentLine;
                } else if (CREDITS.matcher(temp_ascii_storage.get(i + 1)).matches()) {
                    i++;
                    prevLine = currentLine;
                    currentLine = temp_ascii_storage.get(i);
                    credits = currentLine;
                } else if ((CREDITS.matcher(temp_ascii_storage.get(i + 2)).matches())) {
                    i += 2;
                    prevLine = currentLine;
                    currentLine = temp_ascii_storage.get(i);
                    credits = currentLine;
                } else {
                    throw new InvalidFileFormatException("Credits", prevLine, currentLine, i + 1);
                }

                prevLine = currentLine;
                i++;
                currentLine = temp_ascii_storage.get(i);
                if (INSTRUCTOR.matcher(currentLine).matches()
                        || currentLine.equals("TBA")) {
                    instructor = currentLine;
                } else {
                    throw new InvalidFileFormatException("Instructor", prevLine, currentLine, i + 1);
                }

                prevLine = currentLine;
                i++;
                currentLine = temp_ascii_storage.get(i);
                if (SEATS.matcher(currentLine).matches()) {
                    seatsAvail = currentLine;
                } else {
                    throw new InvalidFileFormatException("Seats", prevLine, currentLine, i + 1);
                }
                Course newCourse = new Course(department, focus, courseNum, course, section,
                        title, instructor, seatsAvail, credits);
                
                currentLine = temp_ascii_storage.get(i);
                do {
                    prevLine = currentLine;
                    i++;
                    //System.out.println("Prev line " + prevLine);
                    currentLine = temp_ascii_storage.get(i);
                    //System.out.println("Current")
                    prevLine = currentLine;
                    i++;
                    if (DAYS.matcher(currentLine).matches()
                            || currentLine.equals("TBA")) {
                        days = currentLine;
                    } else {
                        throw new InvalidFileFormatException("Days", prevLine, currentLine, i + 1);
                    }

                    prevLine = currentLine;
                    i++;
                    currentLine = temp_ascii_storage.get(i);
                    if (START_TIME.matcher(currentLine).matches()
                            || currentLine.equals("TBA")) {
                        startTime = currentLine;
                    } else {
                        throw new InvalidFileFormatException("Start time", prevLine, currentLine, i + 1);
                    }

                    prevLine = currentLine;
                    i++;
                    currentLine = temp_ascii_storage.get(i);
                    if (END_TIME.matcher(currentLine).matches()) {
                        endTime = currentLine;
                        prevLine = currentLine;
                        i++;
                        currentLine = temp_ascii_storage.get(i);
                    } else if (startTime.equals("TBA")) {
                        endTime = "TBA"; //this accounts for endTime being blank because start time was TBA.
                    } else {
                        throw new InvalidFileFormatException("End time", prevLine, currentLine, i + 1);
                    }

                    if (ROOM.matcher(currentLine).matches()
                            || currentLine.contains("TBA")) {
                        room = currentLine;
                    } else {
                        throw new InvalidFileFormatException("Room", prevLine, currentLine, i + 1);
                    }

                    prevLine = currentLine;
                    i++;
                    currentLine = temp_ascii_storage.get(i);
                    if (DATES.matcher(currentLine).matches()
                            || currentLine.contains("TBA")) {
                        dates = currentLine;
                    } else {
                        throw new InvalidFileFormatException("Dates", prevLine, currentLine, i + 1);
                    }
                    MeetingTime m = new MeetingTime(days, startTime, endTime, room, dates);
                    newCourse.addMeetingTime(m);
                } while (DAYS.matcher(temp_ascii_storage.get(i + 1)).matches()
                        && !temp_ascii_storage.get(i + 1).equals("FW")
                        && (!temp_ascii_storage.get(i + 1).equals("FS")
                        // account for special case where "FW" might be the next line
                        && START_TIME.matcher(temp_ascii_storage.get(i + 2)).matches()));
                // System.out.println(newCourse.toString() + "\n");
                courses.add(newCourse);
            }
        }
        temp_ascii_storage.clear();
        return courses;
    }

    /**
     * Takes the given 2-4 letter department abbreviation (e.g., MATH, ENG, EE, etc) and downloads
     * course data of that department, saving it to memory.
     * <p>Note that the save process overwrites existing files with the same name.
     * @param dept The 2-4 letter abbreviation
     * @return The name of the file that course data was saved to.
     * @throws NoSuchDepartmentException when the given department is not a real department
     * @throws InvalidFileFormatException when parsing a URL fails in <code>parseCoursesFromFile(String)</code>
     */
    public static LinkedList<Course> downloadAndSaveDepartment(String dept) throws InvalidFileFormatException, NoSuchDepartmentException, IOException {
        boolean isValidDepartment = false;
        for (String s : DEPARTMENT_LIST) {
            if (s.equals(dept)) {
                isValidDepartment = true;
            }
        }
        if (isValidDepartment) {
            //fall 2010: http://www.sis.hawaii.edu/uhdad/avail.classes?i=MAN&t=201110&s=
            downloadTextFromURL(CLASS_DB_URL + dept);
            return parseCourses();
        } else {
            throw new NoSuchDepartmentException(dept);
        }
    }

    /**

    /**
     * <p>Takes all department abbreviations (e.g., MATH for mathematics, ENG for english, EE for electrical
     * engineering, etc) and connects to each department's Class Availability URL.
     * <p>Then, uses <code>downloadTextFromURL()</code> to parse course data, appending each course to a LinkedList
     * of <code>Course</code>s.
     * <p>Speed of execution is dependent on network speed.
     * @return A LinkedList of Courses for all the departments specified
     * @throws InvalidFileFormatException If parsing fails
     */
    public static LinkedList<Course> loadAllDepartments() throws InvalidFileFormatException, IOException {
        LinkedList<Course> catalog = new LinkedList<Course>();
        for (String dept : DEPARTMENT_LIST) {
            downloadTextFromURL(CLASS_DB_URL + dept);
            catalog.addAll(parseCourses());
        }
        return catalog;
    }
}
