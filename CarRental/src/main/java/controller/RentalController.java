package CarRental.example.controller;

import CarRental.example.document.RentalRecord;
import CarRental.example.document.User;
import CarRental.example.document.Vehicle;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.repository.StationRepository;
import CarRental.example.repository.UserRepository;
import CarRental.example.repository.VehicleRepository;
import CarRental.example.service.RentalRecordService;
import CarRental.example.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // QUAN TRỌNG
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/rental")
public class RentalController {

    private final RentalRecordRepository rentalRepo;
    private final VehicleRepository vehicleRepo;
    private final StationRepository stationRepository;
    private final VehicleService vehicleService;
    private final RentalRecordService rentalRecordService;
    private final UserRepository userRepository;

    public RentalController(RentalRecordRepository rentalRepo,
                            VehicleRepository vehicleRepo,
                            StationRepository stationRepository,
                            VehicleService vehicleService,
                            RentalRecordService rentalRecordService,
                            UserRepository userRepository) {
        this.rentalRepo = rentalRepo;
        this.vehicleRepo = vehicleRepo;
        this.stationRepository = stationRepository;
        this.vehicleService = vehicleService;
        this.rentalRecordService = rentalRecordService;
        this.userRepository = userRepository;
    }

    // Lấy chi tiết đơn thuê cho Admin
    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getRentalDetailForAdmin(@PathVariable("id") String id) {
        RentalRecord record = rentalRepo.findById(id).orElse(null);
        if (record == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn thuê");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("rental", record);

        // Lấy thông tin xe
        if (record.getVehicleId() != null) {
            vehicleRepo.findById(record.getVehicleId()).ifPresent(v -> response.put("vehicle", v));
        }

        // Lấy thông tin khách hàng (ẩn mật khẩu)
        if (record.getUsername() != null) {
            User user = userRepository.findByUsername(record.getUsername());
            if (user != null) {
                user.setPassword(null);
                user.setLicenseData(null);
                user.setIdCardData(null);
                response.put("customer", user);
            }
        }
        return ResponseEntity.ok(response);
    }

    // API Hủy đơn - Cần @Transactional để update DB thành công
    @PostMapping("/{rentalId}/cancel")
    @Transactional
    public ResponseEntity<?> cancelRental(@PathVariable String rentalId) {
        RentalRecord record = rentalRepo.findById(rentalId).orElse(null);
        if (record != null) {
            // 1. Cập nhật trạng thái đơn thuê
            record.setStatus("CANCELLED");
            rentalRepo.save(record);

            // 2. Trả xe về trạng thái sẵn sàng (Sử dụng hàm updateBookingStatus đã tạo ở VehicleRepository)
            vehicleRepo.updateBookingStatus(record.getVehicleId(), "AVAILABLE");

            // 3. Nếu Service của bạn có hàm giải phóng giữ chỗ, hãy gọi nó
            vehicleService.releaseHold(record.getVehicleId(), rentalId);

            return ResponseEntity.ok("Đã hủy đơn thuê thành công");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy đơn thuê để hủy");
    }

    // Lấy lịch sử thuê của người dùng hiện tại
    @GetMapping("/history/{username}")
    public ResponseEntity<List<Map<String, Object>>> getUserHistory(@PathVariable String username) {
        List<Map<String, Object>> history = rentalRecordService.getHistoryDetails(username);
        return ResponseEntity.ok(history);
    }
}