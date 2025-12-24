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

    @Column(name = "user_id")
    private String userId;

    @Column(name = "message")
    private String message;

    @Column(name = "type")
    private String type;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "support_request_id")
    private String supportRequestId;

    public Notification(String userId, String message, String type, String supportRequestId) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.supportRequestId = supportRequestId;
        this.createdDate = LocalDateTime.now();
        this.isRead = false;
    }
}