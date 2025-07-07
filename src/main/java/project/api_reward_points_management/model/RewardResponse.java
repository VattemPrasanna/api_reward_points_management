package project.api_reward_points_management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response object for reward points per customer.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RewardResponse {
    private Long customerId;
    private Map<String, Integer> monthlyPoints;
    private int totalPoints;
}