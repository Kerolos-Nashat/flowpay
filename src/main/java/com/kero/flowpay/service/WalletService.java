
package com.kero.flowpay.service;

import com.kero.flowpay.model.*;
import com.kero.flowpay.repository.WalletRepository;
import com.kero.flowpay.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public Wallet getWalletByEmail(String email) {
        return walletRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));
    }

    @Transactional
    public Wallet deposit(String email, BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive");
        }

        Wallet wallet = getWalletByEmail(email);

        wallet.setBalance(wallet.getBalance().add(amount));

        Transaction transaction = new Transaction(
                amount,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                wallet
        );

        transactionRepository.save(transaction);

        return wallet;
    }

    public List<Transaction> getTransactionHistory(String email) {

        Wallet wallet = getWalletByEmail(email);

        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }
}
