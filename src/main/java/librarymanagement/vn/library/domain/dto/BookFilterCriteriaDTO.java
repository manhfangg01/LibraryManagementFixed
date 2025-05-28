package librarymanagement.vn.library.domain.dto;

import java.util.ArrayList;
import java.util.List;

public class BookFilterCriteriaDTO {
    private String title;
    private String author;
    // Khởi tạo categoryIds thành một ArrayList rỗng để tránh NullPointerException
    private List<Long> categoryIds = new ArrayList<>();

    // Getters và Setters cho tất cả các thuộc tính
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        // Đảm bảo rằng nếu một danh sách null được truyền vào (ví dụ từ một select đa
        // chọn rỗng),
        // chúng ta vẫn đặt nó thành một danh sách rỗng, không phải null.
        this.categoryIds = (categoryIds != null) ? categoryIds : new ArrayList<>();
    }

    // Bạn có thể thêm phương thức toString() để dễ debug
    @Override
    public String toString() {
        return "BookFilterCriteria{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", categoryIds=" + categoryIds +
                '}';
    }
}