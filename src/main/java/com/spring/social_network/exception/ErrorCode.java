package com.spring.social_network.exception;

public enum ErrorCode {
    USER_NOT_FOUND("USER_001", "Không tìm thấy người dùng"),
    USER_ALREADY_EXISTS("USER_002", "Người dùng đã tồn tại"),
    USER_INVALID_INPUT("USER_003", "Dữ liệu người dùng không hợp lệ"),
    USER_UNAUTHORIZED("USER_004", "Không có quyền truy cập"),
    USER_IS_BLOCKED("USER_005", "Tài khoản đã bị khóa"),
    ROLE_NOT_FOUND("ROLE_001", "Không tìm thấy vai trò"),
    ROLE_ALREADY_EXISTS("ROLE_002", "Vai trò đã tồn tại"),
    INTERNAL_SERVER_ERROR("SYS_001", "Lỗi hệ thống nội bộ"),
    VALIDATION_ERROR("SYS_002", "Lỗi xác thực dữ liệu"),
    RESOURCE_NOT_FOUND("SYS_003", "Không tìm thấy tài nguyên"),
    BAD_REQUEST("SYS_004", "Yêu cầu không hợp lệ"),
    METHOD_NOT_ALLOWED("SYS_005", "Phương thức HTTP không được hỗ trợ"),
    REQUEST_TIMEOUT("SYS_006", "Yêu cầu quá thời gian chờ"),
    TOO_MANY_REQUESTS("SYS_007", "Quá nhiều yêu cầu, vui lòng thử lại sau"),
    SERVICE_UNAVAILABLE("SYS_008", "Dịch vụ tạm thời không khả dụng"),
    INVALID_CREDENTIALS("SYS_009", "Tài khoản hoặc mật khẩu không hợp lệ"),
    FILE_UPLOAD_FAILED("FILE_001", "Upload file thất bại"),
    POST_NOT_FOUND("POST_001", "Không tìm thấy bài viết"),
    POST_FORBIDDEN("POST_002", "Không có quyền truy cập bài viết"),
    COMMENT_NOT_FOUND("COMMENT_001", "Không tìm thấy bình luận"),
    COMMENT_FORBIDDEN("COMMENT_002", "Không có quyền thao tác với bình luận này"),
    REPLY_NOT_FOUND("REPLY_001", "Không tìm thấy trả lời"),
    REPLY_FORBIDDEN("REPLY_002", "Không có quyền thao tác với trả lời này"),
    EMAIL_SEND_FAILED("EMAIL_001", "Gửi email thất bại"),
    INVALID_TOKEN("TOKEN_001", "Token không hợp lệ"),
    TOKEN_EXPIRED("TOKEN_002", "Token đã hết hạn");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("ErrorCode{code='%s', message='%s'}", code, message);
    }
}
