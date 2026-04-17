package xy.walletmanagementsystem.domain.exception;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;
import xy.walletmanagementsystem.infrastructure.input.rest.message.ErrorMessages;

import java.time.ZonedDateTime;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class WalletManagementGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(WalletManagementException.class)
    public ResponseEntity<Object> handleFundsTrackerException(WalletManagementException exception) {
        return buildResponse(exception.getStatus(), exception.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessages.INTERNAL_SERVER_ERROR,
                exception.getMessage());
    }

    @ExceptionHandler({
            org.springframework.dao.DataAccessException.class,
            java.sql.SQLException.class,
            jakarta.persistence.PersistenceException.class
    })
    public ResponseEntity<Object> handleDatabaseExceptions(Exception exception) {
        log.error("Database error occurred: {}", exception.getMessage(), exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred.", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException exception) {
        log.error(exception.getMessage(), exception);
        return buildResponse(HttpStatus.FORBIDDEN, ErrorMessages.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error(exception.getMessage(), exception);
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode statusCode,
            @NonNull WebRequest request) {
        String message = Objects.requireNonNullElse(
                ex.getFieldError() != null ? ex.getFieldError().getDefaultMessage() : null,
                ErrorMessages.INVALID_INPUT_ERROR_MESSAGE);
        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.error(ex.getMessage(), ex);
        String details = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_INPUT_ERROR_MESSAGE, details);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message, Object data) {
        ApiResponse<Object> response = new ApiResponse<>();
        response.setMessage(message);
        response.setData(data);
        response.setStatusCode(status.value());
        response.setTimestamp(ZonedDateTime.now());
        response.setSuccessful(false);
        return new ResponseEntity<>(response, status);
    }
}
