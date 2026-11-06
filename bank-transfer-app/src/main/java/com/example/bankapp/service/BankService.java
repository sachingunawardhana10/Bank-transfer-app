package com.example.bankapp.service;

import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.exception.InvalidTransferException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditService auditService;

    @Transactional
    public Customer createCustomer(
            String fullName,
            String accountNumber,
            String email,
            BigDecimal balance) {

        if (customerRepository.countAccounts(accountNumber) > 0) {
            throw new InvalidTransferException("Account number already exists");
        }

        if (email != null && customerRepository.countEmails(email) > 0) {
            throw new InvalidTransferException("Email already exists");
        }

        Customer customer = new Customer();

        customer.setFullName(fullName);
        customer.setAccountNumber(accountNumber);
        customer.setEmail(email);
        customer.setBalance(balance);

        return customerRepository.save(customer);
    }

    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        try {
            if (fromAccount.equals(toAccount)) {
                throw new InvalidTransferException(
                        "Sender and receiver must be different accounts");
            }

            Customer sender = customerRepository
                    .getByAccountNumber(fromAccount)
                    .orElseThrow(() -> new AccountNotFoundException(
                            "Sender account not found"));

            customerRepository.getByAccountNumber(toAccount)
                    .orElseThrow(() -> new AccountNotFoundException(
                            "Receiver account not found"));

            int debited = customerRepository.debit(fromAccount, amount);

            if (debited == 0) {
                throw new InsufficientBalanceException(
                        "Insufficient balance");
            }

            if (simulateFailure) {
                throw new RuntimeException("All changes were rolled back");
            }

            customerRepository.credit(toAccount, amount);

            BankTransaction transaction = new BankTransaction();

            transaction.setFromAccount(fromAccount);
            transaction.setToAccount(toAccount);
            transaction.setAmount(amount);
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setDescription("Transfer successful");
            transaction.setCreatedAt(LocalDateTime.now());

            return transactionRepository.save(transaction);

        } catch (RuntimeException exception) {

            auditService.recordFailure(
                    fromAccount,
                    toAccount,
                    amount,
                    exception.getMessage()
            );

            throw exception;
        }
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.searchByName(keyword);
    }

    public List<BankTransaction> getAllTransactions() {
        return transactionRepository.findAllLatest();
    }

    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    public BigDecimal getTotalBalance() {
        return customerRepository.getTotalBalance();
    }

    public long getSuccessfulTransactionCount() {
        return transactionRepository.countByStatus(TransactionStatus.SUCCESS);
    }

    public long getFailedTransactionCount() {
        return transactionRepository.countByStatus(TransactionStatus.FAILED);
    }
}