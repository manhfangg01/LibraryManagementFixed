package librarymanagement.vn.library.domain.model;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String authorName;
    private String publishedDate;
    @ElementCollection // Đánh dấu thuộc tính đa trị
    @CollectionTable(name = "book_images", joinColumns = @JoinColumn(name = "book_id")) // Tạo bảng để quản lý đa trị
                                                                                        // book_images
    @Column(name = "image_url") // tên cột trong bảng book_images
    private List<String> imageUrls;

    @OneToMany(mappedBy = "book")
    private List<Borrow> borrows;

    @ManyToMany
    @JoinTable(name = "books_categories", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;
}
