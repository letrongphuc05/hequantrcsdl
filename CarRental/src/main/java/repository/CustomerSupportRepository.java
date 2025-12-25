package CarRental.example.repository;

import CarRental.example.document.CustomerSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerSupportRepository extends JpaRepository<CustomerSupport, String> {
    // Phải có dòng này thì Controller mới gọi được hàm findByUsername
    List<CustomerSupport> findByUsername(String username);
}