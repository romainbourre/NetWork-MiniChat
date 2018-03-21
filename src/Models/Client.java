package Models;

import java.io.*;
import java.net.Socket;

/**
 * Client
 */
public class Client implements Runnable {

    /**
     * Server
     */
    private TcpServer server = null;

    /**
     * Socket of client
     */
    private Socket socket = null;

    /**
     * Name of client
     */
    private String name = null;

    /**
     * Writer buffer of client
     */
    private BufferedWriter bw = null;

    /**
     * Reader buffer of client
     */
    private BufferedReader br = null;

    /**
     * Thread of client
     */
    private Thread clientThread;

    private boolean connected = false;

    /**
     * Create new client
     * @param address address of client
     * @param port port of client
     * @throws IOException
     */
    Client(String address, int port) throws IOException {
        this.socket = new Socket(address, port);
    }

    /**
     * Create new client
     * @param server server of client
     * @param s socket of client
     * @throws IOException
     */
    Client(TcpServer server, Socket s) throws IOException {
        this.connected = true;
        this.server = server;
        this.socket = s;
        // On demande et enregistre le nom du nouveau client
        while(name == null || name.isEmpty()) {
            write("\n\nQuel est votre nom ? ");
            name = read();
        }
        // On démarre le thread pour la réception des messages du client
        this.clientThread = new Thread(this);
        clientThread.start();
        // Confirmation de la connection du client
        write("\n\n* Vous êtes connecté " + name + " *\n\n");
        // On envoie un message pour annoncer aux autres clients que nous sommes connecté
        Message presentationMsg = new Message(null, "\n* " + name + " vient de se connecter *\n");
        server.sendMessageAllClients(presentationMsg, this, false);
    }

    /**
     * Get socket of client
     * @return Socket
     */
    Socket getSocket() {
        return socket;
    }

    /**
     * Set name of client
     * @param name name of client
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Get reader buffer of client
     * @return BufferedReader
     * @throws IOException
     */
    BufferedReader getBufferReader() throws IOException {
        if(br == null) br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return br;
    }

    /**
     * Get writer buffer of client
     * @return BufferedWriter
     * @throws IOException
     */
    public BufferedWriter getBufferWriter() throws IOException {
        if(bw == null) bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        return bw;
    }

    /**
     * Send string to client
     * @param s string message
     * @throws IOException
     */
    public void write(String s) throws IOException {
        getBufferWriter().write(s);
        getBufferWriter().flush();
    }

    /**
     * Read string message from client
     * @return String
     * @throws IOException
     */
    public String read() throws IOException {
        return getBufferReader().readLine();
    }

    /**
     * Close the client
     * @throws IOException
     */
    public void close() throws IOException {
        socket.close();
        server.deleteClient(this);
        this.connected = false;
    }

    /**
     * String of client
     * @return String
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Thread process of client to receive new message and treat them
     */
    @Override
    public void run() {
        String message;
        while(this.connected) {
            try {
                message = this.read();
                server.addMessage(new Message(this, message));
                server.treatMessages();
            }
            catch(IOException e) {}
        }
    }
}
