package CarRental.example.document;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String userId;
    private String message;
    private String type;
    private String supportRequestId;
    private boolean isRead = false;
    private LocalDateTime createdDate = LocalDateTime.now();

    // Constructor 4 tham số bắt buộc cho Service
    public Notification(String userId, String message, String type, String supportRequestId) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.supportRequestId = supportRequestId;
        this.createdDate = LocalDateTime.now();
        this.isRead = false;
    }
}