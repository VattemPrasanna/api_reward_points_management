package project.api_reward_points_management.exception;

public class NoCustomerFoundException extends RuntimeException {
    public NoCustomerFoundException(String errorMessage) {
        super(errorMessage);
    }
}
