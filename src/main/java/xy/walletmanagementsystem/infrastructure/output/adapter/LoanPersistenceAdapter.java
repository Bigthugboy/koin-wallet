package xy.walletmanagementsystem.infrastructure.output.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xy.walletmanagementsystem.applicationPort.output.LoanOutPutPort;
import xy.walletmanagementsystem.domain.model.Loan;
import xy.walletmanagementsystem.infrastructure.output.entities.LoanEntity;
import xy.walletmanagementsystem.infrastructure.output.mapper.LoanMapper;
import xy.walletmanagementsystem.infrastructure.output.repository.LoanRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoanPersistenceAdapter implements LoanOutPutPort {
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    @Override
    public Loan save(Loan loan) {
        LoanEntity entity = loanMapper.toEntity(loan);
        if (entity.getId() == null) {
            entity.setId(java.util.UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        }
        LoanEntity savedEntity = loanRepository.save(entity);
        return loanMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Loan> findById(Long loanId) {
        return loanRepository.findById(loanId)
                .map(loanMapper::toDomain);
    }

    @Override
    public List<Loan> findByUserId(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(loanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Loan> findAll() {
        return loanRepository.findAll().stream()
                .map(loanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Loan> findByIdempotencyKey(String idempotencyKey) {
        return loanRepository.findByIdempotencyKey(idempotencyKey)
                .map(loanMapper::toDomain);
    }
}
