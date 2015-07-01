package jetbrains.android.data;

public class RequestFailedException extends Exception {
    public RequestFailedException() {
    }

    public RequestFailedException(String message) {
        super(message);
    }

    public RequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestFailedException(Throwable cause) {
        super(cause);
    }
}
