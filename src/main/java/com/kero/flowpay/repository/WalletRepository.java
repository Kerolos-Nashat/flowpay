
package com.kero.flowpay.repository;

import com.kero.flowpay.model.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM Wallet w
            JOIN w.user u
            WHERE LOWER(u.email) = LOWER(:email)
            """)
    Optional<Wallet> findByUserEmailWithLock(
            @Param("email") String email
    );
}
