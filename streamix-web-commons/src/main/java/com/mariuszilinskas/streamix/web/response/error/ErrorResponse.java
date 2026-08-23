package com.mariuszilinskas.streamix.web.response.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mariuszilinskas.streamix.web.response.ResponseConstants;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ResponseConstants.TIMESTAMP_FORMAT)
    private final ZonedDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;

    /**
     * Construct an ErrorResponse with a specified error message.
     * The timestamp is automatically set to the current time.
     *
     * @param message the error message
     * @param status the HTTP status code
     * @param error the type of the error
     */
    public ErrorResponse(String message, int status, String error) {
        this.message = message;
        this.status = status;
        this.error = error;
        this.timestamp = ZonedDateTime.now();
    }

}
