package CarRental.example.controller;

import CarRental.example.document.RentalRecord;
import CarRental.example.document.User;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.repository.StationRepository;
import CarRental.example.repository.UserRepository;
import CarRental.example.repository.VehicleRepository;
import CarRental.example.service.RentalRecordService;
import CarRental.example.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rental")
public class RentalController {

    private final RentalRecordRepository rentalRepo;
    private final VehicleRepository vehicleRepo;
    private final StationRepository stationRepository;
    // Đã xóa SequenceGeneratorService
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

    // API lấy chi tiết đơn thuê cho Admin (đã sửa cho JPA)
    @GetMapping("/admin/detail/{id}")
    public ResponseEntity<?> getRentalDetailForAdmin(@PathVariable("id") String id) {
        try {
            RentalRecord record = rentalRepo.findById(id).orElse(null);
            if (record == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Không tìm thấy đơn thuê"));
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("rental", record);

            // Lấy thông tin xe
            if (record.getVehicleId() != null) {
                response.put("vehicle", vehicleRepo.findById(record.getVehicleId()).orElse(null));
            }

            // Lấy thông tin trạm
            if (record.getStationId() != null) {
                response.put("station", stationRepository.findById(record.getStationId()).orElse(null));
            }

            // Lấy thông tin khách hàng
            if (record.getUsername() != null) {
                User user = userRepository.findByUsername(record.getUsername());
                if(user != null) {
                    // Ẩn thông tin nhạy cảm trước khi trả về JSON
                    user.setPassword(null);
                    user.setLicenseData(null); // Không gửi ảnh về để giảm dung lượng
                    user.setIdCardData(null);
                    response.put("customer", user);
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}