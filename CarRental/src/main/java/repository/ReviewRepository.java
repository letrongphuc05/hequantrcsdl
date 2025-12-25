package CarRental.example.repository;

import CarRental.example.document.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    // Thêm hàm này để ReviewService hết lỗi
    boolean existsByBookingId(String bookingId);
}