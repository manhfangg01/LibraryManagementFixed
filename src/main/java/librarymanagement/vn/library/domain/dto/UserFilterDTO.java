package librarymanagement.vn.library.domain.dto;

import librarymanagement.vn.library.domain.model.auth.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDTO {
    private String username;
    private String email;
    private int age;
    private String address;
    private Role role;
    private String profilePhoto;

}
