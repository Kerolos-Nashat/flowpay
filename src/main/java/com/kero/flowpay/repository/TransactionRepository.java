
package com.kero.flowpay.repository;

import com.kero.flowpay.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
}
