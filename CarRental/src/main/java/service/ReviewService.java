package CarRental.example.service;

import CarRental.example.document.RentalRecord;
import CarRental.example.document.Review;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReviewService {
    private static final double DECIMAL_PLACES_MULTIPLIER = 10.0;
    private final ReviewRepository reviewRepository;
    private final RentalRecordRepository rentalRecordRepository;

    public ReviewService(ReviewRepository reviewRepository, RentalRecordRepository rentalRecordRepository) {
        this.reviewRepository = reviewRepository;
        this.rentalRecordRepository = rentalRecordRepository;
    }

    @Transactional
    public Review createReview(String bookingId, String userId, String carId, String staffId,
                               Integer carRating, Integer staffRating, String comment) {
        RentalRecord rental = rentalRecordRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking không tồn tại"));

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new IllegalArgumentException("Chuyến thuê này đã được đánh giá");
        }

        Review review = new Review();
        review.setBookingId(bookingId);
        review.setUserId(userId);
        review.setCarId(carId);
        review.setStaffId(staffId);
        review.setCarRating(carRating);
        review.setStaffRating(staffRating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    public Map<String, Object> getReviewStats() {
        List<Review> allReviews = reviewRepository.findAll();
        Map<String, Object> stats = new HashMap<>();

        if (allReviews.isEmpty()) {
            stats.put("totalReviews", 0);
            stats.put("avgCarRating", 0.0);
            return stats;
        }

        double totalCarRating = allReviews.stream()
                .filter(r -> r.getCarRating() != null)
                .mapToInt(Review::getCarRating).sum();

        stats.put("totalReviews", allReviews.size());
        stats.put("avgCarRating", Math.round((totalCarRating / allReviews.size()) * DECIMAL_PLACES_MULTIPLIER) / DECIMAL_PLACES_MULTIPLIER);
        return stats;
    }
}