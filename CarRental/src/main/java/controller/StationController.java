package CarRental.example.controller;

import CarRental.example.document.Station;
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

    public StationController(StationRepository stationRepo, VehicleRepository vehicleRepo) {
        this.stationRepo = stationRepo;
        this.vehicleRepo = vehicleRepo;
    }

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

            // Gọi hàm countByStationIdAndBookingStatus đã fix ở Repository
            long availableCars = vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "AVAILABLE");
            map.put("availableCars", availableCars);

            data.add(map);
        }
        return data;
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Map<String, Object>>> getStationsForAdmin() {
        List<Station> stations = stationRepo.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Station st : stations) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", st.getId());
            map.put("name", st.getName());
            map.put("address", st.getAddress());

            // Thống kê số lượng xe theo các trạng thái khác nhau
            map.put("statsAvailable", vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "AVAILABLE"));
            map.put("statsRented", vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "RENTED")
                    + vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "PENDING_PAYMENT"));
            map.put("statsMaintenance", vehicleRepo.countByStationIdAndBookingStatus(st.getId(), "MAINTENANCE"));

            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/add")
    public Station addStation(@RequestBody Station station) {
        return stationRepo.save(station);
    }

    // Các phương thức Update/Delete giữ nguyên...
}