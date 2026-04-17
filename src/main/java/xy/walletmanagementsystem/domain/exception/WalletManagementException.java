package xy.walletmanagementsystem.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class WalletManagementException extends Exception{
    private HttpStatus status;

    public WalletManagementException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus;
    }

    public WalletManagementException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public WalletManagementException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public WalletManagementException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public WalletManagementException(Throwable cause) {
        super(cause);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
