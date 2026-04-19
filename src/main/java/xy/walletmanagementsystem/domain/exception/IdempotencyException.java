package xy.walletmanagementsystem.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IdempotencyException extends WalletManagementException {
    public IdempotencyException(String message) {
        super(message);
    }
}
