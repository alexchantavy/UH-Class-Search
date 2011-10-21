/*
 * CourseApplet.java
 *
 * Created on Jul 2, 2010, 1:43:59 AM
 */



package alexchantavy;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * This Applet is the frontend GUI for the Course Finder application.
 * Downloading of necessary data is performed in a background SwingWorker thread.
 * @author Alex Chantavy
 */
@SuppressWarnings("serial")
public class CourseApplet extends javax.swing.JApplet {
    LinkedList <Course> loaded_courses;
    LoadEntireCatalogTask load_task;
    SearchCriteria criteria;

    /** Initializes the applet CourseApplet */
    @Override
    public void init() {
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    initComponents();
                    initComboBoxes();
                }

                private void initComboBoxes() {
                    /* The following code segment is a hack to get around
                    constraints of NetBeans IDE.*/
                    // Initialize the contents of the Department JComboBox
                    String[] deptListContents = new String[CourseDatabase.DEPARTMENT_LIST.length + 1];
                    deptListContents[0] = "Any";
                    System.arraycopy(CourseDatabase.DEPARTMENT_LIST, 0, deptListContents, 1, CourseDatabase.DEPARTMENT_LIST.length);
                    cmb_deptList.setModel(new javax.swing.DefaultComboBoxModel(deptListContents));
                    // Initialize contents of Focus JComboBox
                    String[] focusListContents = {"Any", "ETH", "HAP", "OC", "WI"};
                    cmb_focusList.setModel(new javax.swing.DefaultComboBoxModel(focusListContents));
                    // Initialize contents of Gen Ed JComboBox
                    String[] genEdListContents = {"Any", "FGA", "FGB", "FGC", "FS", "FW", "HSL", "NI"};
                    cmb_genEdList.setModel(new javax.swing.DefaultComboBoxModel(genEdListContents));
                    // Initialize contents of Diversification requirements
                    String[] divListContents = {"Any", "DA", "DB", "DH", "DL", "DP", "DS", "DY"};
                    cmb_divReqList.setModel(new javax.swing.DefaultComboBoxModel(divListContents));
                }

            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * This is a nested anonymous SwingWorker class that will download the 
     * necessary HTML web page data in the background.  
     * @author Alex Chantavy
     */
    private class LoadEntireCatalogTask extends SwingWorker<Void, String> {
        boolean successful = false;
        @SuppressWarnings("finally")
	@Override
        public Void doInBackground() {
            try {
                indeterminateProgressBar.setIndeterminate(true);
                //Clear out the console window
                if (!console.getText().equals("")) {
                    console.setText("");
                }
                loaded_courses = new LinkedList<Course>();
                LinkedList<String> departmentsToLoad = new LinkedList<String>();
                // Figure out whether to open all departments or just one
                if (criteria.getDepartment().equals("Any")) {
                    departmentsToLoad.addAll(Arrays.asList(CourseDatabase.DEPARTMENT_LIST));
                }
                else {
                    departmentsToLoad.add(criteria.getDepartment());
                }

                // Load up all the classes of selected department(s)
                for (String dept : departmentsToLoad) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    publish(dept); //Report which Department is currently downloading
                    CourseDatabase.downloadTextFromURL(
                                        CourseDatabase.CLASS_DB_URL + dept);
                                        loaded_courses.addAll(CourseDatabase.parseCourses());
                    
                }

                System.out.println("Before remove, there are"+ loaded_courses.size() +" courses");
                String reqDiv, reqGened, reqFocus;
                boolean reqOnline = false;
                reqDiv = criteria.getDiv();
                reqGened = criteria.getGenEd();
                reqFocus = criteria.getFocus();
                reqOnline = criteria.onlineCoursesRequired();
                
                // If the user has no preference for gen end reqs, div reqs, focus
                // reqs, or availability, there is no need for us to filter the results.
                // So, do nothing.
                System.out.println("Requested GenEd: " + reqGened);
                System.out.println("Requested Div: " + reqDiv);
                System.out.println("Requested Focus: " + reqFocus);
                if (reqGened.equals("Any") &&
                        reqDiv.equals("Any") &&
                        reqFocus.equals("Any") &&
                        !reqOnline) {
                    // do nothing, we don't need to remove anything from the list
                }
                else {
                    // Check the list of loaded courses, removing courses that do
                    // not satisfy the user-specified criteria. Iterator is used because
                    // it is thread-safe.
                    Iterator<Course> it = loaded_courses.iterator();
                    while (it.hasNext()) {
                        Course c = it.next();
                        String focusString = c.getFocus();
                        // First check if the course fulfills requirements of the user.
                        // If so, no need to remove the course from the list.

                        System.out.println("Focus string for :" + c.getCourseAbbrev() +" is " + focusString);
                        if  ((!reqDiv.equals("Any")   && !focusString.contains(reqDiv))   || 
                             (!reqFocus.equals("Any") && !focusString.contains(reqFocus)) ||
                             (!reqGened.equals("Any") && !focusString.contains(reqGened)) ||
                             (reqOnline               && !Course.isOnline(c)))             {
                        	it.remove();
                        }
                    }
                }
                successful = true;
            }
            catch (InterruptedException e) {
                loaded_courses = null;
                indeterminateProgressBar.setIndeterminate(false);
                console.setText("Load cancelled.");
            }
            catch (InvalidFileFormatException e) {
                e.printStackTrace();
                console.setText(e.toString());
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(null, "IOException\nPlease check " +
                        "your internet connection.  Otherwise, maybe the UH server" +
                        "is down, so you should try again later.");
            }
            catch (Exception e) {
                loaded_courses = null;
                e.printStackTrace();
            }
            finally {
                btn_search.setText("Search");
                lbl_processing.setEnabled(false);
                fld_progressText.setText("   ");
                fld_progressText.setEnabled(false);
                fld_progressText.setText("   ");
                indeterminateProgressBar.setIndeterminate(false);
                return null;
            }
        }

