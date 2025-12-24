package CarRental.example.document;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "car_id")
    private String carId;

    @Column(name = "staff_id")
    private String staffId;

    @Column(name = "car_rating")
    private Integer carRating;

    @Column(name = "staff_rating")
    private Integer staffRating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "review_date")
    private LocalDateTime reviewDate = LocalDateTime.now();
}