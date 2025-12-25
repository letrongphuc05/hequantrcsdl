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

    private String name;
    private String brand;
    private String type;
    private String image;
    private double price;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String fuel;
    private String engine;
    private String transmission;
    private int seat;
    private int year;
    private String color;

    @Column(unique = true)
    private String plate;

    @Column(name = "station_id")
    private String stationId;

    @Column(name = "booking_status")
    private String bookingStatus;

    private boolean available = true;

    // --- CÁC TRƯỜNG CẦN THIẾT ĐỂ HẾT LỖI ---
    private int battery = 100;

    @Column(name = "pending_rental_id")
    private String pendingRentalId;

    private String issue;

    @Column(name = "issue_severity")
    private String issueSeverity;
}