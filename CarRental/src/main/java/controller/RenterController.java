package CarRental.example.controller;

import CarRental.example.document.User;
import CarRental.example.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional; // Cần thiết để xử lý UserRepository mới

@RestController
@RequestMapping("/api/renter")
public class RenterController {

    private final UserRepository repo;

    public RenterController(UserRepository repo) {
        this.repo = repo;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    // --- SỬA LỖI: Sử dụng Optional để tránh lỗi Incompatible Types ---
    private ResponseEntity<User> resolveCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).build();
        }

        // Dùng .orElse(null) để chuyển từ Optional<User> sang User
        User user = repo.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile")
    public User profile() {
        String username = getCurrentUsername();
        if (username == null) return null;

        // Sửa lỗi gạch đỏ ở đây
        User user = repo.findByUsername(username).orElse(null);
        if (user != null) {
            user.setPassword(null); // Không gửi mật khẩu về client
        }
        return user;
    }

    @GetMapping("/verification-status")
    public ResponseEntity<?> getVerificationStatus() {
        ResponseEntity<User> resolved = resolveCurrentUser();
        if (!resolved.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(resolved.getStatusCode()).body("Unauthorized");
        }

        User user = resolved.getBody();
        if (user == null) return ResponseEntity.notFound().build();

        Map<String, Object> status = new HashMap<>();
        status.put("licenseUploaded", user.getLicenseData() != null && user.getLicenseData().length > 0);
        status.put("idCardUploaded", user.getIdCardData() != null && user.getIdCardData().length > 0);
        status.put("verificationRequested", user.isVerificationRequested());
        status.put("verified", user.isVerified());

        return ResponseEntity.ok(status);
    }

    @PostMapping("/upload-license")
    @Transactional // Quan trọng để lưu vào MySQL
    public ResponseEntity<?> uploadLicense(@RequestParam("file") MultipartFile file) {
        ResponseEntity<User> resolved = resolveCurrentUser();
        if (!resolved.getStatusCode().is2xxSuccessful()) return resolved;

        User user = resolved.getBody();
        try {
            user.setLicenseData(file.getBytes());
            repo.save(user);
            return ResponseEntity.ok("Cập nhật bằng lái thành công");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi upload: " + e.getMessage());
        }
    }

    @PostMapping("/upload-idcard")
    @Transactional
    public ResponseEntity<?> uploadIdCard(@RequestParam("file") MultipartFile file) {
        ResponseEntity<User> resolved = resolveCurrentUser();
        if (!resolved.getStatusCode().is2xxSuccessful()) return resolved;

        User user = resolved.getBody();
        try {
            user.setIdCardData(file.getBytes());
            repo.save(user);
            return ResponseEntity.ok("Cập nhật CCCD thành công");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi upload: " + e.getMessage());
        }
    }

    @PostMapping("/request-verification")
    @Transactional
    public ResponseEntity<?> requestVerification() {
        ResponseEntity<User> resolved = resolveCurrentUser();
        if (!resolved.getStatusCode().is2xxSuccessful()) return resolved;

        User user = resolved.getBody();
        if (user.getLicenseData() == null || user.getIdCardData() == null) {
            return ResponseEntity.badRequest().body("Vui lòng upload đủ giấy tờ trước khi gửi yêu cầu.");
        }

        user.setVerificationRequested(true);
        repo.save(user);
        return ResponseEntity.ok("Đã gửi yêu cầu xác thực");
    }
}