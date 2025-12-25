package CarRental.example.controller;

import CarRental.example.document.Staff;
import CarRental.example.document.Station;
import CarRental.example.repository.StaffRepository;
import CarRental.example.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional; // Bắt buộc phải có import này

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffVehicleController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StationRepository stationRepository;

    @GetMapping("/current-station")
    public ResponseEntity<?> getCurrentStationOfStaff() {
        try {
            // 1. Lấy thông tin user hiện tại từ Authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }
            String username = authentication.getName();

            // 2. SỬA LỖI: Thêm .orElse(null) để lấy Staff ra khỏi Optional
            Staff staff = staffRepository.findByUsername(username).orElse(null);

            if (staff == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy nhân viên"));
            }

            // 3. Lấy thông tin trạm dựa trên stationId của nhân viên
            String stationId = staff.getStationId();
            if (stationId == null || stationId.isBlank()) {
                return ResponseEntity.status(404).body(Map.of("error", "Nhân viên chưa được gán vào trạm"));
            }

            Station station = stationRepository.findById(stationId).orElse(null);

            if (station == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy trạm tương ứng"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", station.getId());
            response.put("name", station.getName());
            response.put("address", station.getAddress());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }
}