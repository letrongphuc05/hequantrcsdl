package CarRental.example.controller;

import CarRental.example.document.Station;
import CarRental.example.repository.StationRepository;
import CarRental.example.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // Cần thiết cho MySQL
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stations")
public class StationController {
    private final StationRepository stationRepo;
    private final VehicleRepository vehicleRepo;

    public StationController(StationRepository stationRepo, VehicleRepository vehicleRepo) {
        this.stationRepo = stationRepo;
        this.vehicleRepo = vehicleRepo;
    }

    // Lấy danh sách trạm và tính toán số xe khả dụng thực tế từ DB
    @GetMapping
    public List<Map<String, Object>> getStations() {
        List<Station> stations = stationRepo.findAll();
        List<Map<String, Object>> data = new ArrayList<>();

        for (Station st : stations) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", st.getId());
            map.put("name", st.getName());
            map.put("latitude", st.getLatitude());
            map.put("longitude", st.getLongitude());
            map.put("address", st.getAddress());

            // MySQL JPA trả về kiểu long cho hàm count
            long availableCount = vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "AVAILABLE");
            map.put("availableCars", availableCount);

            data.add(map);
        }
        return data;
    }

    // API thống kê dành cho Admin Dashboard
    @GetMapping("/admin/all")
    public ResponseEntity<List<Map<String, Object>>> getStationsForAdmin() {
        List<Station> stations = stationRepo.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Station st : stations) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", st.getId());
            map.put("name", st.getName());
            map.put("address", st.getAddress());

            // Thống kê số lượng xe theo từng trạng thái tại trạm
            long available = vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "AVAILABLE");
            long rented = vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "RENTED")
                    + vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "PENDING_PAYMENT");
            long maintenance = vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "MAINTENANCE");

            map.put("statsAvailable", available);
            map.put("statsRented", rented);
            map.put("statsMaintenance", maintenance);

            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    // Thêm trạm mới - Cần @Transactional để lưu vào MySQL thành công
    @PostMapping("/admin/add")
    @Transactional
    public ResponseEntity<Station> addStation(@RequestBody Station station) {
        Station saved = stationRepo.save(station);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Cập nhật trạm - Cần @Transactional
    @PutMapping("/admin/update/{id}")
    @Transactional
    public ResponseEntity<Station> updateStation(@PathVariable String id, @RequestBody Station station) {
        return stationRepo.findById(id).map(existing -> {
            existing.setName(station.getName());
            existing.setAddress(station.getAddress());
            existing.setLatitude(station.getLatitude());
            existing.setLongitude(station.getLongitude());
            return ResponseEntity.ok(stationRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Xóa trạm - Cần @Transactional
    @DeleteMapping("/admin/delete/{id}")
    @Transactional
    public ResponseEntity<String> deleteStation(@PathVariable String id) {
        if (stationRepo.existsById(id)) {
            stationRepo.deleteById(id);
            return ResponseEntity.ok("Đã xóa trạm thành công");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy trạm");
    }
}