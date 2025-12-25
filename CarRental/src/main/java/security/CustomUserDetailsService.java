package CarRental.example.security;

import CarRental.example.document.User;
import CarRental.example.repository.UserRepository;
import CarRental.example.repository.StaffRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;
    private final StaffRepository staffRepo;

    public CustomUserDetailsService(UserRepository userRepo, StaffRepository staffRepo) {
        this.userRepo = userRepo;
        this.staffRepo = staffRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username).orElse(null);
        if (user != null) return new CustomUserDetails(user);

        return staffRepo.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username));
    }
}