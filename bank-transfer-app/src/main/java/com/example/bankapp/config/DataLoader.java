package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final CustomerRepository customerRepository;

    @Bean
    CommandLineRunner loadData() {
        return args -> {

            if (customerRepository.count() == 0) {

                Customer kamal = new Customer();
                kamal.setFullName("Kamal");
                kamal.setAccountNumber("100001");
                kamal.setEmail("kamal@example.com");
                kamal.setBalance(new BigDecimal("50000.00"));

                Customer nimali = new Customer();
                nimali.setFullName("Nimali");
                nimali.setAccountNumber("100002");
                nimali.setEmail("nimali@example.com");
                nimali.setBalance(new BigDecimal("25000.00"));

                Customer saman = new Customer();
                saman.setFullName("Saman");
                saman.setAccountNumber("100003");
                saman.setEmail("saman@example.com");
                saman.setBalance(new BigDecimal("10000.00"));

                customerRepository.saveAll(
                        List.of(kamal, nimali, saman)
                );
            }
        };
    }
}