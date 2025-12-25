package CarRental.example.document;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "plate", unique = true)
    private String plate;

    @Column(name = "station_id")
    private String stationId;

    @Column(name = "booking_status")
    private String bookingStatus;

    @Column(name = "available")
    private boolean available = true;

    // --- CÁC TRƯỜNG CẦN CẬP NHẬT ĐỂ HẾT LỖI ---
    @Column(name = "pending_rental_id")
    private String pendingRentalId;

    @Column(name = "battery")
    private int battery = 100;

    @Column(name = "issue")
    private String issue;

    @Column(name = "issue_severity")
    private String issueSeverity;
}