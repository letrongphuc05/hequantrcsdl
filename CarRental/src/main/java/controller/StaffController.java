package CarRental.example.controller;

import CarRental.example.document.Staff;
import CarRental.example.document.Station;
import CarRental.example.repository.StaffRepository;
import CarRental.example.repository.StationRepository;
import CarRental.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        // Tìm Staff theo username
        Staff staff = staffRepository.findByUsername(principal.getName());

        if (staff != null) {
            model.addAttribute("staff", staff);

            // Tìm thông tin trạm của nhân viên đó
            if (staff.getStationId() != null) {
                Station station = stationRepository.findById(staff.getStationId()).orElse(null);
                model.addAttribute("station", station);
            }
        }
        return "staff-deliver"; // Hoặc trả về trang dashboard chính của bạn
    }
}