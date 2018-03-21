package Exceptions;

public class IncorrectArgumentException extends Exception {

    public IncorrectArgumentException(String argument) {
        super("Argument incorrect : " + argument);
    }

}
