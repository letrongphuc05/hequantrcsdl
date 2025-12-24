package CarRental.example.repository;

import CarRental.example.document.VehicleReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleReportRepository extends JpaRepository<VehicleReport, String> {
}