package librarymanagement.vn.library.domain.dto;

import librarymanagement.vn.library.util.constant.MembershipType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberFilterCriteriaDTO {
    private String name;
    private String email;
    private String phone;
    private MembershipType membershipType;
    private Boolean status;

}
