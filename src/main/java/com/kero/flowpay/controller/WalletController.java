
package com.kero.flowpay.controller;

import com.kero.flowpay.model.Wallet;
import com.kero.flowpay.model.Transaction;
import com.kero.flowpay.service.WalletService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public Map<String, Object> getMyWallet() {

        String email = getCurrentUserEmail();

        Wallet wallet = walletService.getWalletByEmail(email);

        return Map.of(
                "walletId", wallet.getId(),
                "balance", wallet.getBalance()
        );
    }

    @PostMapping("/deposit")
    public Map<String, Object> deposit(
            @Valid @RequestBody DepositRequest request) {

        String email = getCurrentUserEmail();

        Wallet wallet = walletService.deposit(
                email,
                request.amount()
        );

        return Map.of(
                "walletId", wallet.getId(),
                "balance", wallet.getBalance()
        );
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactionHistory() {

        String email = getCurrentUserEmail();

        return walletService.getTransactionHistory(email)
                .stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getType().name(),
                        transaction.getStatus().name(),
                        transaction.getCreatedAt()
                ))
                .toList();
    }

    private String getCurrentUserEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication.getName();
    }

    public record DepositRequest(
            @NotNull
            @DecimalMin(value = "0.0", inclusive = false)
            BigDecimal amount
    ) {}

    public record TransactionResponse(
            Long id,
            BigDecimal amount,
            String type,
            String status,
            LocalDateTime createdAt
    ) {}
}
