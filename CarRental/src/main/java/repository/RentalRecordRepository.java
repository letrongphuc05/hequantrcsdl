package CarRental.example.repository;

import CarRental.example.document.RentalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RentalRecordRepository extends JpaRepository<RentalRecord, String> {
    List<RentalRecord> findByUsername(String username);
    List<RentalRecord> findByStationIdAndStatusIn(String stationId, List<String> statuses);
    List<RentalRecord> findByVehicleIdAndStatus(String vehicleId, String status);
}