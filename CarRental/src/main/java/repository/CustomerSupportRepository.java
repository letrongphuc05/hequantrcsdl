package CarRental.example.repository;

import CarRental.example.document.CustomerSupport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSupportRepository extends JpaRepository<CustomerSupport, String> {
}