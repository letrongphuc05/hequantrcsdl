package CarRental.example.document;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_records")
@Data
@NoArgsConstructor
public class RentalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "username")
    private String username;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "station_id")
    private String stationId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "rental_days")
    private int rentalDays;

    @Column(name = "distance_km")
    private double distanceKm;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "total")
    private double total;

    @Column(name = "damage_fee")
    private double damageFee;

    @Column(name = "deposit_required_amount")
    private Double depositRequiredAmount;

    @Column(name = "deposit_paid_amount")
    private Double depositPaidAmount;

    @Column(name = "deposit_paid_at")
    private LocalDateTime depositPaidAt;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "status")
    private String status;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "contract_signed")
    private boolean contractSigned;

    @Column(name = "checkin_notes")
    private String checkinNotes;

    @Column(name = "checkin_latitude")
    private Double checkinLatitude;

    @Column(name = "checkin_longitude")
    private Double checkinLongitude;

    @Column(name = "return_notes")
    private String returnNotes;

    @Column(name = "return_latitude")
    private Double returnLatitude;

    @Column(name = "return_longitude")
    private Double returnLongitude;

    @Lob
    @Column(name = "checkin_photo_data", columnDefinition = "LONGBLOB")
    private byte[] checkinPhotoData;

    @Lob
    @Column(name = "return_photo_data", columnDefinition = "LONGBLOB")
    private byte[] returnPhotoData;

    @Column(name = "additional_fee_amount")
    private Double additionalFeeAmount;

    @Column(name = "additional_fee_note")
    private String additionalFeeNote;

    @Column(name = "additional_fee_paid_amount")
    private Double additionalFeePaidAmount;

    @Column(name = "additional_fee_paid_at")
    private LocalDateTime additionalFeePaidAt;

    @Column(name = "wallet_reference")
    private String walletReference;

    @Lob
    @Column(name = "delivery_photo_data", columnDefinition = "LONGBLOB")
    private byte[] deliveryPhotoData;

    @Lob
    @Column(name = "receive_photo_data", columnDefinition = "LONGBLOB")
    private byte[] receivePhotoData;

    @Lob
    @Column(name = "signature_data", columnDefinition = "LONGBLOB")
    private byte[] signatureData;

    @Column(name = "delivery_staff_id")
    private String deliveryStaffId;

    @Column(name = "return_staff_id")
    private String returnStaffId;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "feedback")
    private String feedback;

    public RentalRecord(String userId, String vehicleId, String stationId, double total) {
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.stationId = stationId;
        this.total = total;
        this.startTime = LocalDateTime.now();
        this.status = "PENDING";
        this.paymentStatus = "PENDING";
        this.damageFee = 0;
        this.contractSigned = false;
    }
}