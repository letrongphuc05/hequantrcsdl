package CarRental.example.repository;

import CarRental.example.document.RentalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RentalRecordRepository extends JpaRepository<RentalRecord, String> {

    List<RentalRecord> findByUsername(String username);

    List<RentalRecord> findByStatusAndHoldExpiresAtBefore(String status, LocalDateTime time);

    /**
     * Tìm các giao dịch đã hết hạn giữ chỗ (hold expired) mà chưa check-in.
     * Đã chuyển từ MongoDB Query sang JPQL cho MySQL.
     */
    @Query("SELECT r FROM RentalRecord r WHERE r.holdExpiresAt < :now " +
            "AND r.holdExpiresAt IS NOT NULL " +
            "AND r.checkinPhotoData IS NULL " +
            "AND r.checkinTime IS NULL " +
            "AND r.status NOT IN ('CANCELLED', 'EXPIRED', 'COMPLETED', 'RETURNED', 'IN_PROGRESS') " +
            "AND r.paymentStatus NOT IN ('PAID')")
    List<RentalRecord> findExpiredRentalsNotCheckedIn(LocalDateTime now);
}