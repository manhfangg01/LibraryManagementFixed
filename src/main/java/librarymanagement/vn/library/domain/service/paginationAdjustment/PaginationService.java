package librarymanagement.vn.library.domain.service.paginationAdjustment;

import org.springframework.stereotype.Service;

import librarymanagement.vn.library.domain.service.BookService;

@Service
public class PaginationService {
    private final BookService bookService; // Giả sử service này cần BookService để đếm tổng số sách

    public PaginationService(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Điều chỉnh trang đích sau khi xóa một đối tượng.
     * Thường sẽ quay lại trang trước đó nếu trang hiện tại trở nên trống
     * hoặc vượt quá tổng số trang mới.
     *
     * @param currentPage Trang hiện tại trước khi xóa.
     * @param pageSize    Kích thước trang.
     * @return Trang đích sau khi điều chỉnh.
     */
    public int getTargetPageAfterDeletion(int currentPage, int pageSize) {
        long newTotalItems = this.bookService.countAllBooks(); // Giả sử bạn có phương thức để đếm tổng số sách
        int newTotalPages = (int) Math.ceil((double) newTotalItems / pageSize);

        int targetPage = currentPage - 1;
        if (newTotalPages > 0 && targetPage > newTotalPages) {
            targetPage = newTotalPages; // Đi đến trang cuối cùng còn tồn tại
        } else if (newTotalPages == 0) {
            targetPage = 1; // Hoặc xử lý một view cụ thể cho "không có đối tượng"
        }

        return targetPage;
    }

    /**
     * Điều chỉnh trang đích sau khi tạo một đối tượng mới.
     * Thường sẽ chuyển hướng đến trang cuối cùng để hiển thị đối tượng mới.
     *
     * @param pageSize Kích thước trang.
     * @return Trang đích sau khi điều chỉnh.
     */
    public int getTargetPageAfterCreation(int pageSize) {
        long newTotalItems = bookService.countAllBooks(); // Lấy tổng số sách sau khi thêm
        int newTotalPages = (int) Math.ceil((double) newTotalItems / pageSize);

        // Luôn chuyển hướng đến trang cuối cùng để hiển thị đối tượng mới
        return (newTotalPages == 0) ? 1 : newTotalPages - 1;
    }
}
