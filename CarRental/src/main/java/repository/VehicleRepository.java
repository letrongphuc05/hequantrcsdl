package CarRental.example.repository;

import CarRental.example.document.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    @Modifying
    @Transactional
    @Query("UPDATE Vehicle v SET v.bookingStatus = :status WHERE v.id = :id")
    void updateBookingStatus(@Param("id") String id, @Param("status") String status);

    List<Vehicle> findByStationId(String stationId);
    List<Vehicle> findByStationIdAndAvailable(String stationId, boolean available);
    long countByStationIdAndBookingStatus(String stationId, String bookingStatus);
}