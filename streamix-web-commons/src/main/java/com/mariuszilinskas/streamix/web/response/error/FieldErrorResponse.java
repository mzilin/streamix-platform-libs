package com.mariuszilinskas.streamix.web.response.error;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class FieldErrorResponse extends ErrorResponse {

    private final Map<String, String> fieldErrors;

    /**
     * Construct an FieldErrorResponse with specified error messages.
     * The timestamp is automatically set to the current time.
     *
     * @param fieldErrors a map of field errors
     * @param status the HTTP status code
     * @param error the type of the error
     */
    public FieldErrorResponse(Map<String, String> fieldErrors, int status, String error) {
        super("Invalid input data. Please correct the errors and try again.", status, error);
        this.fieldErrors = fieldErrors;
    }
}
