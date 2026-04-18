package xy.walletmanagementsystem.applicationPort.output;

import xy.walletmanagementsystem.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionOutPutPort {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(Long transactionId);
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findAll();
    Optional<Transaction> findByReference(String reference);
}
