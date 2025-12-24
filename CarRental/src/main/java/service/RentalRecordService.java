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

    public RentalRecord saveRecord(RentalRecord record) {
        ensureCreatedAt(record);
        return repo.save(record);
    }
    public List<RentalRecord> getHistoryByUsername(String username) { return repo.findByUsername(username); }
    public List<RentalRecord> getAll() { return repo.findAll(); }
    public RentalRecord getById(String id) { return repo.findById(id).orElse(null); }
    public void delete(String id) { repo.deleteById(id); }

    public List<Map<String, Object>> getHistoryDetails(String username) {
        List<RentalRecord> records = repo.findByUsername(username)
                .stream()
                .filter(this::isVisibleInHistory)
                .sorted(buildHistoryComparator())
                .toList();
        List<Map<String, Object>> response = new ArrayList<>();
        for (RentalRecord record : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            StatusView statusView = resolveStatus(record);
            item.put("record", record);
            item.put("displayStatus", statusView.display);
            item.put("filterStatus", statusView.filterKey);
            vehicleRepository.findById(record.getVehicleId()).ifPresent(vehicle -> {
                Map<String, Object> vehicleInfo = new LinkedHashMap<>();
                vehicleInfo.put("id", vehicle.getId());
                vehicleInfo.put("type", vehicle.getType());
                vehicleInfo.put("plate", vehicle.getPlate());
                vehicleInfo.put("brand", vehicle.getBrand());
                vehicleInfo.put("price", vehicle.getPrice());
                item.put("vehicle", vehicleInfo);
            });
            stationRepository.findById(record.getStationId()).ifPresent(station -> {
                Map<String, Object> stationInfo = new LinkedHashMap<>();
                stationInfo.put("id", station.getId());
                stationInfo.put("name", station.getName());
                stationInfo.put("address", station.getAddress());
                item.put("station", stationInfo);
            });
            response.add(item);
        }
        return response;
    }

    public List<Map<String, Object>> getAllHistoryDetails() {
        List<RentalRecord> records = repo.findAll()
                .stream()
                .filter(this::isVisibleInHistory)
                .sorted(buildHistoryComparator())
                .toList();
        List<Map<String, Object>> response = new ArrayList<>();
        for (RentalRecord record : records) {
            Map<String, Object> item = new LinkedHashMap<>();
            StatusView statusView = resolveStatus(record);
            item.put("record", record);
            item.put("displayStatus", statusView.display);
            item.put("filterStatus", statusView.filterKey);
            vehicleRepository.findById(record.getVehicleId()).ifPresent(vehicle -> {
                Map<String, Object> vehicleInfo = new LinkedHashMap<>();
                vehicleInfo.put("id", vehicle.getId());
                vehicleInfo.put("type", vehicle.getType());
                vehicleInfo.put("plate", vehicle.getPlate());
                vehicleInfo.put("brand", vehicle.getBrand());
                vehicleInfo.put("price", vehicle.getPrice());
                item.put("vehicle", vehicleInfo);
            });
            stationRepository.findById(record.getStationId()).ifPresent(station -> {
                Map<String, Object> stationInfo = new LinkedHashMap<>();
                stationInfo.put("id", station.getId());
                stationInfo.put("name", station.getName());
                stationInfo.put("address", station.getAddress());
                item.put("station", stationInfo);
            });
            response.add(item);
        }
        return response;
    }

    private long getSortTimestamp(RentalRecord record) {
        if (record == null) return 0;
        List<LocalDateTime> timestamps = new ArrayList<>();
        Optional.ofNullable(record.getEndTime()).ifPresent(timestamps::add);
        Optional.ofNullable(record.getStartTime()).ifPresent(timestamps::add);
        Optional.ofNullable(record.getCreatedAt()).ifPresent(timestamps::add);

        LocalDateTime newest = timestamps.stream()
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        return newest.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private Comparator<RentalRecord> buildHistoryComparator() {
        return Comparator.comparingLong(this::getSortTimestamp).reversed();
    }

    private void ensureCreatedAt(RentalRecord record) {
        if (record != null && record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
    }

    public Map<String, Object> calculateStats(String username) {
        List<RentalRecord> records = repo.findByUsername(username).stream().filter(this::isVisibleInHistory).toList();
        double totalSpent = records.stream().mapToDouble(RentalRecord::getTotal).sum();
        int totalTrips = records.size();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTrips", totalTrips);
        stats.put("totalSpent", totalSpent);
        return stats;
    }

    public RentalRecord signContract(String rentalId, String username) {
        RentalRecord record = repo.findById(rentalId).orElse(null);
        if (record == null || !Objects.equals(record.getUsername(), username)) return null;
        record.setContractSigned(true);
        record.setStatus("CONTRACT_SIGNED");
        return repo.save(record);
    }

    public RentalRecord checkIn(String rentalId, String username, String notes) {
        RentalRecord record = repo.findById(rentalId).orElse(null);
        if (record == null || !Objects.equals(record.getUsername(), username)) return null;
        record.setStartTime(LocalDateTime.now());
        record.setCheckinNotes(notes);
        record.setStatus("IN_PROGRESS");
        return repo.save(record);
    }

    public RentalRecord requestReturn(String rentalId, String username, String notes) {
        RentalRecord record = repo.findById(rentalId).orElse(null);
        if (record == null || !Objects.equals(record.getUsername(), username)) return null;
        record.setReturnNotes(notes);
        record.setEndTime(LocalDateTime.now());
        record.setStatus("WAITING_INSPECTION");
        return repo.save(record);
    }

    public List<String> getAiSuggestions() {
        List<String> suggestions = new ArrayList<>();
        List<RentalRecord> allRecords = repo.findAll();
        Map<String, Long> tripsByStation = allRecords.stream()
                .filter(r -> !"CANCELLED".equals(r.getStatus()))
                .collect(Collectors.groupingBy(RentalRecord::getStationId, Collectors.counting()));

        tripsByStation.forEach((stationId, count) -> {
            String stationName = stationRepository.findById(stationId).map(s -> s.getName()).orElse(stationId);
            long currentVehicles = vehicleRepository.findByStationIdAndBookingStatusNot(stationId, "MAINTENANCE").size();
            if (currentVehicles > 0 && (count / currentVehicles) >= 5) {
                suggestions.add("<strong>Nhu cầu cao tại " + stationName + ":</strong> AI khuyến nghị bổ sung thêm xe.");
            }
        });
        if (suggestions.isEmpty()) suggestions.add("<strong>Hệ thống:</strong> Dữ liệu ổn định.");
        return suggestions;
    }

    private boolean isVisibleInHistory(RentalRecord record) {
        if (record == null) return false;
        String status = Optional.ofNullable(record.getStatus()).orElse("").toUpperCase();
        return !status.equals("CANCELLED") && !status.equals("EXPIRED");
    }

    private StatusView resolveStatus(RentalRecord record) {
        String status = Optional.ofNullable(record.getStatus()).orElse("").toUpperCase();
        if (status.equals("RETURNED") || status.equals("COMPLETED")) return new StatusView("Đã trả xe", "returned");
        if (status.equals("IN_PROGRESS")) return new StatusView("Đang thuê", "active");
        return new StatusView("Đang chờ", "rented");
    }
    private record StatusView(String display, String filterKey) {}
}