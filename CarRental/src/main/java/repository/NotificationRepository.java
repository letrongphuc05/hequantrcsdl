package CarRental.example.repository;

import CarRental.example.document.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    // 1. Tìm tất cả thông báo của User, sắp xếp mới nhất lên đầu
    List<Notification> findByUserIdOrderByCreatedDateDesc(String userId);

    // 2. Tìm các thông báo CHƯA ĐỌC (isRead = false) của User, sắp xếp mới nhất lên đầu
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedDateDesc(String userId);

    // 3. Đếm số lượng thông báo chưa đọc
    long countByUserIdAndIsReadFalse(String userId);
}