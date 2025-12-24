package CarRental.example.repository;

import CarRental.example.document.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {

    // Tìm nhân viên theo username (JPA tự động hiểu, không cần viết Query)
    Staff findByUsername(String username);
}