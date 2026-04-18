package xy.walletmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WalletManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletManagementSystemApplication.class, args);
    }

}
