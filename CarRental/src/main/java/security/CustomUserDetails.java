package CarRental.example.security;

import CarRental.example.document.Staff;
import CarRental.example.document.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private String id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    // Constructor cho khách hàng (User)
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        // Gán quyền mặc định cho User (Bạn có thể sửa lại theo DB của bạn)
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // Constructor cho nhân viên (Staff)
    public CustomUserDetails(Staff staff) {
        this.id = staff.getId();
        this.username = staff.getUsername();
        this.password = staff.getPassword();
        // Gán quyền mặc định cho Staff
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"));
    }

    public String getId() { return id; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}