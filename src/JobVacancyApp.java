import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class JobVacancyApp {
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTable jobTable;
    private Connection connection;
    private boolean isHRD;
    
    public JobVacancyApp(boolean isHRD) {
        this.isHRD = isHRD;
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
        
        if (isHRD) {
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
    
    private void deleteJob() {
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
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"HRD", "Pelamar"};
            int choice = JOptionPane.showOptionDialog(null, "Login as:", "User Login",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            new JobVacancyApp(choice == 0);
        });
    }
}