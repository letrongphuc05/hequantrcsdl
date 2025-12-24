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

import java.security.Principal;
import java.util.Base64;

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

        User user = userRepository.findByUsername(principal.getName());
        model.addAttribute("user", user);

        // Hiển thị ảnh nếu có
        if (user.getLicenseData() != null && user.getLicenseData().length > 0) {
            String base64License = Base64.getEncoder().encodeToString(user.getLicenseData());
            model.addAttribute("licenseImage", base64License);
        }
        if (user.getIdCardData() != null && user.getIdCardData().length > 0) {
            String base64IdCard = Base64.getEncoder().encodeToString(user.getIdCardData());
            model.addAttribute("idCardImage", base64IdCard);
        }

        return "user-hosocanhan";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User updatedUser, RedirectAttributes redirectAttributes, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName());
        if (currentUser != null) {
            currentUser.setFullName(updatedUser.getFullName());
            // Cập nhật các trường khác nếu cần
            userRepository.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công!");
        }
        return "redirect:/ho-so";
    }

    @PostMapping("/doi-mat-khau")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        User user = userRepository.findByUsername(principal.getName());

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
            return "redirect:/ho-so";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/ho-so";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        return "redirect:/ho-so";
    }

    @PostMapping("/upload-license")
    public String uploadLicense(@RequestParam("licenseFile") MultipartFile file, Principal principal) {
        try {
            User user = userRepository.findByUsername(principal.getName());
            if (!file.isEmpty()) {
                // SỬA: Lưu trực tiếp byte[] vào MySQL
                user.setLicenseData(file.getBytes());
                userRepository.save(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ho-so";
    }

    @PostMapping("/upload-idcard")
    public String uploadIdCard(@RequestParam("idCardFile") MultipartFile file, Principal principal) {
        try {
            User user = userRepository.findByUsername(principal.getName());
            if (!file.isEmpty()) {
                // SỬA: Lưu trực tiếp byte[] vào MySQL
                user.setIdCardData(file.getBytes());
                userRepository.save(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/ho-so";
    }

    @PostMapping("/request-verification")
    public String requestVerification(Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(principal.getName());
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