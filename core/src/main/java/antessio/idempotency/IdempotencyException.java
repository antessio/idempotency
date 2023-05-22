package antessio.idempotency;

public class IdempotencyException extends RuntimeException{

    private final ErrorCode errorCode;

    public IdempotencyException(ErrorCode errorCode) {
        super("error %s".formatted(errorCode));
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    enum ErrorCode{
        IN_PROGRESS_REQUEST
    }

}
