package CarRental.example.service.sepay;

import CarRental.example.document.RentalRecord;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SepayWebhookHandler {
    private final RentalRecordRepository rentalRepo;
    private final VehicleService vehicleService;
    private static final Logger log = LoggerFactory.getLogger(SepayWebhookHandler.class);

    public SepayWebhookHandler(RentalRecordRepository rentalRepo, VehicleService vehicleService) {
        this.rentalRepo = rentalRepo;
        this.vehicleService = vehicleService;
    }

    @Transactional
    public ResponseEntity<String> processWebhook(SepayWebhookData data) {
        String raw = (data.getDescription() != null && !data.getDescription().isBlank())
                ? data.getDescription() : data.getContent();

        if (raw == null || raw.isBlank()) return ResponseEntity.ok("NO_DESCRIPTION");

        Matcher matcher = Pattern.compile("rental(\\d+)").matcher(raw.toLowerCase());
        if (!matcher.find()) return ResponseEntity.ok("NO_RENTAL_ID");

        String rentalId = matcher.group(0);
        RentalRecord record = rentalRepo.findById(rentalId).orElse(null);
        if (record == null) return ResponseEntity.ok("RENTAL_NOT_FOUND");

        double amount = 0;
        try { amount = Double.parseDouble(data.getAmount()); } catch (Exception e) {}

        record.setDepositPaidAmount(Optional.ofNullable(record.getDepositPaidAmount()).orElse(0.0) + amount);
        record.setDepositPaidAt(LocalDateTime.now());

        if (record.getDepositPaidAmount() >= record.getTotal()) {
            record.setPaymentStatus("PAID");
            record.setStatus("PAID");
            record.setPaidAt(LocalDateTime.now());
            rentalRepo.save(record);
            vehicleService.markRented(record.getVehicleId(), rentalId);
        } else {
            record.setPaymentStatus("DEPOSIT_PENDING");
            rentalRepo.save(record);
        }
        return ResponseEntity.ok("OK");
    }
}