package CarRental.example.controller;

import CarRental.example.document.Review;
import CarRental.example.document.Staff;
import CarRental.example.document.User;
import CarRental.example.document.Vehicle;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.repository.StaffRepository;
import CarRental.example.repository.UserRepository;
import CarRental.example.repository.VehicleRepository;
import CarRental.example.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;
    private final RentalRecordRepository rentalRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final StaffRepository staffRepository;

    public ReviewController(ReviewService reviewService,
                            UserRepository userRepository,
                            RentalRecordRepository rentalRecordRepository,
                            VehicleRepository vehicleRepository,
                            StaffRepository staffRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
        this.rentalRecordRepository = rentalRecordRepository;
        this.vehicleRepository = vehicleRepository;
        this.staffRepository = staffRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody Map<String, Object> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return ResponseEntity.status(401).build();

        User user = userRepository.findByUsername(auth.getName());
        if (user == null) return ResponseEntity.status(404).body("User không tồn tại");

        try {
            String bookingId = (String) body.get("bookingId");
            String carId = (String) body.get("carId");
            String staffId = (String) body.get("staffId");

            Integer carRating = null;
            Integer staffRating = null;

            if (body.get("carRating") instanceof Number)
                carRating = ((Number) body.get("carRating")).intValue();

            if (body.get("staffRating") instanceof Number)
                staffRating = ((Number) body.get("staffRating")).intValue();

            String comment = (String) body.get("comment");

            Review review = reviewService.createReview(bookingId, user.getId(), carId, staffId, carRating, staffRating, comment);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/check/{bookingId}")
    public ResponseEntity<Map<String, Boolean>> checkReviewStatus(@PathVariable("bookingId") String bookingId) {
        boolean reviewed = reviewService.isBookingReviewed(bookingId);
        return ResponseEntity.ok(Collections.singletonMap("reviewed", reviewed));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getReviewByBooking(@PathVariable("bookingId") String bookingId) {
        return reviewService.getReviewByBookingId(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Map<String, Object>>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Review review : reviews) {
            Map<String, Object> reviewData = new LinkedHashMap<>();
            reviewData.put("id", review.getId());
            reviewData.put("bookingId", review.getBookingId());
            reviewData.put("carRating", review.getCarRating());
            reviewData.put("staffRating", review.getStaffRating());
            reviewData.put("comment", review.getComment());
            reviewData.put("reviewDate", review.getReviewDate());

            User user = userRepository.findById(review.getUserId()).orElse(null);
            reviewData.put("customerName", user != null ? user.getUsername() : "Unknown");

            Vehicle vehicle = vehicleRepository.findById(review.getCarId()).orElse(null);
            reviewData.put("vehicleName", vehicle != null ? (vehicle.getBrand() + " " + vehicle.getPlate()) : "Unknown");

            String staffName = "N/A";
            if (review.getStaffId() != null && !review.getStaffId().isEmpty()) {
                Staff staff = staffRepository.findById(review.getStaffId()).orElse(null);
                if (staff != null) {
                    staffName = staff.getFullName() != null ? staff.getFullName() : staff.getUsername();
                } else {
                    User staffUser = userRepository.findById(review.getStaffId()).orElse(null);
                    if (staffUser != null) staffName = staffUser.getUsername();
                }
            }
            reviewData.put("staffName", staffName);

            result.add(reviewData);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getReviewStats() {
        Map<String, Object> stats = reviewService.getReviewStats();
        return ResponseEntity.ok(stats);
    }
}