package CarRental.example.document;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_support")
@Data
@NoArgsConstructor
public class CustomerSupport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String username;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content; // Dùng 'content' để khớp với Controller
    private String status = "PENDING";
    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;
    private LocalDateTime createdAt = LocalDateTime.now();
}