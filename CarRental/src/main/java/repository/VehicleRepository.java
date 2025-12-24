package CarRental.example.repository;

import CarRental.example.document.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    List<Vehicle> findByStationId(String stationId);

    List<Vehicle> findByBookingStatus(String bookingStatus);

    List<Vehicle> findByStationIdAndBookingStatus(String stationId, String bookingStatus);

    // Cần hàm này để AI Suggestions trong RentalRecordService hoạt động
    List<Vehicle> findByStationIdAndBookingStatusNot(String stationId, String bookingStatus);

    // FIX LỖI: Hàm đếm số lượng xe theo trạng thái tại trạm
    long countByStationIdAndBookingStatus(String stationId, String bookingStatus);

    // Cập nhật trạng thái xe trực tiếp (Dùng trong RentalController)
    @Modifying
    @Transactional
    @Query("UPDATE Vehicle v SET v.bookingStatus = :status WHERE v.id = :id")
    void updateBookingStatus(String id, String status);
}