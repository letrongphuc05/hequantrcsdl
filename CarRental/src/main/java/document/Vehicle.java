package CarRental.example.document;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_station_id", columnList = "station_id"),
        @Index(name = "idx_booking_status", columnList = "booking_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "brand")
    private String brand;

    @Column(name = "type")
    private String type;

    @Column(name = "image")
    private String image;

    @Column(name = "price")
    private double price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "fuel")
    private String fuel;

    @Column(name = "engine")
    private String engine;

    @Column(name = "transmission")
    private String transmission;

    @Column(name = "seat")
    private int seat;

    @Column(name = "year")
    private int year;

    @Column(name = "color")
    private String color;

    @Column(name = "plate", unique = true)
    private String plate;

    @Column(name = "station_id")
    private String stationId;

    @Column(name = "booking_status")
    private String bookingStatus;

    // --- CÁC TRƯỜNG CẦN THIẾT CHO VEHICLE SERVICE ---
    @Column(name = "available")
    private boolean available = true;

    @Column(name = "pending_rental_id")
    private String pendingRentalId;
}