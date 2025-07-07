package project.api_reward_points_management.controller;

import project.api_reward_points_management.constants.AuthConstants;
import project.api_reward_points_management.exception.RewardServiceException;
import project.api_reward_points_management.model.ErrorResponse;
import project.api_reward_points_management.model.RewardResponse;
import project.api_reward_points_management.model.Transaction;
import project.api_reward_points_management.repository.TransactionRepository;
import project.api_reward_points_management.service.RewardServiceImp;
import project.api_reward_points_management.utilities.appUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for reward points API.
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardController {
    private static final Logger logger = LoggerFactory.getLogger(RewardController.class);

    private final RewardServiceImp rewardService;
    private final TransactionRepository transactionRepository;

    public RewardController(RewardServiceImp rewardService, TransactionRepository transactionRepository) {
        this.rewardService = rewardService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Returns reward points per customer, per month, and total.
     *
     * @return List of reward responses
     */

    @GetMapping()
    public ResponseEntity<?> getRewardsResponse() {
        logger.info("Processing request to get rewards at controller level started at {}", System.currentTimeMillis());
        List<RewardResponse> rewards;
        try {
            rewards = rewardService.calculateRewards();
        } catch (Exception ex) {
            logger.error("Error occurred while processing rewards: {}", ex.getMessage(), ex);
            throw new RewardServiceException(ex.getMessage(), true, AuthConstants.FAILED_TO_GET_REWARDS, ex.getCause());
        }
        if (CollectionUtils.isEmpty(rewards)) {
            logger.warn("No rewards found");
            ErrorResponse error = appUtil.globalErrorResponse(null, AuthConstants.REWARDS_NOT_FOUND, HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        logger.info("Returning {} rewards responses.", rewards.size());
        return new ResponseEntity<>(rewards, HttpStatus.OK);
    }

    /**
     * Returns reward points for a specific customer by customerId.
     *
     * @param customerId the ID of the customer
     * @return RewardResponse containing monthly and total points for the customer
     */

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getRewardsByCustomerId(@PathVariable("customerId") Long customerId) {
        List<Transaction> transactionList;
        try {
            transactionList = transactionRepository.findById(customerId);
            if (CollectionUtils.isEmpty(transactionList)) {
                logger.warn("No transactions found for customerId: {}", customerId);
                ErrorResponse error = appUtil.globalErrorResponse(null, AuthConstants.REWARDS_NOT_FOUND, HttpStatus.NOT_FOUND);
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            }
            RewardResponse response;
            try {
                response = rewardService.calculateRewardsByCustomerId(customerId, transactionList);
            } catch (Exception ex) {
                logger.error("Error occurred while calculating rewards for customerId {}: {}", customerId, ex.getMessage(), ex);
                throw new RewardServiceException(ex.getMessage(), true, AuthConstants.FAILED_TO_GET_REWARDS, ex.getCause());
            }
            logger.info("Returning {} rewards response for customer {}.", response, customerId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("Error occurred while retrieving transactions for customerId {}: {}", customerId, ex.getMessage(), ex);
            throw new RewardServiceException(ex.getMessage(), true, AuthConstants.FAILED_TO_GET_TRANSACTIONS, ex.getCause());
        }
    }
}