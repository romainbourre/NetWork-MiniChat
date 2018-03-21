import Exceptions.FewArgumentException;
import Exceptions.IncorrectArgumentException;
import Exceptions.TooManyArgumentException;
import Models.TcpServer;
import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MiniChat {

    public static void main(String argv[]) {

        TcpServer server = null;

        try {
            // Si des arguments sont présent
            if (argv.length == 2) {

                // On vérifie l'argument host
                if(TcpServer.checkHostPattern(argv[0])) {
                    TcpServer.setHost(argv[0]);
                }
                else {
                    throw new IncorrectArgumentException(argv[0]);
                }

                // On vérifie l'agument port
                try {
                    int portArg;
                    if (TcpServer.checkPortPattern(portArg = Integer.parseInt(argv[1]))) TcpServer.setPort(portArg); else throw new IncorrectArgumentException(argv[1]);
                }
                catch(NumberFormatException nfe) {
                    throw new IncorrectArgumentException(argv[1]);
                }

                // On démarre le serveur
                try {
                    System.out.print("\nDémarrage du serveur " + TcpServer.getName() + "... ");
                    server = TcpServer.start();
                    System.out.println("Serveur " + TcpServer.getName() + " connecté (" + TcpServer.getHost() + ":" + TcpServer.getPort() + ")");
                }
                catch(IOException ioe) {
                    System.out.println("\n" + ioe + "\n");
                }


            }
            else if(argv.length > 0 && argv.length < 2) {
                throw new FewArgumentException();
            }
            else if(argv.length > 2) {
                throw new TooManyArgumentException();
            }

            System.out.println("\nBienvenue sur le serveur " + TcpServer.getName());

            Scanner s = new Scanner(System.in);
            int choice;

            try {

                do {

                    // Menu utilisateurs
                    System.out.println("\nQue voulez-vous faire ?");
                    System.out.println("1. Démarrer/Arrêter le serveur");
                    System.out.println("2. Voir le nombre d'utilisateurs connectés");
                    System.out.println("3. Paramètrer le serveur");
                    System.out.println("0. Quitter le serveur");
                    System.out.print("Choix : ");
                    choice = s.nextInt();
                    s.nextLine();

                    switch (choice) {

                        case 0: // On quitte l'application, si le serveur est allumé, on l'éteint
                            if (server != null) {
                                System.out.print("\nFermeture du serveur " + TcpServer.getName() + "... ");
                                server.close();
                                System.out.println("Serveur " + TcpServer.getName() + " arrêté");
                            }
                            System.out.println("\nAurevoir et à bientôt!");
                            return;

                        case 1: // On démarre ou arrête le serveur
                            if (server == null) {
                                System.out.print("\nDémarrage du serveur " + TcpServer.getName() + "... ");
                                server = TcpServer.start();
                                System.out.println("Serveur " + TcpServer.getName() + " connecté (" + TcpServer.getHost() + ":" + TcpServer.getPort() + ")");
                            } else {
                                System.out.print("\nFermeture du serveur " + TcpServer.getName() + "... ");
                                // En cas de client(s) connecté(s) on demande confirmation
                                boolean confirmClose = false;
                                if(server.countClient() > 0) {
                                    System.out.print("\nDes clients sont connecté, êtes-vous sûr de vouloir arrêter le serveur ? (y/n) ");
                                    confirmClose = s.nextLine().equals("y");
                                }
                                // Si les conditions sont réunis on ferme le serveur
                                if(server.countClient() == 0 || confirmClose) {
                                    server.close();
                                    server = null;
                                    System.out.println("Serveur " + TcpServer.getName() + " arrêté");
                                }
                            }
                            break;

                        case 2:
                            if(server != null) {
                                System.out.println("\nIl y a actuellement " + server.countClient() + " client(s) connecté(s)");
                            }
                            else {
                                System.out.println("\nLe serveur doit être allumé pour pouvoir consulter le nombre d'utilisateur connecté");
                            }
                            break;

                        case 3: // Menu de paramètrage
                            if(server == null) {
                                int submenu;
                                do {

                                    System.out.println("\n1. Changer le nom du serveur");
                                    System.out.println("2. Changer l'adresse ip");
                                    System.out.println("3. Changer le port");
                                    System.out.println("0. Retour");
                                    System.out.print("Choix : ");
                                    submenu = s.nextInt();
                                    s.nextLine();

                                    switch (submenu) {

                                        case 0:
                                            break;

                                        case 1:
                                            String name;
                                            boolean validName;
                                            do {

                                                System.out.println("\nNom actuel : " + TcpServer.getName());
                                                System.out.print("Nouveau nom : ");
                                                name = s.nextLine();
                                                if (!(validName = TcpServer.checkNamePattern(name)))
                                                    System.out.println("\nSaisie incorrect (le nom du serveur ne doit pas contenir de caractère spéciaux). Recommencez.");

                                            } while (!validName);
                                            TcpServer.setName(name);
                                            break;

                                        case 2:
                                            String ip;
                                            boolean validIp;
                                            do {

                                                System.out.println("\nAdresse ip actuelle : " + TcpServer.getHost());
                                                System.out.print("Nouvelle adresse ip : ");
                                                ip = s.nextLine();
                                                if (!(validIp = TcpServer.checkHostPattern(ip)))
                                                    System.out.println("\nSaisie incorrect (l'adresse ip doit être au format 255.255.255.255). Recommencez.");

                                            } while (!validIp);
                                            TcpServer.setHost(ip);
                                            break;

                                        case 3:
                                            int port = -1;
                                            boolean validPort;
                                            do {
                                                try {
                                                    System.out.println("\nPort actuel : " + TcpServer.getPort());
                                                    System.out.print("Nouveau port : ");
                                                    port = s.nextInt();
                                                    validPort = TcpServer.checkPortPattern(port);
                                                } catch (InputMismatchException e) {
                                                    validPort = false;
                                                } finally {
                                                    s.nextLine();
                                                }
                                                if (!validPort)
                                                    System.out.println("\nSaisie incorrect (le port doit être entre 0 et 65536). Recommencez.");
                                            } while (!validPort);
                                            TcpServer.setPort(port);
                                            break;

                                        default:
                                            System.out.println("Votre choix est incorrect.");
                                            break;

                                    }

                                } while (submenu != 0);

                            }
                            else {
                                System.out.println("\nLe serveur doit être éteint pour pouvoir changer les paramètres");
                            }
                            break;

                        default:
                            System.out.println("Votre choix est incorrect.");
                            break;

                    }

                } while (true);


            } catch (IOException e) {
                System.out.println("\n" + e + "\n");
            }

        }
        catch (FewArgumentException fa) {
            System.out.println("\n" + fa.getMessage() + "\n");
        }
        catch (TooManyArgumentException tma) {
            System.out.println("\n" + tma.getMessage() + "\n");
        }
        catch(IncorrectArgumentException iae) {
            System.out.println("\n" + iae.getMessage() + "\n");
        }

    }

}
