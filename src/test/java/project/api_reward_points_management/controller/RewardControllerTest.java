package project.api_reward_points_management.controller;

import project.api_reward_points_management.model.RewardResponse;
import project.api_reward_points_management.model.Transaction;
import project.api_reward_points_management.repository.TransactionRepository;
import project.api_reward_points_management.service.RewardServiceImp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static project.api_reward_points_management.constants.AuthConstants.REWARDS_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardController.class)
public class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardServiceImp rewardService;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @Test
    public void testGetRewardsResponse() throws Exception {
        List<RewardResponse> mockRewards = List.of(
                new RewardResponse(1L, Map.of("2025-01", 50, "2025-02", 70), 120),
                new RewardResponse(2L, Map.of("2025-01", 30, "2025-02", 40), 70)
        );

        when(rewardService.calculateRewards()).thenReturn(mockRewards);

        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].totalPoints").value(120))
                .andExpect(jsonPath("$[1].customerId").value(2))
                .andExpect(jsonPath("$[1].totalPoints").value(70));
    }

    @Test
    public void testCalculateRewardsForValidCustomer() throws Exception {
        Long customerId = 1L;
        List<Transaction> transactionList = List.of(
                new Transaction(customerId, 50.00, LocalDate.of(2025, 1, 10)),
                new Transaction(customerId, 70.00, LocalDate.of(2025, 2, 1))
        );
        RewardResponse mockReward = new RewardResponse(customerId, Map.of("2025-01", 50, "2025-02", 70), 120);

        when(transactionRepository.findById(customerId)).thenReturn(transactionList);
        when(rewardService.calculateRewardsByCustomerId(customerId, transactionList)).thenReturn(mockReward);

        mockMvc.perform(get("/api/rewards/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.totalPoints").value(120));
    }

    @Test
    public void testCalculateRewardsForInvalidCustomer() throws Exception {
        Long customerId = 5L;
        when(transactionRepository.findById(customerId)).thenReturn(List.of());

        mockMvc.perform(get("/api/rewards/{customerId}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value(REWARDS_NOT_FOUND));

    }

    @Test
    public void testCalculateRewardsWithNoCustomerId() throws Exception {
        mockMvc.perform(get("/api/rewards/"))
                .andExpect(status().isNotFound())
                .andExpect(result ->
                        assertInstanceOf(NoResourceFoundException.class, result.getResolvedException()));
    }

}