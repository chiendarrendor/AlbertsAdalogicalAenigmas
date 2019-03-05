public class UnhandledEmptyException extends RuntimeException {
    Board b;
    public UnhandledEmptyException(String message,Board b) { super(message); this.b = b; }
}
