package CarRental.example.service;

import CarRental.example.document.Vehicle;
import CarRental.example.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepo;

    public VehicleService(VehicleRepository vehicleRepo) {
        this.vehicleRepo = vehicleRepo;
    }

    @Transactional
    public void updateAvailable(String id, boolean available) {
        Vehicle v = vehicleRepo.findById(id).orElse(null);
        if (v != null) {
            v.setAvailable(available);
            vehicleRepo.save(v);
        }
    }

    @Transactional
    public boolean markPendingPayment(String vehicleId, String rentalId) {
        return markPendingPaymentInternal(vehicleId, rentalId, true);
    }

    @Transactional
    public boolean markPendingPaymentHidden(String vehicleId, String rentalId) {
        return markPendingPaymentInternal(vehicleId, rentalId, false);
    }

    private boolean markPendingPaymentInternal(String vehicleId, String rentalId, boolean showAsAvailable) {
        Vehicle v = vehicleRepo.findById(vehicleId).orElse(null);
        if (v == null) return false;

        String status = v.getBookingStatus() == null ? "AVAILABLE" : v.getBookingStatus();
        if ("RENTED".equalsIgnoreCase(status)) return false;

        // Kiểm tra xem xe có đang bị giữ bởi đơn thuê khác không
        if ("PENDING_PAYMENT".equalsIgnoreCase(status) && v.getPendingRentalId() != null
                && !v.getPendingRentalId().equals(rentalId)) {
            return false;
        }

        v.setBookingStatus("PENDING_PAYMENT");
        v.setPendingRentalId(rentalId);
        v.setAvailable(showAsAvailable);
        vehicleRepo.save(v);
        return true;
    }

    @Transactional
    public void markRented(String vehicleId, String rentalId) {
        Vehicle v = vehicleRepo.findById(vehicleId).orElse(null);
        if (v == null) return;

        if (v.getPendingRentalId() != null && !v.getPendingRentalId().equals(rentalId)
                && "PENDING_PAYMENT".equalsIgnoreCase(v.getBookingStatus())) {
            return;
        }

        v.setBookingStatus("RENTED");
        v.setPendingRentalId(rentalId);
        v.setAvailable(false);
        vehicleRepo.save(v);
    }

    @Transactional
    public void releaseHold(String vehicleId, String rentalId) {
        Vehicle v = vehicleRepo.findById(vehicleId).orElse(null);
        if (v == null) return;

        if (rentalId == null || v.getPendingRentalId() == null || v.getPendingRentalId().equals(rentalId)) {
            v.setBookingStatus("AVAILABLE");
            v.setPendingRentalId(null);
            v.setAvailable(true);
            vehicleRepo.save(v);
        }
    }
}