package CarRental.example.document;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "staff")
@PrimaryKeyJoinColumn(name = "user_id") // Liên kết ID với bảng users
@Setter
@Getter
public class Staff extends User {

    public Staff() {
        super();
    }

    public Staff(String stationId) {
        this.setStationId(stationId);
    }

    // Giữ hàm này để không bị lỗi ở các file Controller cũ
    public String getName() {
        return this.getFullName() != null ? this.getFullName() : "";
    }
}