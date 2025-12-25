package CarRental.example.controller;

import CarRental.example.document.CustomerSupport;
import CarRental.example.document.User;
import CarRental.example.repository.CustomerSupportRepository;
import CarRental.example.repository.UserRepository;
import CarRental.example.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/support")
public class CustomerSupportController {

    private final CustomerSupportRepository supportRepo;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public CustomerSupportController(CustomerSupportRepository supportRepo,
                                     NotificationService notificationService,
                                     UserRepository userRepository) {
        this.supportRepo = supportRepo;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createTicket(@RequestBody Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return ResponseEntity.status(401).build();

        CustomerSupport ticket = new CustomerSupport();
        ticket.setUsername(auth.getName());
        ticket.setTitle(body.get("title"));
        ticket.setContent(body.get("content")); // Đã khớp với Entity
        ticket.setStatus("PENDING");
        ticket.setCreatedAt(LocalDateTime.now());

        supportRepo.save(ticket);
        return ResponseEntity.ok("Gửi thành công");
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<CustomerSupport>> getMyHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return ResponseEntity.status(401).build();

        List<CustomerSupport> list = supportRepo.findByUsername(auth.getName());
        list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return ResponseEntity.ok(list);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<CustomerSupport>> getAllTickets() {
        List<CustomerSupport> list = supportRepo.findAll();
        list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return ResponseEntity.ok(list);
    }

    @PostMapping("/admin/reply/{id}")
    @Transactional
    public ResponseEntity<?> replyTicket(@PathVariable("id") String id, @RequestBody Map<String, String> body) {
        CustomerSupport ticket = supportRepo.findById(id).orElse(null);
        if (ticket == null) return ResponseEntity.notFound().build();

        ticket.setAdminReply(body.get("reply"));
        ticket.setStatus("RESOLVED");
        supportRepo.save(ticket);

        // SỬA LỖI: Xử lý Optional bằng orElse(null)
        User user = userRepository.findByUsername(ticket.getUsername()).orElse(null);
        if (user != null) {
            String msg = "Yêu cầu hỗ trợ \"" + ticket.getTitle() + "\" đã được phản hồi.";
            // Gọi Service với 4 tham số: userId, message, type, supportRequestId
            notificationService.createNotification(user.getId(), msg, "SUPPORT_REPLY", ticket.getId());
        }

        return ResponseEntity.ok("Đã phản hồi");
    }
}