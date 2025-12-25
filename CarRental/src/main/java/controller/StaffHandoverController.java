package CarRental.example.controller;

import CarRental.example.document.RentalRecord;
import CarRental.example.document.Vehicle;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // Cực kỳ quan trọng
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff/handover")
@CrossOrigin(origins = "*")
public class StaffHandoverController {

    private final RentalRecordRepository rentalRepo;
    private final VehicleRepository vehicleRepo;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StaffHandoverController(RentalRecordRepository rentalRepo, VehicleRepository vehicleRepo) {
        this.rentalRepo = rentalRepo;
        this.vehicleRepo = vehicleRepo;
    }

    // 1. Lấy danh sách các đơn cần bàn giao xe
    @GetMapping("/list")
    public List<Map<String, Object>> listHandovers() {
        List<Map<String, Object>> result = new ArrayList<>();
        // Tìm các đơn đã thanh toán (PAID) hoặc đang chờ bàn giao
        List<RentalRecord> allRecords = rentalRepo.findAll();

        for (RentalRecord record : allRecords) {
            // Chỉ lấy các đơn đang ở trạng thái chờ bàn giao xe (ví dụ: PAID hoặc DEPOSIT_PENDING)
            if ("PAID".equals(record.getStatus()) || "READY".equals(record.getStatus())) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", record.getId());
                map.put("username", record.getUsername());
                map.put("pickupDate", record.getPickupDate() != null ? record.getPickupDate().format(dateFormatter) : "N/A");

                vehicleRepo.findById(record.getVehicleId()).ifPresent(v -> {
                    map.put("vehicleName", v.getName());
                    map.put("plate", v.getPlate());
                });

                result.add(map);
            }
        }
        return result;
    }

    // 2. Lấy chi tiết đơn bàn giao
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getHandoverDetail(@PathVariable("id") String id) {
        RentalRecord record = rentalRepo.findById(id).orElse(null);
        if (record == null) return ResponseEntity.notFound().build();

        Map<String, Object> response = new HashMap<>();
        response.put("rental", record);

        vehicleRepo.findById(record.getVehicleId()).ifPresent(v -> response.put("vehicle", v));

        return ResponseEntity.ok(response);
    }

    // 3. Xác nhận đã giao xe cho khách - Cần @Transactional để lưu vào MySQL thành công
    @PostMapping("/confirm/{id}")
    @Transactional
    public ResponseEntity<?> confirmHandover(@PathVariable("id") String id) {
        RentalRecord record = rentalRepo.findById(id).orElse(null);
        if (record == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy đơn thuê"));
        }

        // Cập nhật trạng thái đơn thuê sang đang thực hiện (IN_PROGRESS hoặc ACTIVE)
        record.setStatus("IN_PROGRESS");
        rentalRepo.save(record);

        // Cập nhật trạng thái xe sang đã cho thuê (RENTED)
        vehicleRepo.updateBookingStatus(record.getVehicleId(), "RENTED");

        return ResponseEntity.ok(Map.of("success", true, "message", "Xác nhận bàn giao xe thành công"));
    }
}