package com.example.transactionservice.repository;

import com.example.transactionservice.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromWalletIdOrderByTransactionDateDesc(Long fromWalletId);

    List<Transaction> findByToWalletIdOrderByTransactionDateDesc(Long toWalletId);

    //Combined history for a wallet (sent + received)
    List<Transaction> findByFromWalletIdOrToWalletIdOrderByTransactionDateDesc(Long fromWalletId, Long toWalletId);

    Page<Transaction> findByFromWalletIdOrToWalletId(Pageable pageable, Long fromWalletId, Long toWalletId);

}
