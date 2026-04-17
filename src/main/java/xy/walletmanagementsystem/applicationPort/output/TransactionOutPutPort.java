package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionOutPutPort {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(String transactionId);
    List<Transaction> findByUserId(String userId);
    List<Transaction> findAll();
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
}
