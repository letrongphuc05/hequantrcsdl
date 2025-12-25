package CarRental.example.controller;

import CarRental.example.document.User;
import CarRental.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional; // Import quan trọng

import java.security.Principal;
import java.util.Base64;
import java.util.Optional; // Cần thiết để xử lý Optional

@Controller
@RequestMapping("/ho-so")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        // SỬA LỖI: Dùng .orElse(null) để lấy User ra khỏi Optional
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", user);

        // Hiển thị ảnh nếu có
        if (user != null) {
            if (user.getLicenseData() != null && user.getLicenseData().length > 0) {
                String base64License = Base64.getEncoder().encodeToString(user.getLicenseData());
                model.addAttribute("licenseImage", base64License);
            }
            if (user.getIdCardData() != null && user.getIdCardData().length > 0) {
                String base64IdCard = Base64.getEncoder().encodeToString(user.getIdCardData());
                model.addAttribute("idCardImage", base64IdCard);
            }
        }

        return "user-hosocanhan";
    }

    @PostMapping("/upload-license")
    @Transactional // Cần thiết cho thao tác ghi vào MySQL
    public String uploadLicense(@RequestParam("licenseFile") MultipartFile file, Principal principal) {
        try {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null && !file.isEmpty()) {
                user.setLicenseData(file.getBytes());
                userRepository.save(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ho-so";
    }

    @PostMapping("/upload-idcard")
    @Transactional
    public String uploadIdCard(@RequestParam("idCardFile") MultipartFile file, Principal principal) {
        try {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null && !file.isEmpty()) {
                user.setIdCardData(file.getBytes());
                userRepository.save(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ho-so";
    }

    @PostMapping("/request-verification")
    @Transactional
    public String requestVerification(Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user != null) {
            if (user.getLicenseData() == null || user.getIdCardData() == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng upload đủ ảnh bằng lái và CCCD trước khi gửi yêu cầu!");
            } else {
                user.setVerificationRequested(true);
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu xác thực!");
            }
        }
        return "redirect:/ho-so";
    }
}