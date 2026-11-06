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
    public String addCustomer(
            @RequestParam String fullName,
            @RequestParam String accountNumber,
            @RequestParam(required = false) String email,
            @RequestParam BigDecimal balance,
            RedirectAttributes redirectAttributes) {

        try {
            bankService.createCustomer(
                    fullName, accountNumber, email, balance);

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Customer added successfully");

        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", exception.getMessage());
        }

        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferRequest")
            TransferRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            List<Customer> customers = bankService.getAllCustomers();

            model.addAttribute("customers", customers);
            model.addAttribute("transactions", bankService.getAllTransactions());
            model.addAttribute("totalBalance", bankService.getTotalBalance());
            model.addAttribute("successCount",
                    bankService.getSuccessfulTransactionCount());
            model.addAttribute("failedCount",
                    bankService.getFailedTransactionCount());

            return "bank";
        }

        try {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Transfer completed successfully");

        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", exception.getMessage());
        }

        return "redirect:/";
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