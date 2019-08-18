package grid.logic;

public class ContainerRuntimeException extends RuntimeException {
    Object o = null;
    public ContainerRuntimeException(String message) { super(message); }
    public ContainerRuntimeException(String message, Throwable cause) { super(message,cause); }
    public ContainerRuntimeException(String message, Throwable cause, Object contained) { super(message,cause); o = contained; }

    public Object getContained() { return o; }
}
