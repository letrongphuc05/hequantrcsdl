package CarRental.example.security;

import CarRental.example.document.Staff;
import CarRental.example.document.User;
import CarRental.example.repository.StaffRepository;
import CarRental.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        return userRepo.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseGet(() -> staffRepo.findByUsername(username)
                        .map(CustomUserDetails::new)
                        .orElseThrow(() -> new UsernameNotFoundException("Not found: " + username)));
    }
}