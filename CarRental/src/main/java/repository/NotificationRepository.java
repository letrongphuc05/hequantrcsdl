package CarRental.example.repository;

import CarRental.example.document.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    // Tìm tất cả thông báo của người dùng, sắp xếp mới nhất lên đầu
    List<Notification> findByUserIdOrderByCreatedDateDesc(String userId);

    // Tìm thông báo chưa đọc
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedDateDesc(String userId);

    // Đếm số thông báo chưa đọc để hiển thị badge trên giao diện
    long countByUserIdAndIsReadFalse(String userId);
}