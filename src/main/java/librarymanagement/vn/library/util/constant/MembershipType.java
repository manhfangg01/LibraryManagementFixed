package librarymanagement.vn.library.util.constant;

public enum MembershipType {
    BASIC("Cơ bản"),
    PREMIUM("Cao cấp"),
    STUDENT("Sinh viên"),
    VIP("VIP"),
    // Thêm hằng số Regular vào đây
    REGULAR("Thường xuyên"); // Hoặc tên hiển thị bạn muốn

    private final String label;

    MembershipType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}