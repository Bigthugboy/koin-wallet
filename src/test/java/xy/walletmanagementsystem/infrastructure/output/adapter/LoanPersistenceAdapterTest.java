package xy.walletmanagementsystem.infrastructure.output.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.infrastructure.output.entities.LoanEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.LoanMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.LoanRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanPersistenceAdapterTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanMapper loanMapper;
    @InjectMocks
    private LoanPersistenceAdapter adapter;

    @Test
    void save_shouldPersistMappedEntity() {
        Loan loan = Loan.builder().loanId(1L).build();
        LoanEntity entity = LoanEntity.builder().id(1L).build();
        when(loanMapper.toEntity(loan)).thenReturn(entity);
        when(loanRepository.save(entity)).thenReturn(entity);
        when(loanMapper.toDomain(entity)).thenReturn(loan);

        adapter.save(loan);
        verify(loanRepository).save(entity);
    }

    @Test
    void findById_shouldReturnDomainOptional() {
        LoanEntity entity = LoanEntity.builder().id(1L).build();
        when(loanRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(loanMapper.toDomain(entity)).thenReturn(Loan.builder().loanId(1L).build());

        assertTrue(adapter.findById(1L).isPresent());
    }

    @Test
    void findAll_shouldReturnMappedList() {
        LoanEntity entity = LoanEntity.builder().id(1L).build();
        when(loanRepository.findAll()).thenReturn(List.of(entity));
        when(loanMapper.toDomain(entity)).thenReturn(Loan.builder().loanId(1L).build());

        assertEquals(1, adapter.findAll().size());
    }
}
