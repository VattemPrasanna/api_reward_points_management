package project.api_reward_points_management.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RewardServiceException extends RuntimeException {
    private final String data;
    private final boolean hasError;
    private String message;

    public RewardServiceException(String data, boolean hasError, String message, Throwable cause) {
        super(message, cause);
        this.data = data;
        this.message = message;
        this.hasError = hasError;
    }
}
