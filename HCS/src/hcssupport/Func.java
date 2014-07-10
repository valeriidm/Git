/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hcssupport;

import hcsmain.DB;
import hcsmain.MedOfficer;
import hcsmain.Staff;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @author ContEd Student
 */
public class Func<T> {

    public void fillStaff(Vector<T> staff, int id) {
        ResultSet rs = DB.db.staff();
        try {
            while (rs.next()) {
                Staff s = new Staff();
                s.setLname(rs.getString("lname"));
                s.setFname(rs.getString("fname"));
                s.setbDate(rs.getDate("bDate"));
                if (rs.getInt("posid") == id)
                    staff.add((T) s);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MedOfficer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setOpacity(Object comp) {
        if (comp instanceof JScrollPane) {
            JScrollPane cp = (JScrollPane) comp;
            cp.setOpaque(false);
            cp.getViewport().setOpaque(false);
            cp.getHorizontalScrollBar().setOpaque(false);
            cp.getVerticalScrollBar().setOpaque(false);
        } else if (comp instanceof JTextPane) {
            JTextPane cp = (JTextPane) comp;
            cp.setContentType("text/html");
            cp.setOpaque(false);
            cp.setEditable(false);
        }
    }

    public void staffPersonalInfo(Vector<Staff> staff, String toMatch, JTextField... staffTF) {
        try {
            int ind = -1;
            String lName = toMatch.substring(0, toMatch.indexOf(","));
            String dBirth = toMatch.substring(toMatch.indexOf("(") + 1, toMatch.indexOf(")"));
            for (int i = 0; i < staff.size(); i++)
                if (staff.get(i).getLname().equalsIgnoreCase(lName) && staff.get(i).getbDate().toString().equalsIgnoreCase(dBirth))
                    ind = i;
            ResultSet res = DB.db.staff(staff.get(ind).getLname(), staff.get(ind).getbDate());
            while (res.next()) {
                staffTF[0].setText(res.getString("fname"));
                staffTF[1].setText(res.getString("lname"));
                staffTF[2].setText(res.getDate("bdate").toString());
                staffTF[3].setText(res.getString("email"));
                staffTF[4].setText(res.getString("address"));
                staffTF[5].setText(res.getString("zip"));
                staffTF[6].setText(res.getString("phone"));
                String tmp = res.getString("SSN");
                staffTF[7].setText(tmp.substring(0, 3) + "-" + tmp.substring(3, 6) + "-" + tmp.substring(6, 9));
                ResultSet rs = DB.db.position(res.getInt("posid"));
                while (rs.next()) {
                    staffTF[8].setText(rs.getString("posdesc"));
                }
                rs = DB.db.qualification(res.getInt("qualid"));
                while (rs.next()) {
                    staffTF[9].setText(rs.getString("qualdesc"));
                }
            }
            DB.db.close();

        } catch (SQLException ex) {
            Logger.getLogger(MedOfficer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*Added 04.07*/
    public void staffListHosp(Vector<Staff> staff, String toMatch, DefaultListModel list) {
        try {
            list.removeAllElements();
            Vector<StringBuffer> t = new Vector();
            int ind = -1;
            String lName = toMatch.substring(0, toMatch.indexOf(","));
            String dBirth = toMatch.substring(toMatch.indexOf("(") + 1, toMatch.indexOf(")"));
            for (int i = 0; i < staff.size(); i++)
                if (staff.get(i).getLname().equalsIgnoreCase(lName) && staff.get(i).getbDate().toString().equalsIgnoreCase(dBirth))
                    ind = i;
            ResultSet rs = DB.db.staff(staff.get(ind).getLname(), staff.get(ind).getbDate());
            while (rs.next()) {
                ResultSet rh = DB.db.staffSchedule(rs.getInt("id"));
                while(rh.next()){
                    ResultSet rsh = DB.db.hospital(rh.getInt("hospid"));
                    while(rsh.next()){
                        StringBuffer tmp = new StringBuffer();
                        tmp.append(rsh.getString("name"));
                        t.add(tmp);
                    }
                }
            }
            // building list
            for (int i = 0; i < t.size(); i++)
                list.add(i, t.get(i).toString());
        } catch (SQLException ex) {
            Logger.getLogger(Func.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void staffListInfo(Vector<Staff> staff, Object comp, JTextField tf, DefaultListModel list) {
        JTextPane cp = (JTextPane) comp;
        list.removeAllElements();
        StringBuffer st = new StringBuffer();
        Vector<StringBuffer> t = new Vector();
        st.append("<table width='250'>");
        for (int i = 0; i < staff.size(); i++)
            if (staff.get(i).getLname().toLowerCase().startsWith(
                    tf.getText().toLowerCase())) {
                // preparing information for staff info
                st.append("<tr><td><b>");
                st.append(staff.get(i).getLname());
                st.append(", " + staff.get(i).getFname());
                st.append("</b></td>");
                st.append("<td><b>");
                st.append(staff.get(i).getbDate().toString());
                st.append("</b></td>");
                st.append("</tr>");
                // preparing information for list
                StringBuffer tmp = new StringBuffer();
                tmp.append(staff.get(i).getLname() + ", ");
                tmp.append(staff.get(i).getFname().charAt(0) + ". (");
                tmp.append(staff.get(i).getbDate().toString() + ")");
                t.add(tmp);
            }
        //finishing and sending information to staff info
        st.append("</table>");
        cp.setText(st.toString());
        // building list
        for (int i = 0; i < t.size(); i++)
            list.add(i, t.get(i).toString());

    }

    public String fillStaffInfo(String staffPos) {

        ResultSet rs = DB.db.staff();
        StringBuffer info = new StringBuffer();
        try {
            info.append("<table>");
            while (rs.next()) {
                // check through the positions
                ResultSet res = DB.db.position(rs.getInt("posid"));
                // fill panes according to the position
                while (res.next()) {
                    if (res.getString("posdesc").equalsIgnoreCase(staffPos)) {
                        if (staffPos.equalsIgnoreCase("gp"))
                            info.append("<tr><td>Dr. <b>");
                        else if (staffPos.equalsIgnoreCase("ns"))
                            info.append("<tr><td>R.N. <b>");
                        else if (staffPos.equalsIgnoreCase("ma"))
                            info.append("<tr><td>Med.As. <b>");
                        info.append(rs.getString("lname"));
                        info.append(", " + rs.getString("fname"));
                        info.append("</b></td></tr>");
                        info.append("<tr><td>Phone: <b>");
                        info.append(rs.getString("phone") + "</b></td></tr>");
                        // find the qualification
                        ResultSet ress = DB.db.qualification(rs.getInt("qualid"));
                        while (ress.next()) {
                            info.append("<tr><td>Qualification: <b>");
                            info.append(ress.getString("qualdesc") + "</b></td></tr>");
                        }
                        //find the hospital
                        ResultSet rss = DB.db.staffSchedule(rs.getInt("id"));
                        while (rss.next()) {
                            ress = DB.db.hospital(rss.getInt("hospid"));
                            while (ress.next()) {
                                info.append("<tr><td>Hospital: <b>");
                                info.append(ress.getString("name") + "</b></td></tr>");
                            }
                        }
                        info.append("<tr></tr>");
                    }
                }
            }
            info.append("</table>");
            DB.db.close();
        } catch (SQLException ex) {
            Logger.getLogger(MedOfficer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return info.toString();
    }

    /* Added 08.07.2014*/
    public void checkSchedule(Vector<Staff> staff, String toMatch, JTextPane pane, JList item){
        int ind=-1;
        int hospId = -1;
        int staffId = -1;
        StringBuffer st = new StringBuffer();
        try {
            String lName = toMatch.substring(0, toMatch.indexOf(","));
            String dBirth = toMatch.substring(toMatch.indexOf("(") + 1, toMatch.indexOf(")"));
            for (int i = 0; i < staff.size(); i++)
                if (staff.get(i).getLname().equalsIgnoreCase(lName) && staff.get(i).getbDate().toString().equalsIgnoreCase(dBirth))
                    ind = i;
            ResultSet res = DB.db.hospital(item.getSelectedValue().toString());
            while (res.next()) {
                hospId = res.getInt("id");
            }
            res = DB.db.staff(staff.get(ind).getLname(), staff.get(ind).getbDate());
            while (res.next()) {
                staffId = res.getInt("id");
            }
            res = DB.db.staffSchedule(staffId, hospId);
            while (res.next()){
                st.append(res.getString("workday"));
                st.append("<br />");
                st.append(res.getString("workhour"));
                st.append("<br />");
            }
            pane.setText(st.toString());
            DB.db.close();
        } catch (SQLException ex) {
            Logger.getLogger(Func.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // make it later
    public Vector<T> findChoosenStaff(String staffPos, String name) {

        Vector<T> st = new Vector();
        // ,\make it through calling the db
        int pos;
        if (staffPos.equalsIgnoreCase("gp"))
            pos = 1;
        else if (staffPos.equalsIgnoreCase("ns"))
            pos = 2;
        else if (staffPos.equalsIgnoreCase("ma"))
            pos = 3;
        else
            pos = 4;
        ////
        ResultSet rs = DB.db.staff(name, pos);
        try {
            while (rs.next()) {
                Staff t = new Staff();
                t.setFname(rs.getString("fname"));
                t.setLname(rs.getString("lname"));
                t.setAddress(rs.getString("address"));
                t.setEmail(rs.getString("email"));
                t.setPhone(rs.getString("phone"));
                t.setSsn(rs.getString("SSN"));
                t.setZip(rs.getString("zip"));
//                t.setbDate(rs.getDate("bdate").toString());
                ResultSet rss = DB.db.staffSchedule(rs.getInt("id"));
                while (rss.next()) {
                    ResultSet ress = DB.db.hospital(rss.getInt("hospid"));
                    while (ress.next()) {
                        t.setHospitals(ress.getString("name"));
                    }
                }
                rss = DB.db.qualification(rs.getInt("qualid"));
                while (rss.next()) {
                    t.setQualif(rss.getString("qualdesc"));
                }
                st.add((T) t);
            }
            DB.db.close();
        } catch (SQLException ex) {
            Logger.getLogger(MedOfficer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return st;
    }
}
