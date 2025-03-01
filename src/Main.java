// src/Main.java
import javax.swing.*;
import controller.JobController;
import view.JobView;
import model.DBConnector;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"HRD", "Pelamar"};
            int choice = JOptionPane.showOptionDialog(null, "Login as:", "User Login",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            boolean isHRD = (choice == 0);

            DBConnector jobModel = new DBConnector();
            JobView jobView = new JobView(isHRD);
            JobController jobController = new JobController(jobView, jobModel);  // Simpan referensi

            jobView.setJobController(jobController); // Kirim ke View
        });
    }
}
