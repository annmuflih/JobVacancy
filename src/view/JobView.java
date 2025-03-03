// src/view/JobVacancyView.java
package view;

import controller.JobController;
import model.DBConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class JobView extends JFrame {
    private JPanel buttonPanel;
    private JobController jobController;  // Tambahkan atribut controller

    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable jobTable;
    private Connection connection;
    private boolean isHRD;
    private int hrdId;
    
    public JobView(boolean isHRD,int hrdId) {
        this.isHRD = isHRD;
        this.hrdId = hrdId;
        connection = DBConnector.getConnection();
        frame = new JFrame("Job Vacancy Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        String[] columnNames = {"ID", "Job Name", "Company", "Address", "Payment", "Post Date"};
        tableModel = new DefaultTableModel(columnNames, 0);
        jobTable = new JTable(tableModel);
        jobTable.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(jobTable);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        if (isHRD && hrdId == 1) {
            JButton addButton = new JButton("Add Job");
            addButton.setFont(new Font("Arial", Font.BOLD, 12));
            addButton.setBackground(new Color(34, 167, 240));
            addButton.setForeground(Color.WHITE);
            addButton.addActionListener(e -> addJob());
            buttonPanel.add(addButton);

            JButton editButton = new JButton("Edit Job");
            editButton.setFont(new Font("Arial", Font.BOLD, 12));
            editButton.setBackground(new Color(255, 193, 7));
            editButton.setForeground(Color.WHITE);
            editButton.addActionListener(e -> editJob());
            buttonPanel.add(editButton);
            
            JButton deleteButton = new JButton("Delete Job");
            deleteButton.setFont(new Font("Arial", Font.BOLD, 12));
            deleteButton.setBackground(new Color(220, 53, 69));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.addActionListener(e -> deleteJob());
            buttonPanel.add(deleteButton);

            JButton viewApplicantsButton = new JButton("View Applicants");
            viewApplicantsButton.setFont(new Font("Arial", Font.BOLD, 12));
            viewApplicantsButton.setBackground(new Color(100, 149, 237));
            viewApplicantsButton.setForeground(Color.WHITE);
            viewApplicantsButton.addActionListener(e -> viewApplicants());
            buttonPanel.add(viewApplicantsButton);
        } else {
            JButton applyButton = new JButton("Apply");
            applyButton.setFont(new Font("Arial", Font.BOLD, 12));
            applyButton.setBackground(new Color(38, 166, 91));
            applyButton.setForeground(Color.WHITE);
            applyButton.addActionListener(e -> applyJob());
            buttonPanel.add(applyButton);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
        loadJobsFromDatabase();
    }
    private void loadJobsFromDatabase() {
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
    
    private void addJob() {
    JTextField jobNameField = new JTextField();
    JTextField companyField = new JTextField();
    JTextField addressField = new JTextField();
    JTextField paymentField = new JTextField();
    JTextField postDateField = new JTextField(); // Format: YYYY-MM-DD

    JPanel panel = new JPanel(new GridLayout(5, 2));
    panel.add(new JLabel("Job Name:")); panel.add(jobNameField);
    panel.add(new JLabel("Company:")); panel.add(companyField);
    panel.add(new JLabel("Address:")); panel.add(addressField);
    panel.add(new JLabel("Payment:")); panel.add(paymentField);
    panel.add(new JLabel("Post Date (YYYY-MM-DD):")); panel.add(postDateField);

    int result = JOptionPane.showConfirmDialog(frame, panel, "Enter Job Details", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        try {
            // Konversi gaji ke integer
            int payment = Integer.parseInt(paymentField.getText());

            // Konversi tanggal ke java.sql.Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = dateFormat.parse(postDateField.getText());
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            // Simpan ke database
            PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO loker (nama_job, perusahaan, alamat, gaji, tanggal_posting, hrd_id) VALUES (?, ?, ?, ?, ?, ?)"
            );
            pstmt.setString(1, jobNameField.getText());
            pstmt.setString(2, companyField.getText());
            pstmt.setString(3, addressField.getText());
            pstmt.setInt(4, payment);
            pstmt.setDate(5, sqlDate);
            pstmt.setInt(6, hrdId);
            pstmt.executeUpdate();
            
            loadJobsFromDatabase();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid input for Payment. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(frame, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
    
    public JTable getJobTable() {
        return jobTable;
    }
    
    public void deleteJob() {
        int selectedRow = getJobTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a job to delete.");
            return;
        }
    
        int jobId = (int) getJobTable().getValueAt(selectedRow, 0);
    
        int confirm = JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete this job? This will also remove all applicants!", 
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
    
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnector.getConnection()) {
                // Hapus pelamar yang terkait dengan job ini
                String deletePelamarSQL = "DELETE FROM pelamar WHERE id IN (SELECT pelamar_id FROM loker WHERE id = ?)";
                try (PreparedStatement pelamarStmt = conn.prepareStatement(deletePelamarSQL)) {
                    pelamarStmt.setInt(1, jobId);
                    pelamarStmt.executeUpdate();
                }
    
                // Hapus job dari loker
                String deleteLokerSQL = "DELETE FROM loker WHERE id = ?";
                try (PreparedStatement jobStmt = conn.prepareStatement(deleteLokerSQL)) {
                    jobStmt.setInt(1, jobId);
                    int rowsDeleted = jobStmt.executeUpdate();
    
                    if (rowsDeleted > 0) {
                        JOptionPane.showMessageDialog(null, "Job and related applicants deleted successfully.");
                        
                        // Refresh tabel dengan memuat ulang data dari database
                        loadJobsFromDatabase();
    
                        // Memastikan tabel langsung diperbarui di UI
                        SwingUtilities.invokeLater(() -> {
                            tableModel.fireTableDataChanged();
                        });
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to delete job.");
            }
        }
    }    
    
    private void applyJob() {
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
    
    
    private void editJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a job to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        // Ambil data dari tabel
        String jobName = tableModel.getValueAt(selectedRow, 1).toString();
        String company = tableModel.getValueAt(selectedRow, 2).toString();
        String address = tableModel.getValueAt(selectedRow, 3).toString();
        int payment = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());
        String postDate = tableModel.getValueAt(selectedRow, 5).toString(); // Format YYYY-MM-DD
    
        // Buat input fields dengan nilai default
        JTextField jobNameField = new JTextField(jobName);
        JTextField companyField = new JTextField(company);
        JTextField addressField = new JTextField(address);
        JTextField paymentField = new JTextField(String.valueOf(payment));
        JTextField postDateField = new JTextField(postDate);
    
        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Job Name:")); panel.add(jobNameField);
        panel.add(new JLabel("Company:")); panel.add(companyField);
        panel.add(new JLabel("Address:")); panel.add(addressField);
        panel.add(new JLabel("Payment:")); panel.add(paymentField);
        panel.add(new JLabel("Post Date (YYYY-MM-DD):")); panel.add(postDateField);
    
        int result = JOptionPane.showConfirmDialog(frame, panel, "Edit Job Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int jobId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            try {
                // Konversi gaji ke integer
                int newPayment = Integer.parseInt(paymentField.getText());
    
                // Konversi tanggal ke java.sql.Date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                java.util.Date utilDate = dateFormat.parse(postDateField.getText());
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
    
                // Simpan ke database
                String query = "UPDATE loker SET nama_job = ?, perusahaan = ?, alamat = ?, gaji = ?, tanggal_posting = ? WHERE id = ?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, jobNameField.getText());
                pstmt.setString(2, companyField.getText());
                pstmt.setString(3, addressField.getText());
                pstmt.setInt(4, newPayment);
                pstmt.setDate(5, sqlDate);
                pstmt.setInt(6, jobId);
                pstmt.executeUpdate();
                
                loadJobsFromDatabase();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid input for Payment. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    private void viewApplicants() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow != -1) {
            int jobId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
            try {
                ResultSet rs = DBConnector.getApplicantsForJob(jobId);
                StringBuilder applicantsList = new StringBuilder("Applicants:\n\n");
                while (rs.next()) {
                    applicantsList.append("Name: ").append(rs.getString("nama"))
                                  .append(", Email: ").append(rs.getString("email"))
                                  .append(", Phone: ").append(rs.getInt("telepon"))
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
    public void setJobController(JobController jobController) {
        this.jobController = jobController;
    }
}
