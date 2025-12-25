package CarRental.example.service;

import CarRental.example.document.RentalRecord;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.repository.StationRepository;
import CarRental.example.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RentalRecordService {

    private final RentalRecordRepository repo;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;

    public RentalRecordService(RentalRecordRepository repo, VehicleRepository vehicleRepository, StationRepository stationRepository) {
        this.repo = repo;
        this.vehicleRepository = vehicleRepository;
        this.stationRepository = stationRepository;
    }

    // --- HÀM MỚI THÊM ĐỂ ADMIN KHÔNG LỖI ---
    public Map<String, Object> getGlobalStats() {
        List<RentalRecord> allRecords = repo.findAll();
        double totalRevenue = allRecords.stream()
                .filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()) || "RETURNED".equalsIgnoreCase(r.getStatus()))
                .mapToDouble(RentalRecord::getTotal)
                .sum();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTrips", allRecords.size());
        stats.put("totalRevenue", totalRevenue);
        stats.put("activeRentals", allRecords.stream().filter(r -> "IN_PROGRESS".equalsIgnoreCase(r.getStatus())).count());
        return stats;
    }

    public RentalRecord saveRecord(RentalRecord record) {
        if (record != null && record.getCreatedAt() == null) record.setCreatedAt(LocalDateTime.now());
        return repo.save(record);
    }

    public List<Map<String, Object>> getAllHistoryDetails() {
        List<RentalRecord> records = repo.findAll().stream()
                .filter(this::isVisibleInHistory)
                .sorted(Comparator.comparing(RentalRecord::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<Map<String, Object>> response = new ArrayList<>();
        for (RentalRecord record : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("record", record);
            item.put("displayStatus", resolveStatus(record).display);

            vehicleRepository.findById(record.getVehicleId()).ifPresent(v -> item.put("vehicle", v));
            stationRepository.findById(record.getStationId()).ifPresent(s -> item.put("station", s));
            response.add(item);
        }
        return response;
    }

    public List<String> getAiSuggestions() {
        List<String> suggestions = new ArrayList<>();
        long totalTrips = repo.count();
        if (totalTrips > 100) suggestions.add("<strong>Phân tích AI:</strong> Lượng khách tăng cao, nên bổ sung thêm xe điện.");
        else suggestions.add("<strong>Hệ thống:</strong> Dữ liệu ổn định.");
        return suggestions;
    }

    public Map<String, Object> calculateStats(String username) {
        List<RentalRecord> records = repo.findByUsername(username);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTrips", records.size());
        stats.put("totalSpent", records.stream().mapToDouble(RentalRecord::getTotal).sum());
        return stats;
    }

    public List<Map<String, Object>> getHistoryDetails(String username) {
        return getAllHistoryDetails().stream()
                .filter(m -> ((RentalRecord)m.get("record")).getUsername().equals(username))
                .toList();
    }

    private boolean isVisibleInHistory(RentalRecord record) {
        String s = Optional.ofNullable(record.getStatus()).orElse("").toUpperCase();
        return !s.equals("CANCELLED") && !s.equals("EXPIRED");
    }

    private StatusView resolveStatus(RentalRecord record) {
        String s = Optional.ofNullable(record.getStatus()).orElse("").toUpperCase();
        if (s.equals("RETURNED") || s.equals("COMPLETED")) return new StatusView("Đã trả xe", "returned");
        if (s.equals("IN_PROGRESS")) return new StatusView("Đang thuê", "active");
        return new StatusView("Đang chờ", "pending");
    }
    private record StatusView(String display, String filterKey) {}
}