package be.kdg.prog6.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RequestErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(RequestErrorHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidRequest(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableRequest(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "The request body is not valid JSON, or a value has the wrong type");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException e, HttpServletRequest request) {
        log.warn("{} {} refused: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
    public ProblemDetail handleUnreachableDatabase(Exception e, HttpServletRequest request) {
        log.error("{} {} failed, the database cannot be reached", request.getMethod(), request.getRequestURI(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The database cannot be reached at the moment");
    }

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDatabaseError(DataAccessException e, HttpServletRequest request) {
        log.error("{} {} failed with a database error", request.getMethod(), request.getRequestURI(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "The change could not be stored");
    }

    @ExceptionHandler(AmqpException.class)
    public ProblemDetail handleUnreachableQueue(AmqpException e, HttpServletRequest request) {
        log.error("{} {} failed, the message broker cannot be reached", request.getMethod(), request.getRequestURI(), e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The message broker cannot be reached at the moment");
    }
}
