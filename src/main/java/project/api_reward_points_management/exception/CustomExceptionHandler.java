package project.api_reward_points_management.exception;

import project.api_reward_points_management.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RewardServiceException.class)
    public ResponseEntity<ErrorResponse> handleRewardException(RewardServiceException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        if (exception.getData() != null || exception.getMessage() != null) {
            try {
                errorResponse.setData(exception.getData());
                errorResponse.setErrorMessage(exception.getMessage());
                errorResponse.setHasError(exception.isHasError());
                errorResponse.setStatus(String.valueOf(exception.isHasError() ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR));
            } catch (Exception e) {
                errorResponse.setData("Invalid data provided");
            }
        }

        return new ResponseEntity<>(errorResponse, exception.isHasError() ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
