package CarRental.example.security;

import CarRental.example.document.Staff;
import CarRental.example.document.User;
import CarRental.example.repository.StaffRepository;
import CarRental.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;
    private final StaffRepository staffRepo;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepo, StaffRepository staffRepo) {
        this.userRepo = userRepo;
        this.staffRepo = staffRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user != null) {
            return new CustomUserDetails(user);
        }

        Staff staff = staffRepo.findByUsername(username);
        if (staff != null) {
            return new CustomUserDetails(staff);
        }

        throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
    }
}