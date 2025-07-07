package project.api_reward_points_management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a customer transaction.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private Long customerId;
    private double amount;
    private LocalDate date;
}