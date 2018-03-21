package Exceptions;

public class FewArgumentException extends Exception {

    public FewArgumentException() {
        super("Vous n'avez pas saisie assez d'arguments, 2 demandés");
    }

}
