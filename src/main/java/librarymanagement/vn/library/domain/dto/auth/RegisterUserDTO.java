package librarymanagement.vn.library.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserDTO {
    @NotNull(message = "username cannot blank")
    @Size(min = 3, message = "Username must contain more than 3 characters")
    private String username;
    @NotNull(message = "email can not null")
    @Email(message = "Email is invalid", regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$")
    private String email;
    private int age;
    private String address;
    @Size(min = 3, message = "Password must contain more than 3 characters")
    private String password;
    private String confirmPassword;

}
