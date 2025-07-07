package project.api_reward_points_management.service;

import project.api_reward_points_management.configuration.PropertyConfig;
import project.api_reward_points_management.constants.AuthConstants;
import project.api_reward_points_management.exception.NoCustomerFoundException;
import project.api_reward_points_management.model.RewardResponse;
import project.api_reward_points_management.model.Transaction;
import project.api_reward_points_management.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to calculate reward points for transactions.
 */
@Service
public class RewardServiceImp implements RewardService {
    private static final Logger logger = LoggerFactory.getLogger(RewardServiceImp.class);

    private final TransactionRepository transactionRepository;
    private final PropertyConfig propertyConfig;

    public RewardServiceImp(TransactionRepository transactionRepository, PropertyConfig propertyConfig) {
        this.transactionRepository = transactionRepository;
        this.propertyConfig = propertyConfig;
    }

    /**
     * Calculates reward points for all customers, grouped by month.
     */
    public List<RewardResponse> calculateRewards() {
        logger.info("Calculating rewards for all customers at service level started at {}", System.currentTimeMillis());
        List<Transaction> transactionList = transactionRepository.findAll();
        logger.info("All transactions retrieved: size {}", transactionList.size());

        if (CollectionUtils.isEmpty(transactionList)) {
            logger.warn("No transactions found. Returning empty reward response list.");
            return Collections.emptyList();
        }

        // Group transactions by customer
        Map<Long, List<Transaction>> customerTransactions = transactionList.stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId));

        return customerTransactions.entrySet().stream()
                .map(entry -> calculateRewardsForTransactions(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates reward points for a specific customer.
     */
    @Override
    public RewardResponse calculateRewardsByCustomerId(Long customerId, List<Transaction> transactionList) {
        if (customerId == null || transactionList == null || transactionList.isEmpty()) {
            throw new NoCustomerFoundException("Customer ID or transaction list cannot be null or empty.");
        }
        return calculateRewardsForTransactions(customerId, transactionList);
    }

    /**
     * Common logic to calculate rewards for a customer and their transactions.
     */
    private RewardResponse calculateRewardsForTransactions(Long customerId, List<Transaction> transactionList) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AuthConstants.YEAR_MONTH_FORMAT);

        // Find latest transaction date for this customer
        LocalDate latestDate = transactionList.stream()
                .map(Transaction::getDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        // Calculate the start date for the 3-month period (inclusive)
        LocalDate startDate = latestDate.minusMonths(propertyConfig.getTimePeriod()).withDayOfMonth(1);

        // Filter transactions within the 3-month period
        List<Transaction> filteredTransactions = transactionList.stream()
                .filter(tx -> !tx.getDate().isBefore(startDate) && !tx.getDate().isAfter(latestDate))
                .toList();

        // Group by month and sum points
        Map<String, Integer> monthPoints = filteredTransactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getDate().format(formatter),
                        Collectors.summingInt(tx -> calculatePoints(tx.getAmount()))
                ));

        int total = monthPoints.values().stream().mapToInt(Integer::intValue).sum();
        return new RewardResponse(customerId, monthPoints, total);
    }

    /**
     * Calculates reward points for a single transaction amount.
     *
     * @param amount Transaction amount
     * @return Points earned
     */
    public int calculatePoints(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Transaction amount cannot be negative: " + amount);
        }
        int points = 0;
        if (amount > 100) {
            logger.info("Amount {} exceeds maximum range $100. Calculating points accordingly.", amount);
            points += (int) ((amount - 100) * 2 + 50);
        } else if (amount > 50) {
            logger.info("Amount {} is within the range $50 - $100. Calculating points accordingly.", amount);
            points += (int) (amount - 50);
        }
        return points;
    }

    public boolean isCustomerExists(Long customerId) {
        List<Transaction> transactionList = transactionRepository.findById(customerId);
        return transactionList != null && !transactionList.isEmpty();
    }
}