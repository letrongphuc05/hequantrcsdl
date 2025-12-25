package CarRental.example.controller;

import CarRental.example.document.*;
import CarRental.example.repository.*;
import CarRental.example.service.RentalRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final VehicleRepository vehicleRepository;
    private final RentalRecordService rentalRecordService;

    public AdminController(UserRepository userRepository, StaffRepository staffRepository,
                           VehicleRepository vehicleRepository, RentalRecordService rentalRecordService) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
        this.vehicleRepository = vehicleRepository;
        this.rentalRecordService = rentalRecordService;
    }

    @GetMapping("/customers")
    public String showCustomers(Model model) {
        model.addAttribute("customers", userRepository.findAll());
        return "admin-customers";
    }

    @GetMapping("/customers/view/{id}")
    public String viewCustomer(@PathVariable String id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return "redirect:/admin/customers";
        model.addAttribute("customer", user);
        model.addAttribute("stats", rentalRecordService.calculateStats(user.getUsername()));
        model.addAttribute("history", rentalRecordService.getHistoryDetails(user.getUsername()));
        return "admin-customer-detail";
    }

    @PostMapping("/customers/toggle-risk/{id}")
    @ResponseBody
    public ResponseEntity<String> toggleRisk(@PathVariable String id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setRisk(!u.isRisk());
            userRepository.save(u);
        });
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/reports/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReportData() {
        Map<String, Object> stats = rentalRecordService.getGlobalStats();
        stats.put("aiSuggestions", rentalRecordService.getAiSuggestions());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/history")
    public String showHistory(Model model) {
        model.addAttribute("history", rentalRecordService.getAllHistoryDetails());
        return "admin-history";
    }
}