package CarRental.example.service;

import CarRental.example.document.RentalRecord;
import CarRental.example.document.Vehicle;
import CarRental.example.document.Station;
import CarRental.example.repository.RentalRecordRepository;
import CarRental.example.repository.StationRepository;
import CarRental.example.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors; // Bắt buộc phải có import này

@Service
public class RentalRecordService {

    private final RentalRecordRepository repo;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private static final double DECIMAL_PLACES_MULTIPLIER = 10.0;

    public RentalRecordService(RentalRecordRepository repo,
                               VehicleRepository vehicleRepository,
                               StationRepository stationRepository) {
        this.repo = repo;
        this.vehicleRepository = vehicleRepository;
        this.stationRepository = stationRepository;
    }

    // 1. Lấy thống kê tổng quan cho Admin
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

    // 2. Lấy toàn bộ lịch sử kèm thông tin xe và trạm (Dùng cho Admin)
    public List<Map<String, Object>> getAllHistoryDetails() {
        return repo.findAll().stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("record", record);
                    map.put("vehicle", vehicleRepository.findById(record.getVehicleId()).orElse(null));
                    map.put("station", stationRepository.findById(record.getStationId()).orElse(null));
                    map.put("statusView", resolveStatus(record));
                    return map;
                })
                .collect(Collectors.toList());
    }

    // 3. Lấy lịch sử riêng của từng khách hàng
    public List<Map<String, Object>> getHistoryDetails(String username) {
        return getAllHistoryDetails().stream()
                .filter(m -> {
                    RentalRecord r = (RentalRecord) m.get("record");
                    return r != null && username.equals(r.getUsername()) && isVisibleInHistory(r);
                })
                .collect(Collectors.toList());
    }

    // 4. Logic phân tích gợi ý (AI đơn giản)
    public List<String> getAIAnalysis() {
        List<RentalRecord> records = repo.findAll();
        List<String> suggestions = new ArrayList<>();
        if (records.size() > 10) {
            suggestions.add("<strong>Phân tích AI:</strong> Lượng khách tăng cao, nên bổ sung thêm xe điện.");
        } else {
            suggestions.add("<strong>Hệ thống:</strong> Dữ liệu ổn định.");
        }
        return suggestions;
    }

    private boolean isVisibleInHistory(RentalRecord record) {
        String s = Optional.ofNullable(record.getStatus()).orElse("").toUpperCase();
        return !s.equals("CANCELLED") && !s.equals("EXPIRED");
    }

    // --- SỬA LỖI: Định nghĩa lớp StatusView để không còn gạch đỏ ---
    private StatusView resolveStatus(RentalRecord record) {
        String s = Optional.ofNullable(record.getStatus()).orElse("").toUpperCase();
        if (s.equals("RETURNED") || s.equals("COMPLETED")) return new StatusView("Đã trả xe", "returned");
        if (s.equals("IN_PROGRESS")) return new StatusView("Đang thuê", "active");
        if (s.equals("PAID")) return new StatusView("Đã thanh toán", "paid");
        return new StatusView("Đang chờ...", "pending");
    }

    // Lớp nội bộ để xử lý hiển thị trạng thái
    public static class StatusView {
        public String text;
        public String cssClass;
        public StatusView(String text, String cssClass) {
            this.text = text;
            this.cssClass = cssClass;
        }
    }
}