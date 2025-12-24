package CarRental.example.controller;

import CarRental.example.document.User;
import CarRental.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff")
public class StaffVerifyController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/verification")
    public String showVerificationPage(Model model) {
        // Lấy danh sách user đang chờ xác thực
        // (Lọc thủ công bằng Java Stream vì JPA findAll lấy hết)
        List<User> pendingUsers = userRepository.findAll().stream()
                .filter(User::isVerificationRequested)
                .filter(u -> !u.isVerified())
                .collect(Collectors.toList());

        model.addAttribute("users", pendingUsers);
        return "staff-verification";
    }

    @GetMapping("/verification/image/{userId}/{type}")
    @ResponseBody
    public String getUserImage(@PathVariable String userId, @PathVariable String type) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "";

        byte[] imageData = null; // Sửa từ Binary thành byte[]

        if ("license".equals(type)) {
            imageData = user.getLicenseData();
        } else if ("idcard".equals(type)) {
            imageData = user.getIdCardData();
        }

        // Chuyển byte[] sang Base64 String để hiện lên HTML
        if (imageData != null && imageData.length > 0) {
            return Base64.getEncoder().encodeToString(imageData);
        }
        return "";
    }

    @PostMapping("/verification/approve")
    public String approveUser(@RequestParam String userId, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setVerified(true);
            user.setVerificationRequested(false);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Đã xác thực thành công cho " + user.getUsername());
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
        }
        return "redirect:/staff/verification";
    }

    @PostMapping("/verification/reject")
    public String rejectUser(@RequestParam String userId, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setVerificationRequested(false);
            // user.setLicenseData(null); // Có thể xóa ảnh nếu muốn
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Đã từ chối xác thực cho " + user.getUsername());
        }
        return "redirect:/staff/verification";
    }
}