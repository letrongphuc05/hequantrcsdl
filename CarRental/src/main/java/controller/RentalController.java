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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/rental")
public class RentalController {

    private final RentalRecordRepository rentalRepo;
    private final VehicleRepository vehicleRepo;
    private final StationRepository stationRepository;
    // ĐÃ XÓA SequenceGeneratorService
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

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    // API Đặt xe (Quan trọng nhất)
    @PostMapping(value = "/book", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> bookRental(@RequestParam("vehicleId") String vehicleId,
                                        @RequestParam("stationId") String stationId,
                                        @RequestParam("startDate") String startDateStr,
                                        @RequestParam("endDate") String endDateStr,
                                        @RequestParam(value = "distanceKm", required = false) Double distanceKm) {

        String username = getCurrentUsername();
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        Vehicle vehicle = vehicleRepo.findById(vehicleId).orElse(null);
        if (vehicle == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Xe không tồn tại");

        if ("RENTED".equalsIgnoreCase(vehicle.getBookingStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Xe đã được thuê");
        }

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        int rentalDays = (int) Math.max(1, ChronoUnit.DAYS.between(startDate, endDate) + 1);

        // Tạo đơn thuê (MySQL tự sinh ID)
        RentalRecord record = new RentalRecord();
        record.setUsername(username);
        record.setVehicleId(vehicleId);
        record.setStationId(stationId);
        record.setStartDate(startDate);
        record.setEndDate(endDate);
        record.setRentalDays(rentalDays);
        record.setTotal(vehicle.getPrice() * rentalDays);
        record.setStatus("PENDING_PAYMENT");
        record.setPaymentStatus("PENDING");
        record.setCreatedAt(LocalDateTime.now());
        record.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10)); // Giữ xe 10 phút

        // Lưu vào DB để lấy ID
        record = rentalRepo.save(record);

        // Cập nhật trạng thái xe
        vehicleRepo.updateBookingStatus(vehicleId, "PENDING_PAYMENT");

        return ResponseEntity.ok(record);
    }

    // API Checkout hiển thị thông tin trước khi đặt
    @PostMapping("/checkout")
    public Map<String, Object> checkout(@RequestBody Map<String, Object> req) {
        String vehicleId = (String) req.get("vehicleId");
        String stationId = (String) req.get("stationId");
        Vehicle v = vehicleRepo.findById(vehicleId).orElse(null);

        if (v == null) return Map.of("error", "Vehicle not found");

        Map<String, Object> data = new HashMap<>();
        data.put("vehicleId", v.getId());
        data.put("vehicleName", v.getName());
        data.put("price", v.getPrice());
        data.put("stationId", stationId);
        return data;
    }

    // API Lịch sử thuê
    @GetMapping("/history")
    public ResponseEntity<?> history() {
        String username = getCurrentUsername();
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(rentalRecordService.getHistoryDetails(username));
    }

    // API Admin xem chi tiết
    @GetMapping("/admin/detail/{id}")
    public ResponseEntity<?> getRentalDetailForAdmin(@PathVariable("id") String id) {
        RentalRecord record = rentalRepo.findById(id).orElse(null);
        if (record == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("rental", record);

        if (record.getVehicleId() != null)
            response.put("vehicle", vehicleRepo.findById(record.getVehicleId()).orElse(null));

        if (record.getUsername() != null) {
            User user = userRepository.findByUsername(record.getUsername());
            if (user != null) {
                user.setPassword(null);
                user.setLicenseData(null); // Không gửi ảnh nặng về
                user.setIdCardData(null);
                response.put("customer", user);
            }
        }
        return ResponseEntity.ok(response);
    }

    // API Hủy đơn
    @PostMapping("/{rentalId}/cancel")
    public ResponseEntity<?> cancelRental(@PathVariable String rentalId) {
        RentalRecord record = rentalRepo.findById(rentalId).orElse(null);
        if (record != null) {
            record.setStatus("CANCELLED");
            rentalRepo.save(record);
            // Trả xe về trạng thái sẵn sàng
            vehicleRepo.updateBookingStatus(record.getVehicleId(), "AVAILABLE");
            return ResponseEntity.ok("Đã hủy đơn");
        }
        return ResponseEntity.badRequest().body("Lỗi");
    }
}