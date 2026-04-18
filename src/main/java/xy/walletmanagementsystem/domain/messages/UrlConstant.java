package xy.walletmanagementsystem.domain.messages;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlConstant {
    public static final String BASE_URL = "/api/v1/koin-wallet";
    public static final String KYC_URL = BASE_URL + "/kyc";
    public static final String AUTH_URL = BASE_URL + "/auth";
    public static final String USER_URL = BASE_URL + "/users/account";
    public static final String ADMIN_URL = BASE_URL + "/admin";
    public static final String TRANSACTION_URL = BASE_URL + "/transaction";
    public static final String WALLET_URL = BASE_URL + "/wallet";
    public static final String LOAN_URL = BASE_URL + "/loan";
    public static final String IDEMPOTENCY_KEY = "idempotency-key";

}
