package librarymanagement.vn.library.domain.dto;

import librarymanagement.vn.library.util.constant.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BorrowFilterCriteriaDTO {
    private String memberName;
    private String bookTitle;
    private BorrowStatus status;
    private String librarianName;

}
