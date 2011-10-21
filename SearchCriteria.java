/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package alexchantavy;

/**
 * <p>This class represents the abstract idea of a Search Criterion.
 * One of this application's goals is to allow for a user to specify a set of
 * conditions for a desired course and have the program return a list of
 * courses that fit all conditions.</p>
 *
 * <p>In this case, a criteria consists of a
 * <ul>
 *    <li>Department</li>
 *    <li>Gen. Ed. Requirement</li>
 *    <li>Div. Requirement</li>
 *    <li>Focus Requirement</li>
 * </ul>
 * </p>
 *
 * <p>As an example, a user may search for a class with "Any" department,
 * fulfilling "Any" Gen. Ed Requirement, "Any" Div. Requirement, and with "OC"
 * Focus in order to get a list of all classes with OC requirements.
 *
 * @author Alex Chantavy
 */
public class SearchCriteria {
    private String department, genEdReq, divReq, focusReq;
    private boolean onlineOnly;

    public SearchCriteria (String dept, String genEd, String div, String focus, boolean online) {
        this.department = dept;
        this.genEdReq = genEd;
        this.divReq = div;
        this.focusReq = focus;
        this.onlineOnly = online;
    }

    /**
     * Returns the department of this criterion.
     * @return The department
     */
    public String getDepartment() {
        return this.department;
    }

    /**
     * Returns the general education requirement sought in the criterion
     * @return The gen. ed. requirement
     */
    public String getGenEd() {
        return this.genEdReq;
    }

    /**
     * Returns the diversification requirement of the criterion
     * @return The diverification requirement
     */
    public String getDiv() {
        return this.divReq;
    }

    /**
     * Returns the focus requirement of the criterion
     * @return The focus requirement
     */
    public String getFocus() {
        return this.focusReq;
    }

    /**
     * Returns whether the criterion requires the course to have open seats
     * @return Whether it is required for the course to have open seats
     */
    public boolean onlineCoursesRequired() {
        return this.onlineOnly;
    }

    @Override
    public String toString () {
        return "Dept: " + this.department + ", Gen Ed: " + this.genEdReq +
                ", Div: " + this.divReq + ", Focus: " + this.focusReq +
                ", Open Seats Required: " + this.onlineOnly;
    }
}
