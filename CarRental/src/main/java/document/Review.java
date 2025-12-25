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
    private String bookingId;
    private String userId;
    private String carId;
    private String staffId;
    private Integer carRating;
    private Integer staffRating;
    @Column(columnDefinition = "TEXT")
    private String comment;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}