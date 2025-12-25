package CarRental.example.service;

import CarRental.example.document.VehicleReport;
import CarRental.example.document.Vehicle;
import CarRental.example.repository.VehicleReportRepository;
import CarRental.example.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleReportService {

    @Autowired
    private VehicleReportRepository vehicleReportRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    public VehicleReport createReport(VehicleReport report) {
        return vehicleReportRepository.save(report);
    }

    public List<VehicleReport> getReportsByVehicleId(String vehicleId) {
        return vehicleReportRepository.findByVehicleId(vehicleId);
    }

    public VehicleReport updateReportStatus(String reportId, String newStatus) {
        Optional<VehicleReport> reportOpt = vehicleReportRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            VehicleReport report = reportOpt.get();
            report.setStatus(newStatus);
            VehicleReport updated = vehicleReportRepository.save(report);

            // Nếu báo cáo đã xử lý xong (RESOLVED), cập nhật xe về trạng thái sẵn sàng
            if ("RESOLVED".equalsIgnoreCase(newStatus)) {
                vehicleRepository.findById(report.getVehicleId()).ifPresent(vehicle -> {
                    vehicle.setAvailable(true);
                    vehicle.setBookingStatus("AVAILABLE");
                    vehicle.setIssue(null); // Xóa bỏ ghi chú lỗi trên xe
                    vehicle.setIssueSeverity(null);
                    vehicleRepository.save(vehicle);
                });
            }
            return updated;
        }
        return null;
    }

    public List<VehicleReport> getAllReports() {
        return vehicleReportRepository.findAll();
    }

    public void deleteReport(String reportId) {
        vehicleReportRepository.deleteById(reportId);
    }
}