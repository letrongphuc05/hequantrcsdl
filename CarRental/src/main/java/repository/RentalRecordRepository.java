package CarRental.example.repository;

import CarRental.example.document.RentalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RentalRecordRepository extends JpaRepository<RentalRecord, String> {

    List<RentalRecord> findByUsername(String username);

    @Query("SELECT r FROM RentalRecord r WHERE r.holdExpiresAt < :now " +
            "AND r.holdExpiresAt IS NOT NULL " +
            "AND r.checkinTime IS NULL " +
            "AND r.status NOT IN ('CANCELLED', 'EXPIRED', 'COMPLETED', 'RETURNED')")
    List<RentalRecord> findExpiredRentalsNotCheckedIn(@Param("now") LocalDateTime now);
}