package controller;

import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import model.DBConnector;
import view.JobView;

public class JobController {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable jobTable;
    private Connection connection;
    private boolean isHRD;

    public JobController(JobView jobView, DBConnector jobModel) {
        //TODO Auto-generated constructor stub
    }
    public void loadJobsFromDatabase() {
        try {
            tableModel.setRowCount(0); // Clear table before reloading
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM loker");
    
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_job"),
                    rs.getString("perusahaan"),
                    rs.getString("alamat"),
                    rs.getString("gaji"),
                    rs.getString("tanggal_posting")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addJob() {
        JTextField jobNameField = new JTextField();
        JTextField companyField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField paymentField = new JTextField();
        JTextField postDateField = new JTextField();
    
        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Job Name:")); panel.add(jobNameField);
        panel.add(new JLabel("Company:")); panel.add(companyField);
        panel.add(new JLabel("Address:")); panel.add(addressField);
        panel.add(new JLabel("Payment:")); panel.add(paymentField);
        panel.add(new JLabel("Post Date:")); panel.add(postDateField);
    
        int result = JOptionPane.showConfirmDialog(frame, panel, "Enter Job Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                PreparedStatement pstmt = connection.prepareStatement("INSERT INTO loker (nama_job, perusahaan, alamat, gaji, tanggal_posting) VALUES (?, ?, ?, ?, ?)");
                pstmt.setString(1, jobNameField.getText());
                pstmt.setString(2, companyField.getText());
                pstmt.setString(3, addressField.getText());
                pstmt.setString(4, paymentField.getText());
                pstmt.setString(5, postDateField.getText());
                pstmt.executeUpdate();
                loadJobsFromDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void deleteJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow != -1) {
            int jobId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this job?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                DBConnector.deleteJob(jobId);
                loadJobsFromDatabase();
                JOptionPane.showMessageDialog(frame, "Job deleted successfully.");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a job to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void applyJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow != -1) {
            String name = JOptionPane.showInputDialog(frame, "Enter Your Name:");
            String email = JOptionPane.showInputDialog(frame, "Enter Your Email:");
            String phone = JOptionPane.showInputDialog(frame, "Enter Your Phone Number:");
    
            if (name != null && email != null && phone != null &&
                !name.trim().isEmpty() && !email.trim().isEmpty() && !phone.trim().isEmpty()) {
                try {
                    Connection conn = DBConnector.getConnection();
    
                    // **1. Insert pelamar dan dapatkan id yang baru**
                    String insertPelamarSQL = "INSERT INTO pelamar (nama, email, telepon) VALUES (?, ?, ?)";
                    PreparedStatement pelamarStmt = conn.prepareStatement(insertPelamarSQL, Statement.RETURN_GENERATED_KEYS);
                    pelamarStmt.setString(1, name);
                    pelamarStmt.setString(2, email);
                    pelamarStmt.setString(3, phone);
                    pelamarStmt.executeUpdate();
    
                    ResultSet generatedKeys = pelamarStmt.getGeneratedKeys();
                    int pelamarId = -1;
                    if (generatedKeys.next()) {
                        pelamarId = generatedKeys.getInt(1); // Mendapatkan ID pelamar
                    }
    
                    // **2. Update loker dengan pelamar_id**
                    if (pelamarId != -1) {
                        int jobId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
                        String updateLokerSQL = "UPDATE loker SET pelamar_id = ? WHERE id = ?";
                        PreparedStatement lokerStmt = conn.prepareStatement(updateLokerSQL);
                        lokerStmt.setInt(1, pelamarId);
                        lokerStmt.setInt(2, jobId);
                        lokerStmt.executeUpdate();
                    }
    
                    JOptionPane.showMessageDialog(frame, "Application submitted successfully!");
    
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Failed to submit application.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a job to apply.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public void editJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow != -1) {
            String jobName = JOptionPane.showInputDialog(frame, "Enter new Job Name:", tableModel.getValueAt(selectedRow, 1));
            String company = JOptionPane.showInputDialog(frame, "Enter new Company:", tableModel.getValueAt(selectedRow, 2));
            String address = JOptionPane.showInputDialog(frame, "Enter new Address:", tableModel.getValueAt(selectedRow, 3));
            String payment = JOptionPane.showInputDialog(frame, "Enter new Payment:", tableModel.getValueAt(selectedRow, 4));
            String postDate = JOptionPane.showInputDialog(frame, "Enter new Post Date:", tableModel.getValueAt(selectedRow, 5));
            
            int jobId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            try {
                String query = "UPDATE loker SET nama_job = ?, perusahaan = ?, alamat = ?, gaji = ?, tanggal_posting = ? WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, jobName);
                pstmt.setString(2, company);
                pstmt.setString(3, address);
                pstmt.setString(4, payment);
                pstmt.setString(5, postDate);
                pstmt.setInt(6, jobId);
                pstmt.executeUpdate();
                loadJobsFromDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a job to edit.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void viewApplicants() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow != -1) {
            int jobId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            try {
                ResultSet rs = DBConnector.getApplicantsForJob(jobId);
                StringBuilder applicantsList = new StringBuilder("Applicants:\n\n");
                while (rs.next()) {
                    applicantsList.append("Name: ").append(rs.getString("nama"))
                                  .append(", Email: ").append(rs.getString("email"))
                                  .append(", Phone: ").append(rs.getString("telepon"))
                                  .append("\n");
                }
                JOptionPane.showMessageDialog(frame, applicantsList.toString(), "Applicants List", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to retrieve applicants.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a job to view applicants.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
