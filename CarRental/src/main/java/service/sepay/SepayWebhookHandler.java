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
        log.info("Webhook SePay nhận được: {}", data);

        String raw = (data.getDescription() != null && !data.getDescription().isBlank())
                ? data.getDescription() : data.getContent();

        if (raw == null || raw.isBlank()) return ResponseEntity.ok("NO_DESCRIPTION");

        String lower = raw.toLowerCase();
        String rentalId = null;

        Matcher matcher = Pattern.compile("rental(\\d+)").matcher(lower);
        if (matcher.find()) {
            rentalId = matcher.group(0);
        }

        if (rentalId == null) return ResponseEntity.ok("NO_RENTAL_ID");

        RentalRecord record = rentalRepo.findById(rentalId).orElse(null);
        if (record == null) return ResponseEntity.ok("RENTAL_NOT_FOUND");

        double incomingAmount = 0;
        try {
            incomingAmount = Double.parseDouble(data.getAmount());
        } catch (Exception e) {
            try { incomingAmount = Double.parseDouble(data.getSub_amount()); } catch (Exception ignored) {}
        }

        record.setDepositPaidAmount(Optional.ofNullable(record.getDepositPaidAmount()).orElse(0.0) + incomingAmount);
        record.setDepositPaidAt(LocalDateTime.now());
        record.setWalletReference(data.getTranId());

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