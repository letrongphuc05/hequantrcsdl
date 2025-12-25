package CarRental.example.repository;

import CarRental.example.document.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Tìm kiếm khách hàng bằng username (Dùng cho đăng nhập và bảo mật)
    Optional<User> findByUsername(String username);

    // Kiểm tra username đã tồn tại chưa (Dùng cho đăng ký)
    boolean existsByUsername(String username);

    // Tìm kiếm bằng email
    Optional<User> findByEmail(Optional<String> email);
}