package io.github.plotnik;

public class BookException extends RuntimeException {
    
    public String reason;
    
    public BookException(String reason) {
        this.reason = reason;
    }
}
