package xy.walletmanagementsystem.infrastructure.input.rest.data.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.micrometer.common.lang.Nullable;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;

    private T data;

    private int statusCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXXXX'['VV']'")
    private ZonedDateTime timestamp;

    private boolean isSuccessful;

    public static ApiResponse<String> ok(String object) {

        return ApiResponse.<String>builder()
                .data(null)
                .message(object)
                .statusCode(HttpStatus.OK.value())
                .isSuccessful(true)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> ok(T object) {
        return ApiResponse.<T>builder()
                .data(object)
                .message("")
                .statusCode(HttpStatus.OK.value())
                .isSuccessful(true)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T body, String message) {
        return ApiResponse.<T>builder()
                .data(body)
                .message(message)
                .statusCode(HttpStatus.OK.value())
                .isSuccessful(true)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public static Object accessDeniedError(String message) {
        return ApiResponse.builder()
                .data(null)
                .message(message)
                .statusCode(HttpStatus.FORBIDDEN.value())
                .isSuccessful(false)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public ApiResponse<T> ok(T object, String message) {

        return ApiResponse.<T>builder()
                .data(object)
                .message(message)
                .statusCode(HttpStatus.OK.value())
                .isSuccessful(true)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public ApiResponse<T> created(T object, String message) {

        return ApiResponse.<T>builder()
                .data(object)
                .message(message)
                .statusCode(HttpStatus.CREATED.value())
                .isSuccessful(true)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public static ApiResponse<String> badRequest(String message) {

        return ApiResponse.<String>builder()
                .data(null)
                .message(message)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .isSuccessful(false)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public static ApiResponse<String> internalServerError(@Nullable String message) {

        return ApiResponse.<String>builder()
                .data(null)
                .message(message)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .isSuccessful(false)
                .timestamp(ZonedDateTime.now())
                .build();
    }
}

