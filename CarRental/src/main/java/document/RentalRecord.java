package CarRental.example.document;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_records")
@Data
@NoArgsConstructor
public class RentalRecord {
    @Id
    private String id; // Lưu ý: SePay dùng ID chuỗi như 'rental123'
    private String username;
    private String vehicleId;
    private String stationId;
    private LocalDateTime pickupDate;
    private LocalDateTime returnDate;
    private double total;
    private String status; // PAID, IN_PROGRESS, COMPLETED, CANCELLED
    private String paymentStatus;
    private double depositRequiredAmount;
    private double depositPaidAmount;

    @Lob
    @Column(name = "delivery_photo_data", columnDefinition = "LONGBLOB")
    private byte[] deliveryPhotoData;

    @Lob
    @Column(name = "signature_data", columnDefinition = "LONGBLOB")
    private byte[] signatureData;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime paidAt;
}