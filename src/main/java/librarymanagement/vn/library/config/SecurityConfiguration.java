package librarymanagement.vn.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices.RememberMeTokenAlgorithm;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import jakarta.servlet.DispatcherType;
import librarymanagement.vn.library.domain.service.auth.UserDetailCustomService;
import librarymanagement.vn.library.domain.service.UserService;

@Configuration
// @EnableMethodSecurity
public class SecurityConfiguration {
    @Value("$SECRET_VALUE")
    private String secretKey;

    String[] whiteList = { "/", "/login", "/register", "/logout", "/client/**", "/css/**", "/js/**", "/image/**" };

    public UserDetailsService userDetailsService(UserService userService) {
        return new UserDetailCustomService(userService);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return new CustomSuccessHandler();
    }

    @Bean
    DaoAuthenticationProvider authProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        // authProvider.setHideUserNotFoundExceptions(false);

        return authProvider;
    }

    @Bean
    public SpringSessionRememberMeServices rememberMeServices() {
        SpringSessionRememberMeServices rememberMeServices = new SpringSessionRememberMeServices();
        // optionally customize
        rememberMeServices.setAlwaysRemember(true);
        return rememberMeServices;
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("MY_SESSION_ID");
        serializer.setCookiePath("/");
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        serializer.setCookieMaxAge(60 * 60 * 24 * 30);

        return serializer;
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
                        .rememberMeServices(rememberMeServices()))
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-deny"))
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/logout?expired")
                        .maximumSessions(1) // Giới hạn số lượng session của người dùng
                        .maxSessionsPreventsLogin(true)) // Cho phép đăng nhập mới nếu đã có session cũ (Nếu true thì
                                                         // sẽ áp dụng cái ở trên)

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL để kích hoạt logout (POST)
                        .logoutSuccessUrl("/?logout") // Chuyển hướng về trang login với param logout khi đăng xuất
                                                      // thành công
                        .invalidateHttpSession(true) // Hủy bỏ session hiện tại
                        .deleteCookies("MY_SESSION_ID") // Xóa cookie JSESSIONID
                        .permitAll()); // Cho phép tất cả truy cập URL logout

        return http.build();
        // Muốn đăng xuất theo cơ chế tự động của Spring thì phải truyền cho nó CSRF
        // token -> Nhờ cso cái này nên không cần phải viết endpoint cho logout -> tất
        // cả đều tự độgn

    }

}

// Session đucợ lưu trong cookies chứ 1 chuỗi đó chính là ID ánh xạ đến thông
// tin người dùng trong RAM