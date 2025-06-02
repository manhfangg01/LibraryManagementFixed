package librarymanagement.vn.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.DispatcherType;
import librarymanagement.vn.library.domain.service.auth.UserDetailCustomService;
import librarymanagement.vn.library.domain.service.UserService;

@Configuration
// @EnableMethodSecurity
public class SecurityConfiguration {
    String[] whiteList = { "/", "/login", "/register", "/logout", "/client/**", "/css/**", "/js/**", "/image/**" };

    public UserDetailsService userDetailsService(UserService userService) {
        return new UserDetailCustomService(userService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return new CustomSuccessHandler();
    }

    @Bean
    public DaoAuthenticationProvider authProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        // authProvider.setHideUserNotFoundExceptions(false);

        return authProvider;
    }

    @Bean
    public SecurityFilterChain customFilterChain(HttpSecurity http, UserDetailsService userDetailsService)
            throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE).permitAll()
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())

                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .failureUrl("/access-deny")
                        .successHandler(
                                myAuthenticationSuccessHandler())
                        .permitAll())
                .rememberMe(rememberMe -> rememberMe
                        .tokenValiditySeconds(86000 * 30) // Set token validity to 30 days (86000 seconds * 30)
                        .userDetailsService(userDetailsService))
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-deny"))
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL để kích hoạt logout (POST)
                        .logoutSuccessUrl("/login?logout") // Chuyển hướng về trang login với param logout khi đăng xuất
                                                           // thành công
                        .invalidateHttpSession(true) // Hủy bỏ session hiện tại
                        .deleteCookies("JSESSIONID") // Xóa cookie JSESSIONID
                        .permitAll()); // Cho phép tất cả truy cập URL logout
        return http.build();
        // Muốn đăng xuất theo cơ chế tự động của Spring thì phải truyền cho nó CSRF
        // token -> Nhờ cso cái này nên không cần phải viết endpoint cho logout -> tất
        // cả đều tự độgn

    }

}

// Session đucợ lưu trong cookies chứ 1 chuỗi đó chính là ID ánh xạ đến thông
// tin người dùng trong RAM