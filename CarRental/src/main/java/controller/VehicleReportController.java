package CarRental.example.controller;

import CarRental.example.document.VehicleReport;
import CarRental.example.document.RentalRecord;
import CarRental.example.repository.VehicleReportRepository;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.service.VehicleReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicle-reports")
public class VehicleReportController {

    @Autowired
    private VehicleReportService vehicleReportService;

    @Autowired
    private VehicleReportRepository vehicleReportRepository;

    @Autowired
    private RentalRecordRepository rentalRecordRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllReports() {
        List<VehicleReport> reports = vehicleReportService.getAllReports();
        return ResponseEntity.ok(Map.of("success", true, "reports", reports));
    }

    @PostMapping("/upload-photo/{vehicleId}")
    @Transactional // QUAN TRỌNG: Để update blob data vào MySQL
    public ResponseEntity<?> uploadReportPhoto(@PathVariable("vehicleId") String vehicleId, @RequestBody byte[] photoData) {
        try {
            // Tìm đơn thuê đang thực hiện của xe này
            List<RentalRecord> records = rentalRecordRepository.findByVehicleIdAndStatus(vehicleId, "IN_PROGRESS");

            // Sửa lỗi: Dùng getReturnDate() thay cho getEndTime()
            RentalRecord targetRecord = records.stream()
                    .sorted((r1, r2) -> {
                        if (r1.getReturnDate() == null) return 1;
                        if (r2.getReturnDate() == null) return -1;
                        return r2.getReturnDate().compareTo(r1.getReturnDate());
                    })
                    .findFirst()
                    .orElse(null);

            if (targetRecord == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy hợp đồng thuê xe đang kích hoạt"));
            }

            // Lưu ảnh binary data vào RentalRecord
            targetRecord.setDeliveryPhotoData(photoData);
            rentalRecordRepository.save(targetRecord);

            return ResponseEntity.ok(Map.of("success", true, "message", "Lưu ảnh báo cáo thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{reportId}")
    @Transactional
    public ResponseEntity<?> deleteReport(@PathVariable("reportId") String reportId) {
        vehicleReportService.deleteReport(reportId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Xóa báo cáo thành công"));
    }
}