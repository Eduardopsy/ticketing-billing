package com.jeduardo.ticketing.billing.repository;

import com.jeduardo.ticketing.billing.domain.entities.Cobranca;
import com.jeduardo.ticketing.billing.domain.enums.CobrancaStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {

    Optional<Cobranca> findTopByTxidOrderByIdDesc(String txid);

    Optional<Cobranca> findTopByTransactionIdOrderByIdDesc(String transactionId);

    Optional<Cobranca> findTopByIdOrderByIdDesc(Long id);

    List<Cobranca> findByStatus(CobrancaStatusEnum status);
}
