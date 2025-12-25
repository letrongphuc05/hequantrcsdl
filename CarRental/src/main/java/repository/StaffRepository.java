package CarRental.example.repository;

import CarRental.example.document.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {

    // Tìm kiếm nhân viên bằng username (Dùng cho đăng nhập Admin/Staff)
    Optional<Staff> findByUsername(String username);

    // Kiểm tra nhân viên tồn tại (Dùng cho quản trị)
    boolean existsByUsername(String username);
}