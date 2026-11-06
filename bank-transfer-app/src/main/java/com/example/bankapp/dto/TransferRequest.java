package com.example.bankapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Sender account is required")
    private String fromAccount;

    @NotBlank(message = "Receiver account is required")
    private String toAccount;

    private boolean simulateFailure;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 12, fraction = 2, message = "Amount can have up to 2 decimal places")
    private BigDecimal amount;
}