
package com.kero.flowpay.service;

import com.kero.flowpay.model.Transaction;
import com.kero.flowpay.model.TransactionStatus;
import com.kero.flowpay.model.TransactionType;
import com.kero.flowpay.model.Wallet;

import com.kero.flowpay.repository.WalletRepository;
import com.kero.flowpay.repository.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import com.kero.flowpay.exception.ResourceNotFoundException;
import com.kero.flowpay.exception.BadRequestException;
import com.kero.flowpay.exception.InsufficientBalanceException;

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
                        new ResourceNotFoundException("Wallet not found"));
    }

    // DEPOSIT
    @Transactional
    public Wallet deposit(String email, BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
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



@Transactional
public Wallet withdraw(String email, BigDecimal amount) {

    // 1. Validate amount
    if (amount == null ||
            amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("Amount must be positive");
    }

    // 2. Get user's wallet
    Wallet wallet = getWalletByEmail(email);

    // 3. Check sufficient balance
    if (wallet.getBalance().compareTo(amount) < 0) {
        throw new InsufficientBalanceException("Insufficient balance");
    }

    // 4. Deduct amount
    wallet.setBalance(
            wallet.getBalance().subtract(amount)
    );

    // 5. Create withdrawal transaction
    Transaction transaction = new Transaction(
            amount,
            TransactionType.WITHDRAWAL,
            TransactionStatus.COMPLETED,
            wallet
    );

    // 6. Save transaction
    transactionRepository.save(transaction);

    return wallet;
}


    // TRANSACTION HISTORY
    public List<Transaction> getTransactionHistory(String email) {

        Wallet wallet = getWalletByEmail(email);

        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }

    // MONEY TRANSFER
    @Transactional
    public void transfer(
            String senderEmail,
            String recipientEmail,
            BigDecimal amount) {

        // 1. Validate amount
        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Amount must be positive");
        }
        
        // 2. Prevent self-transfer
        if (senderEmail.equalsIgnoreCase(recipientEmail)) {
            throw new BadRequestException(
                    "Cannot transfer to yourself");
        }

        // 3. Lock wallets in a consistent order
        String firstEmail =
                senderEmail.compareToIgnoreCase(recipientEmail) < 0
                        ? senderEmail : recipientEmail;

        String secondEmail =
                firstEmail.equalsIgnoreCase(senderEmail)
                        ? recipientEmail : senderEmail;

        Wallet firstWallet = walletRepository
                .findByUserEmailWithLock(firstEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        Wallet secondWallet = walletRepository
                .findByUserEmailWithLock(secondEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet not found"));

        // 4. Identify sender and recipient
        Wallet senderWallet =
                senderEmail.equalsIgnoreCase(firstEmail)
                        ? firstWallet : secondWallet;

        Wallet recipientWallet =
                senderEmail.equalsIgnoreCase(firstEmail)
                        ? secondWallet : firstWallet;

        // 5. Check sufficient balance
        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        // 6. Update balances
        senderWallet.setBalance(
                senderWallet.getBalance().subtract(amount));

        recipientWallet.setBalance(
                recipientWallet.getBalance().add(amount));

        // 7. Create transaction records
        Transaction outgoingTransaction = new Transaction(
                amount,
                TransactionType.TRANSFER_OUT,
                TransactionStatus.COMPLETED,
                senderWallet
        );

        Transaction incomingTransaction = new Transaction(
                amount,
                TransactionType.TRANSFER_IN,
                TransactionStatus.COMPLETED,
                recipientWallet
        );

        // 8. Save both transactions
        transactionRepository.saveAll(
                List.of(outgoingTransaction, incomingTransaction)
        );
    }
}
