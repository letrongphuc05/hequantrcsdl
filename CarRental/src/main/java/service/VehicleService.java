package CarRental.example.service;

import CarRental.example.document.Vehicle;
import CarRental.example.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepo;
    public VehicleService(VehicleRepository vehicleRepo) { this.vehicleRepo = vehicleRepo; }

    @Transactional
    public void markRented(String vehicleId, String rentalId) {
        vehicleRepo.findById(vehicleId).ifPresent(v -> {
            v.setBookingStatus("RENTED");
            v.setAvailable(false);
            v.setPendingRentalId(rentalId);
            vehicleRepo.save(v);
        });
    }

    @Transactional
    public void markDeposited(String vehicleId, String rentalId) {
        markRented(vehicleId, rentalId);
    }

    @Transactional
    public void releaseHold(String vehicleId, String rentalId) {
        vehicleRepo.findById(vehicleId).ifPresent(v -> {
            if (rentalId == null || rentalId.equals(v.getPendingRentalId())) {
                v.setBookingStatus("AVAILABLE");
                v.setAvailable(true);
                v.setPendingRentalId(null);
                vehicleRepo.save(v);
            }
        });
    }
}