        @Override
        public void done() {
            if (successful) {
                if (loaded_courses.size()==0) {
                    console.setText("No courses match your criteria.  Please try again.");
                }
                else {
                    console.setText(console.getText() + "\n" + 
                            CourseDatabase.courseListToString(loaded_courses));
                }
            }
            btn_search.setEnabled(true);
            lbl_processing.setEnabled(false);
            fld_progressText.setEnabled(false);
        }

        @Override
        public void process (List<String> data) {
            String s = data.get(data.size()-1);
            fld_progressText.setText(s);
        }
    }

    /** This method is called from within the init() method to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lbl_title = new javax.swing.JLabel();
        lbl_chooseCriteria = new javax.swing.JLabel();
        lbl_dept = new javax.swing.JLabel();
        lbl_focus = new javax.swing.JLabel();
        lbl_genEd = new javax.swing.JLabel();
        lbl_divReq = new javax.swing.JLabel();
        chkbx_onlineCourses = new javax.swing.JCheckBox();
        cmb_deptList = new javax.swing.JComboBox();
        cmb_focusList = new javax.swing.JComboBox();
        cmb_genEdList = new javax.swing.JComboBox();
        cmb_divReqList = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        console = new javax.swing.JEditorPane();
        lbl_viewResults = new javax.swing.JLabel();
        btn_search = new javax.swing.JButton();
        fld_progressText = new javax.swing.JTextField();
        lbl_processing = new javax.swing.JLabel();
        indeterminateProgressBar = new javax.swing.JProgressBar();

        setStub(null);

        lbl_title.setText("UH Manoa CourseFinder: 2011 Spring");

        lbl_chooseCriteria.setText("Step 1: Choose your search criteria.");

        lbl_dept.setText("Department:");

        lbl_focus.setText("Focus Reqs:");

        lbl_genEd.setText("Gen. Ed Reqs:");

        lbl_divReq.setText("Div. Reqs:");

        chkbx_onlineCourses.setText("Show only online courses");
        chkbx_onlineCourses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkbx_onlineCoursesActionPerformed(evt);
            }
        });

        cmb_deptList.setMaximumRowCount(100);
        cmb_deptList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmb_focusList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmb_genEdList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmb_divReqList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jScrollPane1.setViewportView(console);

        lbl_viewResults.setText("Step 2: View Results");

        btn_search.setText("Search");
        btn_search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_searchActionPerformed(evt);
            }
        });

        fld_progressText.setEditable(false);
        fld_progressText.setText("       ");
        fld_progressText.setEnabled(false);

        lbl_processing.setText("Currently Processing:");
        lbl_processing.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(68, 68, 68)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(indeterminateProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                .add(lbl_dept)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 83, Short.MAX_VALUE)
                                                .add(cmb_deptList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(chkbx_onlineCourses)
                                            .add(btn_search)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                .add(lbl_focus)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 86, Short.MAX_VALUE)
                                                .add(cmb_focusList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(layout.createSequentialGroup()
                                                .add(lbl_divReq)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 98, Short.MAX_VALUE)
                                                .add(cmb_divReqList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(layout.createSequentialGroup()
                                                .add(lbl_genEd)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 76, Short.MAX_VALUE)
                                                .add(cmb_genEdList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                                    .add(layout.createSequentialGroup()
                                        .add(43, 43, 43)
                                        .add(lbl_chooseCriteria))
                                    .add(layout.createSequentialGroup()
                                        .add(30, 30, 30)
                                        .add(lbl_processing)))
                                .add(39, 39, 39))
                            .add(layout.createSequentialGroup()
                                .add(62, 62, 62)
                                .add(fld_progressText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lbl_viewResults)
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 362, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(273, 273, 273)
                        .add(lbl_title)))
                .addContainerGap(66, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(indeterminateProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(28, 28, 28)
                            .add(lbl_title)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                    .add(lbl_chooseCriteria)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                            .add(18, 18, 18)
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                .add(lbl_dept)
                                                .add(cmb_deptList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                .add(lbl_focus)
                                                .add(cmb_focusList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                .add(lbl_divReq)
                                                .add(cmb_divReqList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                                .add(cmb_genEdList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(lbl_genEd))
                                            .add(6, 6, 6)
                                            .add(chkbx_onlineCourses)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                            .add(btn_search))
                                        .add(layout.createSequentialGroup()
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE))))
                                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .add(52, 52, 52)
                            .add(lbl_viewResults)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 302, Short.MAX_VALUE)
                            .add(lbl_processing)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(fld_progressText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    /**
     * Handles what happens when the Search/Cancel Button is pressed
     * @param evt
     */
    private void btn_searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_searchActionPerformed
        if (evt.getActionCommand().equals("Search")) {
            criteria = new SearchCriteria((String)cmb_deptList.getSelectedItem(),
                                          (String)cmb_genEdList.getSelectedItem(),
                                          (String)cmb_divReqList.getSelectedItem(),
                                          (String)cmb_focusList.getSelectedItem(),
                                          chkbx_onlineCourses.isSelected());      
            btn_search.setText("Cancel");
            lbl_processing.setEnabled(true);
            fld_progressText.setEnabled(true);
            load_task = new LoadEntireCatalogTask();
            load_task.execute();
        }
        else if (evt.getActionCommand().equals("Cancel")) {
            load_task.cancel(true);
            load_task = null;
            btn_search.setText("Search");
        }
    }//GEN-LAST:event_btn_searchActionPerformed

    private void chkbx_onlineCoursesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkbx_onlineCoursesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkbx_onlineCoursesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_search;
    private javax.swing.JCheckBox chkbx_onlineCourses;
    private javax.swing.JComboBox cmb_deptList;
    private javax.swing.JComboBox cmb_divReqList;
    private javax.swing.JComboBox cmb_focusList;
    private javax.swing.JComboBox cmb_genEdList;
    private javax.swing.JEditorPane console;
    private javax.swing.JTextField fld_progressText;
    private javax.swing.JProgressBar indeterminateProgressBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lbl_chooseCriteria;
    private javax.swing.JLabel lbl_dept;
    private javax.swing.JLabel lbl_divReq;
    private javax.swing.JLabel lbl_focus;
    private javax.swing.JLabel lbl_genEd;
    private javax.swing.JLabel lbl_processing;
    private javax.swing.JLabel lbl_title;
    private javax.swing.JLabel lbl_viewResults;
    // End of variables declaration//GEN-END:variables

}
