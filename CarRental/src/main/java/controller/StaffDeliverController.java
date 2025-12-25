package CarRental.example.controller;

import CarRental.example.document.RentalRecord;
import CarRental.example.document.Vehicle;
import CarRental.example.document.Staff;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.repository.VehicleRepository;
import CarRental.example.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/staff/deliver")
@CrossOrigin(origins = "*")
public class StaffDeliverController {

    @Autowired
    private RentalRecordRepository rentalRecordRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private StaffRepository staffRepository;

    @GetMapping("/vehicles-ready")
    public ResponseEntity<?> getVehiclesReadyForDelivery() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) return ResponseEntity.status(401).build();

            // SỬA LỖI: Dùng .orElse(null) cho Staff
            Staff staff = staffRepository.findByUsername(authentication.getName()).orElse(null);
            if (staff == null) return ResponseEntity.status(404).body("Staff not found");

            // Lấy các đơn đã thanh toán hoặc đã cọc tại trạm của nhân viên
            List<RentalRecord> records = rentalRecordRepository.findByStationIdAndStatusIn(
                    staff.getStationId(), List.of("PAID", "DEPOSIT_PENDING"));

            List<Map<String, Object>> response = new ArrayList<>();
            for (RentalRecord record : records) {
                Map<String, Object> map = new HashMap<>();
                map.put("rentalId", record.getId());
                map.put("username", record.getUsername());
                vehicleRepository.findById(record.getVehicleId()).ifPresent(v -> map.put("vehicleName", v.getName()));
                response.add(map);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/{rentalId}/photo")
    @Transactional
    public ResponseEntity<?> saveDeliveryPhoto(@PathVariable("rentalId") String rentalId, @RequestBody byte[] photoData) {
        return rentalRecordRepository.findById(rentalId).map(record -> {
            record.setDeliveryPhotoData(photoData);
            rentalRecordRepository.save(record);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{rentalId}/signature")
    @Transactional
    public ResponseEntity<?> saveDeliverySignature(@PathVariable("rentalId") String rentalId, @RequestBody byte[] signatureData) {
        return rentalRecordRepository.findById(rentalId).map(record -> {
            record.setSignatureData(signatureData);
            rentalRecordRepository.save(record);
            return ResponseEntity.ok(Map.of("success", true));
        }).orElse(ResponseEntity.notFound().build());
    }
}