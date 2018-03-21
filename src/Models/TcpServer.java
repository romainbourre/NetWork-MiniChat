package Models;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TCP Server
 */
public class TcpServer extends ServerSocket implements Runnable {

    /**
     * Instance of TcpServer
     */
    private static TcpServer server = null;

    /**
     * Name of server
     */
    private static String name = "MiniChat";

    /**
     * Host of server
     */
    private static String host = "127.0.0.1";

    /**
     * Port of server
     */
    private static int port = 3000;

    /**
     * List of connected clients
     */
    private ConcurrentLinkedQueue<Client> clients = new ConcurrentLinkedQueue<Client>();

    /**
     * Pile of messages
     */
    private ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<Message>();

    /**
     * Create server
     * @throws IOException
     */
    private TcpServer() throws IOException {
        // On créer le serveur
        super(TcpServer.port, 20, InetAddress.getByName(TcpServer.host));
        // On démarre le thread d'écoute client
        (new Thread(this)).start();
    }

    /**
     * Get name of server
     * @return
     */
    public static String getName() {
        return name;
    }

    /**
     * Set name of server, if instance server doesn't exist
     * @param name name of server
     */
    public static void setName(String name) {
        if(TcpServer.server == null && !name.equals("")) TcpServer.name = name;
    }

    /**
     * Get host of server
     * @return
     */
    public static String getHost() {
        return host;
    }

    /**
     * Get host of server, if instance server doesn't exist
     * @param host of server
     */
    public static void setHost(String host) {
        if(TcpServer.server == null && !host.equals("")) TcpServer.host = host;
    }

    /**
     * Get port of server
     * @return
     */
    public static int getPort() {
        return port;
    }

    /**
     * Set port of server, if instance server doesn't exist
     * @param port port of server
     */
    public static void setPort(int port) {
        if(TcpServer.server == null) TcpServer.port = port;
    }

    /**
     * Start server with saved parameters
     * @return Server
     * @throws IOException
     */
    public static TcpServer start() throws IOException {
        if(TcpServer.server == null) TcpServer.server = new TcpServer();
        return TcpServer.server;
    }

    /**
     * Accept client and add in clients list
     * @return Client
     * @throws IOException
     */
    public Client acceptClient() throws IOException {
        Socket s =  super.accept();
        Client c;
        clients.add(c = new Client(this, s));
        return c;
    }

    /**
     * Delete client of server
     * @param client
     */
    void deleteClient(Client client) {
        if(clients != null) clients.remove(client);
    }

    /**
     * Get clients list
     * @return Clients Iterator
     */
    public Iterator<Client> getClients() {
        return clients.iterator();
    }

    /**
     * Get connected number of clients
     * @return number of clients
     */
    public int countClient() {
        return clients.size();
    }

    /**
     * Add message in messages pile
     * @param message message
     */
    public void addMessage(Message message) {
        if(messages != null) messages.add(message);
    }

    /**
     * Get messages list
     * @return Messages Iterator
     */
    public Iterator<Message> getMessages() {
        ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<Message>();
        if(!this.messages.isEmpty()) {
            messages.addAll(this.messages);
            this.messages.clear();
        }
        return messages.iterator();
    }

    /**
     * Send Message to all clients
     * @param message message to send
     * @param exceptionClient exception client doesn't send
     * @throws IOException
     */
    void sendMessageAllClients(Message message, Client exceptionClient, boolean head) throws IOException {

        // On liste tout les clients
        Iterator<Client> senderIterator;
        senderIterator = this.getClients();
        Client clientSend;

        while (senderIterator.hasNext()) {

            clientSend = senderIterator.next();

            // On envoie le message à tout le monde, sauf au client précisé en deuxième paramètre (si non null)
            if (exceptionClient == null || !clientSend.equals(exceptionClient)) {
                if(head) clientSend.write(message.toString() + "\n"); else clientSend.write(message.getMessage() + "\n");
            }

        }

    }

    /**
     * Treat all messages in a pile
     * @throws IOException
     */
    public void treatMessages() throws IOException {

        Iterator<Message> messages;

        // On lis tous les messages de la pile
        messages = this.getMessages();
        Message message;
        String content;
        while(messages.hasNext()) {

            message = messages.next();
            content = message.getMessage();

            if(content != null && content.equals("bye")) {
                // On dis aurevoir à l'utilisateur
                message.getClient().write("\n Aurevoir " + message.getClient() + " !\n");
                message.getClient().write("* Vous êtes déconnecté " + message.getClient() + " *\n\n");
                // On envoie le message de déconnexion de l'utilisateur à tout les autres clients
                Message goodByeMessage = new Message(null, "* " + message.getClient().toString() + " s'est déconnecté *");
                sendMessageAllClients(goodByeMessage, goodByeMessage.getClient(), false);
                // On "ferme" le client
                message.getClient().close();
            }
            else if(content != null && !content.isEmpty()) {
                // On envoie le message de l'utilisateur à tous les autres utilisateurs
                sendMessageAllClients(message, message.getClient(), true);
            }
            else if(content == null) {
                message.getClient().close();
            }

        }

    }

    /**
     * Close server
     * @throws IOException
     */
    @Override
    public void close() throws IOException {

        // On envoie une notification à tout les utilisateurs
        Message exitMessage = new Message(null, "\n* Le serveur " + TcpServer.getName() + " a été arrêté *\n");
        this.sendMessageAllClients(exitMessage, null, false);

        // On ferme tout les clients
        Iterator<Client> clients = getClients();
        Client client;
        while(clients.hasNext()) {
            client = clients.next();
            client.close();
        }

        // On ferme le serveur
        super.close();
        TcpServer.server = null;
        this.clients = null;
        this.messages = null;

    }

    /**
     * Thread of server to accept new client
     */
    @Override
    public void run() {
        Client newClient = null;
        while(TcpServer.server != null) {
            try {
                if((newClient = acceptClient()) != null) {
                }
            }
            catch(IOException e) {}
        }
    }

    /**
     * Check if host pattern is correct
     * @param host host
     * @return
     */
    public static boolean checkHostPattern(String host) {

        Pattern p = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");
        Matcher m;
        boolean valid;

        try {

            if ((valid = !host.equals(""))) {
                m = p.matcher(host);
                valid = m.matches();
                String values[] = host.split("\\.");
                for (int i = 0; i < values.length; i++) {
                    if (Integer.parseInt(values[i]) > 255 || Integer.parseInt(values[i]) < 0)
                        valid = false;
                }
            }

        }
        catch(NumberFormatException nfe) {
            valid = false;
        }

        return valid;

    }

    /**
     * Check if port pattern is correct
     * @param port port
     * @return
     */
    public static boolean checkPortPattern(int port) {
        return (port > 0 && port <= 65536);
    }

    /**
     * Check name pattern of server
     * @param name name
     * @return
     */
    public static boolean checkNamePattern(String name) {
        return !name.isEmpty() && Pattern.compile("^(([a-zA-Z]|[0-9])*)$").matcher(name).matches();
    }

}
