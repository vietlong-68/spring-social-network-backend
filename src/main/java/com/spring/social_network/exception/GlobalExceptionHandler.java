package com.spring.social_network.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.spring.social_network.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(AppException.class)
        public ResponseEntity<ApiResponse<Object>> handleAppException(
                        AppException ex, HttpServletRequest request) {

                ErrorCode errorCode = ex.getErrorCode();
                HttpStatus httpStatus = determineHttpStatus(errorCode);

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                ex.getMessage(),
                                httpStatus.value(),
                                errorCode.getCode(),
                                request);

                return ResponseEntity.status(httpStatus).body(apiResponse);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
                        AccessDeniedException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Bạn không có quyền truy cập tài nguyên này",
                                HttpStatus.FORBIDDEN.value(),
                                "Truy cập bị từ chối",
                                request);

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
        }

        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<ApiResponse<Object>> handleAuthorizationDeniedException(
                        AuthorizationDeniedException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Quyền truy cập bị từ chối",
                                HttpStatus.FORBIDDEN.value(),
                                "Ủy quyền bị từ chối",
                                request);

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
        }

        @ExceptionHandler(JwtException.class)
        public ResponseEntity<ApiResponse<Object>> handleJwtException(
                        JwtException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                ex.getMessage(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "JWT Error",
                                request);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
                        AuthenticationException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Xác thực thất bại: " + ex.getMessage(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Lỗi xác thực",
                                request);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
                        BadCredentialsException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Thông tin đăng nhập không hợp lệ",
                                HttpStatus.UNAUTHORIZED.value(),
                                "Thông tin đăng nhập sai",
                                request);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleAuthenticationCredentialsNotFoundException(
                        AuthenticationCredentialsNotFoundException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Không tìm thấy thông tin xác thực",
                                HttpStatus.UNAUTHORIZED.value(),
                                "Thiếu thông tin xác thực",
                                request);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        @ExceptionHandler(InsufficientAuthenticationException.class)
        public ResponseEntity<ApiResponse<Object>> handleInsufficientAuthenticationException(
                        InsufficientAuthenticationException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Xác thực không đủ",
                                HttpStatus.UNAUTHORIZED.value(),
                                "Xác thực không đủ",
                                request);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }

        private HttpStatus determineHttpStatus(ErrorCode errorCode) {
                switch (errorCode) {
                        case USER_NOT_FOUND:
                        case ROLE_NOT_FOUND:
                        case RESOURCE_NOT_FOUND:
                                return HttpStatus.NOT_FOUND;
                        case USER_ALREADY_EXISTS:
                        case ROLE_ALREADY_EXISTS:
                                return HttpStatus.CONFLICT;
                        case USER_INVALID_INPUT:
                        case VALIDATION_ERROR:
                        case BAD_REQUEST:
                                return HttpStatus.BAD_REQUEST;
                        case USER_UNAUTHORIZED:
                                return HttpStatus.UNAUTHORIZED;
                        case USER_IS_BLOCKED:
                                return HttpStatus.FORBIDDEN;
                        case METHOD_NOT_ALLOWED:
                                return HttpStatus.METHOD_NOT_ALLOWED;
                        case REQUEST_TIMEOUT:
                                return HttpStatus.REQUEST_TIMEOUT;
                        case TOO_MANY_REQUESTS:
                                return HttpStatus.TOO_MANY_REQUESTS;
                        case SERVICE_UNAVAILABLE:
                                return HttpStatus.SERVICE_UNAVAILABLE;
                        case INTERNAL_SERVER_ERROR:
                        default:
                                return HttpStatus.INTERNAL_SERVER_ERROR;
                }
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                List<String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.toList());

                String errorMessage = String.join(", ", errors);

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Tham số yêu cầu không hợp lệ",
                                HttpStatus.BAD_REQUEST.value(),
                                errors,
                                request);

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
                        ConstraintViolationException ex, HttpServletRequest request) {

                List<String> errors = ex.getConstraintViolations()
                                .stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.toList());

                String errorMessage = String.join(", ", errors);

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Dữ liệu yêu cầu không hợp lệ",
                                HttpStatus.BAD_REQUEST.value(),
                                errors,
                                request);

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Định dạng body yêu cầu không hợp lệ",
                                HttpStatus.BAD_REQUEST.value(),
                                "JSON không hợp lệ",
                                request);

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiResponse<Object>> handleMissingParameter(
                        MissingServletRequestParameterException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Tham số bắt buộc '" + ex.getParameterName() + "' bị thiếu",
                                HttpStatus.BAD_REQUEST.value(),
                                "Thiếu tham số",
                                request);

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Object>> handleException(
                        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

                Class<?> requiredType = ex.getRequiredType();
                String typeName = requiredType != null ? requiredType.getSimpleName() : "không xác định";

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Tham số '" + ex.getName() + "' phải có kiểu dữ liệu " + typeName,
                                HttpStatus.BAD_REQUEST.value(),
                                "Không khớp kiểu dữ liệu",
                                request);

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Phương thức HTTP '" + ex.getMethod() + "' không được hỗ trợ cho endpoint này",
                                HttpStatus.METHOD_NOT_ALLOWED.value(),
                                "Phương thức không được phép",
                                request);

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(apiResponse);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
                        IllegalArgumentException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Yêu cầu không hợp lệ",
                                request);

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
                        RuntimeException ex, HttpServletRequest request) {

                if (ex.getMessage() != null && ex.getMessage().contains("Tài khoản đã bị khóa")) {
                        ApiResponse<Object> apiResponse = ApiResponse.error(
                                        ex.getMessage(),
                                        HttpStatus.FORBIDDEN.value(),
                                        "Tài khoản đã bị khóa",
                                        request);

                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
                }

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                ex.getMessage(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Lỗi runtime",
                                request);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Object>> handleGenericException(
                        Exception ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.",
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Lỗi máy chủ nội bộ",
                                request);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
}
