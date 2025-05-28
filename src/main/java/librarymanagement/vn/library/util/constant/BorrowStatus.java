package librarymanagement.vn.library.util.constant;

public enum BorrowStatus {
    PENDING("Đang chờ"),
    BORROWED("Đang mượn"),
    RETURNED("Đã trả"),
    OVERDUE("Quá hạn");

    private final String displayName;

    BorrowStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
