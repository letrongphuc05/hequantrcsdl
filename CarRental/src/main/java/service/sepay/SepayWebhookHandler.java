package CarRental.example.service.sepay;

import CarRental.example.document.RentalRecord;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.service.VehicleService;
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

    public SepayWebhookHandler(RentalRecordRepository rentalRepo, VehicleService vehicleService) {
        this.rentalRepo = rentalRepo;
        this.vehicleService = vehicleService;
    }

    @Transactional
    public ResponseEntity<String> processWebhook(SepayWebhookData data) {
        String raw = (data.getDescription() != null && !data.getDescription().isBlank()) ? data.getDescription() : data.getContent();
        if (raw == null) return ResponseEntity.ok("EMPTY");

        Matcher m = Pattern.compile("rental(\\d+)").matcher(raw.toLowerCase());
        if (!m.find()) return ResponseEntity.ok("NO_ID");

        String rentalId = m.group(0);
        RentalRecord record = rentalRepo.findById(rentalId).orElse(null);
        if (record == null) return ResponseEntity.ok("NOT_FOUND");

        double amount = 0;
        try { amount = Double.parseDouble(data.getAmount()); } catch (Exception e) {}

        record.setDepositPaidAmount(Optional.ofNullable(record.getDepositPaidAmount()).orElse(0.0) + amount);
        if (record.getDepositPaidAmount() >= record.getTotal()) {
            record.setStatus("PAID");
            rentalRepo.save(record);
            vehicleService.markRented(record.getVehicleId(), rentalId);
        }
        return ResponseEntity.ok("OK");
    }
}