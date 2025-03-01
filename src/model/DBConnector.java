package model;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/job_vacancy";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // No password

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL Driver
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found!", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }

    public static void deleteJob(int jobId) {
        try (Connection connection = getConnection()) {
            String query = "DELETE FROM loker WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, jobId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete job", e);
        }
    }
    
    public static ResultSet getApplicantsForJob(int jobId) {
        try {
            Connection connection = getConnection();
            String query = "SELECT pelamar.nama, pelamar.email, pelamar.telepon FROM pelamar " +
                           "JOIN loker ON pelamar.id = loker.pelamar_id WHERE loker.id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, jobId);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve applicants", e);
        }
    }
}