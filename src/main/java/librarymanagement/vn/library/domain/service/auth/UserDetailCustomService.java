package librarymanagement.vn.library.domain.service.auth;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import librarymanagement.vn.library.domain.service.UserService;

@Service
public class UserDetailCustomService implements UserDetailsService {
    private final UserService userService;

    public UserDetailCustomService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        librarymanagement.vn.library.domain.model.auth.User user = this.userService.fetchUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Thông tin không đúng"));

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))); // Spring sẽ
                                                                                                            // tự động
                                                                                                            // xóa tiền
                                                                                                            // tế ROLE
                                                                                                            // khi nó
                                                                                                            // được gọi
                                                                                                            // hàm nhưng
                                                                                                            // nếu không
                                                                                                            // có "ROLE"
                                                                                                            // thì sẽ
                                                                                                            // khiến
                                                                                                            // Spring
                                                                                                            // hoạt động
                                                                                                            // sai
    }

}
