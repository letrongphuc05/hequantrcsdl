package CarRental.example.repository;

import CarRental.example.document.CustomerSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerSupportRepository extends JpaRepository<CustomerSupport, String> {

    // Thêm hàm này để Controller tìm được danh sách yêu cầu của người dùng
    List<CustomerSupport> findByUsername(String username);
}