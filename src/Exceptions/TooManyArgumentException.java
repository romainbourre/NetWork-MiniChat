package Exceptions;

public class TooManyArgumentException extends Exception {

    public TooManyArgumentException() {
        super("Vous avez saisie trop d'argument, 2 demandés");
    }

}
