
package com.kero.flowpay.repository;

import com.kero.flowpay.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserEmailIgnoreCase(String email);
}
