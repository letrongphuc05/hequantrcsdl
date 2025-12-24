package CarRental.example.controller;

import CarRental.example.document.Station;
import CarRental.example.document.Vehicle;
import CarRental.example.repository.StationRepository;
import CarRental.example.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stations")
public class StationController {
    private final StationRepository stationRepo;
    private final VehicleRepository vehicleRepo;
    // Đã xóa SequenceGeneratorService

    public StationController(StationRepository stationRepo,
                             VehicleRepository vehicleRepo) {
        this.stationRepo = stationRepo;
        this.vehicleRepo = vehicleRepo;
    }

    // API cho trang chủ (Khách hàng)
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

            // JPA: Đếm số xe có trạng thái AVAILABLE tại trạm
            List<Vehicle> availableVehicles = vehicleRepo.findByStationIdAndBookingStatus(st.getId(), "AVAILABLE");
            map.put("availableCars", availableVehicles.size());

            data.add(map);
        }
        return data;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStation(@PathVariable("id") String id) {
        Optional<Station> station = stationRepo.findById(id);
        return station.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Station not found"));
    }

    // API cho Admin Dashboard
    @GetMapping("/admin/all")
    public ResponseEntity<List<Map<String, Object>>> getStationsForAdmin() {
        List<Station> stations = stationRepo.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Station st : stations) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", st.getId());
            map.put("name", st.getName());
            map.put("address", st.getAddress());
            map.put("latitude", st.getLatitude());
            map.put("longitude", st.getLongitude());

            // 1. Sẵn sàng
            int countAvailable = vehicleRepo.findByStationIdAndBookingStatus(st.getId(), "AVAILABLE").size();

            // 2. Đang thuê (RENTED + PENDING_PAYMENT)
            int countRented = vehicleRepo.findByStationIdAndBookingStatus(st.getId(), "RENTED").size();
            int countPending = vehicleRepo.findByStationIdAndBookingStatus(st.getId(), "PENDING_PAYMENT").size();

            // 3. Bảo trì
            int countMaintenance = vehicleRepo.findByStationIdAndBookingStatus(st.getId(), "MAINTENANCE").size();

            map.put("statsAvailable", countAvailable);
            map.put("statsRented", countRented + countPending);
            map.put("statsMaintenance", countMaintenance);

            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/{id}")
    public Optional<Station> getStationById(@PathVariable("id") String id) {
        return stationRepo.findById(id);
    }

    @PostMapping("/admin/add")
    public Station addStation(@RequestBody Station station) {
        // MySQL tự sinh ID, không cần set thủ công
        return stationRepo.save(station);
    }

    @PutMapping("/admin/update/{id}")
    public Station updateStation(@PathVariable("id") String id, @RequestBody Station updatedStation) {
        updatedStation.setId(id);
        return stationRepo.save(updatedStation);
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<String> deleteStation(@PathVariable("id") String id) {
        // Kiểm tra nếu trạm còn xe thì không cho xóa
        List<Vehicle> vehiclesInStation = vehicleRepo.findByStationId(id);
        if (!vehiclesInStation.isEmpty()) {
            return new ResponseEntity<>("Không thể xóa trạm vì vẫn còn xe.", HttpStatus.BAD_REQUEST);
        }
        stationRepo.deleteById(id);
        return new ResponseEntity<>("Xóa trạm " + id + " thành công!", HttpStatus.OK);
    }
}