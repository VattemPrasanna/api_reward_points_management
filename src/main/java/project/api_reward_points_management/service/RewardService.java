package project.api_reward_points_management.service;

import project.api_reward_points_management.model.RewardResponse;
import project.api_reward_points_management.model.Transaction;

import java.util.List;

public interface RewardService {
    List<RewardResponse> calculateRewards();

    RewardResponse calculateRewardsByCustomerId(Long customerId, List<Transaction> transactionList);

}