package project.api_reward_points_management.utilities;

import project.api_reward_points_management.model.ErrorResponse;
import org.springframework.http.HttpStatus;

public final class appUtil {

    public static ErrorResponse globalErrorResponse(String requestParam, String errorMessage, HttpStatus status) {
        return generateErrorResponse(requestParam, errorMessage, status.name());
    }

    private static ErrorResponse generateErrorResponse(String requestParam, String errorMessage, String status) {
        ErrorResponse response = new ErrorResponse();
        response.setErrorMessage(errorMessage);
        response.setStatus(status);
        response.setData(requestParam);
        return response;
    }
}
