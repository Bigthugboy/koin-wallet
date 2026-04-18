package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.EmailObject;

public interface EmailOutPutPort {
        void sendEmail(EmailObject email);
}
