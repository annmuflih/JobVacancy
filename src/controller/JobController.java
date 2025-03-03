package controller;

import model.DBConnector;
import view.JobView;

public class JobController {
    private JobView view;
    private DBConnector model;
    private int hrdId;
    
    public JobController(JobView jobView, DBConnector jobModel, int hrdId) {
        this.view = jobView; // Perbaiki inisialisasi
        this.model = jobModel;
        this.hrdId = hrdId;  // Simpan ID HRD dengan benar
    
        if (hrdId != -1) {
            System.out.println("Logged in as HRD with ID: " + hrdId);
        }
    }    
    public int getHrdId() {
        return hrdId;
    }
    
}
