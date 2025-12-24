package CarRental.example.document;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_reports")
@Data
@NoArgsConstructor
public class VehicleReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "vehicle_plate")
    private String vehiclePlate;

    @Column(name = "staff_id")
    private String staffId;

    @Column(name = "staff_name")
    private String staffName;

    @Column(name = "station_id")
    private String stationId;

    @Column(name = "issue")
    private String issue;

    @Column(name = "severity")
    private String severity;

    @Column(name = "reported_date")
    private LocalDateTime reportedDate = LocalDateTime.now();

    @Column(name = "status")
    private String status = "REPORTED";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public VehicleReport(String vehicleId, String vehiclePlate, String staffId, String staffName,
                         String stationId, String issue, String severity) {
        this.vehicleId = vehicleId;
        this.vehiclePlate = vehiclePlate;
        this.staffId = staffId;
        this.staffName = staffName;
        this.stationId = stationId;
        this.issue = issue;
        this.severity = severity;
        this.reportedDate = LocalDateTime.now();
        this.status = "REPORTED";
    }
}