package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.domain.model.Transaction;
import xy.walletmanagementsystem.infrastructure.output.entities.TransactionEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.TransactionMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.TransactionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionPersistenceAdapterTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @InjectMocks
    private TransactionPersistenceAdapter adapter;

    @Test
    void save_shouldPersistMappedEntity() {
        Transaction tx = Transaction.builder().transactionId(1L).build();
        TransactionEntity entity = TransactionEntity.builder().id(1L).build();
        when(transactionMapper.toEntity(tx)).thenReturn(entity);
        when(transactionRepository.save(entity)).thenReturn(entity);
        when(transactionMapper.toDomain(entity)).thenReturn(tx);

        adapter.save(tx);
        verify(transactionRepository).save(entity);
    }

    @Test
    void findById_shouldReturnOptionalDomain() {
        TransactionEntity entity = TransactionEntity.builder().id(1L).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(transactionMapper.toDomain(entity)).thenReturn(Transaction.builder().transactionId(1L).build());

        assertTrue(adapter.findById(1L).isPresent());
    }

    @Test
    void findAll_shouldReturnMappedList() {
        TransactionEntity entity = TransactionEntity.builder().id(1L).build();
        when(transactionRepository.findAll()).thenReturn(List.of(entity));
        when(transactionMapper.toDomain(entity)).thenReturn(Transaction.builder().transactionId(1L).build());

        assertEquals(1, adapter.findAll().size());
    }
}
