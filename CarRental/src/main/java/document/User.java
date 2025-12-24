package CarRental.example.document;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED) // <--- DÒNG QUAN TRỌNG NHẤT ĐỂ STAFF KHÔNG LỖI
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "station_id")
    private String stationId;

    @Lob
    @Column(name = "license_data", columnDefinition = "LONGBLOB")
    private byte[] licenseData;

    @Lob
    @Column(name = "id_card_data", columnDefinition = "LONGBLOB")
    private byte[] idCardData;

    @Column(name = "verified")
    private boolean verified = false;

    @Column(name = "verification_requested")
    private boolean verificationRequested = false;

    @Column(name = "risk")
    private boolean risk = false;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();
}