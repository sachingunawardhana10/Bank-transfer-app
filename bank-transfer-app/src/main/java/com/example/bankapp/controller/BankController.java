package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Customer> customers;

        if (keyword != null && !keyword.isBlank()) {
            customers = bankService.searchCustomers(keyword);
        } else {
            customers = bankService.getAllCustomers();
        }

        model.addAttribute("customers", customers);
        model.addAttribute("transactions", bankService.getAllTransactions());
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("successCount",
                bankService.getSuccessfulTransactionCount());
        model.addAttribute("failedCount",
                bankService.getFailedTransactionCount());
        model.addAttribute("transferRequest", new TransferRequest());

        return "bank";
    }

    @PostMapping("/customers")
    public String addCustomer(@RequestBody Customer customer) {

        bankService.createCustomer(
                customer.getFullName(),
                customer.getAccountNumber(),
                customer.getEmail(),
                customer.getBalance()
        );

        return "Customer created successfully";
    }

    @PostMapping("/transfer")
    @ResponseBody
    public String transfer(@Valid @RequestBody TransferRequest request) {

        bankService.transfer(
                request.getFromAccount(),
                request.getToAccount(),
                request.getAmount(),
                request.isSimulateFailure()
        );

        return "Transfer completed successfully";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(
            @PathVariable String accountNumber,
            Model model) {

        model.addAttribute("accountNumber", accountNumber);
        model.addAttribute(
                "transactions",
                bankService.getAccountHistory(accountNumber));

        return "history";
    }
}