/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nioEngine;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import chat.ChatClient;

/**
 *
 * @author Jah
 */
public class NioEngine implements I_NioEngine {//, Runnable {

    public SocketChannel channelToCommunicateWithServer; // to communicate with the server (Client side)
    public ServerSocketChannel channelToWaitConnectionFromClient; // to receive connection demands from Client (Server side)
    public SocketChannel channelToCommunicateWithClient; // to communicate with client (Server side)
    public Selector selector;
    public int numeroMessage; // pourra etre supprime et remplace par le contenu du message
    public ByteBuffer outBuffer;
    public ByteBuffer inBuffer;
    public HashMap<SocketChannel, ByteBuffer> inBuffers;
    public int octetsTransmis;
    public String message;
    public int sizeOfMessageToRead;
    public int numRead;
    public ArrayList<ClientChannel> registeredClients;
    public ArrayList<ClientChannel> currentGroup;//Groupe de clients connus par chaque client du groupe "actuel"
    public int typeOfMessage = 0;
    public static final int MESSAGE_TYPE_REGISTER = 1;
    public static final int MESSAGE_TYPE_LEAVE = 2;
    public static final int MESSAGE_TYPE_WHO = 3;
    public static final int MESSAGE_TYPE_SENDMSG = 4;
    public static final int MESSAGE_TYPE_QUIT = 5;
    public int serverResponse = 0;
    public final int CLIENT_REGISTER = 1;
    public ChatClient chatClient;
    public boolean isLastToSend;
    int numberOfUncompleteTransmission = 0;
    
    public enum SendType {

        BROADCAST, ONE;
    };
    public SendType sendType;
    public boolean running;
    public boolean isServer;
    public EtatReception etat;

    public NioEngine() throws IOException {
        numeroMessage = 0;
        outBuffer = ByteBuffer.allocate(4); // mettre à 4 au debut pour envoyer la taille du message
        inBuffer = ByteBuffer.allocate(4); // 
        inBuffers = new HashMap<SocketChannel,ByteBuffer>();
        sizeOfMessageToRead = 0;
        numRead = 0;
        etat = etat.RECEPTION_TAILLE;
        registeredClients = new ArrayList<ClientChannel>();
        currentGroup = new ArrayList<ClientChannel>();
        isLastToSend = false;
        running = true;
        

    }

    /**
     * Server-side NIO engine initialization
     *
     * @param the host address and port of the server
     * @param handler for received messages, should implement @code IRecvMsgHandler interface
     * @throws IOException
     */
    @Override
    public void InitializeAsServer(InetAddress hostAddress, int port, I_RecvMsgHandler handler) throws IOException {
        isServer = true;
        selector = SelectorProvider.provider().openSelector();

        // create a new non-blocking server socket channel
        channelToWaitConnectionFromClient = ServerSocketChannel.open();
        channelToWaitConnectionFromClient.configureBlocking(false);
        channelToWaitConnectionFromClient.socket().bind(new InetSocketAddress(hostAddress, port));
        //serverChannel.socket().bind(new InetSocketAddress(hostAddress, port)); // does not work in localhost

        // be notified when connection requests arrive
        channelToWaitConnectionFromClient.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("## Server STARTED at address " + hostAddress + " on port " + port + " ##\n");

    }

    /**
     * Client-side NIO engine initialization Tries to connects to the server.
     *
     * @param the host address and port of the server
     * @param handler for received messages, should implement @code IRecvMsgHandler interface
     * @throws IOException
     */
    @Override
    public void InitializeAsClient(InetAddress hostAddress, int port, I_RecvMsgHandler handler) throws IOException {
        isServer = false;
        //etat = etat.ENVOI_TAILLE;
        channelToCommunicateWithServer = SocketChannel.open();
        // set channel to non blocking
        channelToCommunicateWithServer.configureBlocking(false);
        // connect to a running server
        channelToCommunicateWithServer.connect(new InetSocketAddress(InetAddress.getLocalHost(), port));
        //serverSocket.connect(new InetSocketAddress("127.0.0.1", 8888));
        // get a selector
        selector = Selector.open();
        // register the client socket with "connect operation" to the selector
        channelToCommunicateWithServer.register(selector, SelectionKey.OP_CONNECT);
        System.out.println("## Client STARTED and will connect to " + hostAddress + " on port " + port + " ##\n");
    }

    @Override
    public void mainloop() { // prise en compte client uniquement, mettre serveur!
        try {
            while (true) {
                System.out.println("Attente d'un evenement");
                this.selector.select();
                Set keys = selector.selectedKeys();
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    System.out.println("Evenement recu");
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        System.out.println("Cle invalide");
                        continue;
                    } else if (key.isConnectable()) {
                        System.out.println("L'evenement recu est : \"isConnectable\"");
                        if (channelToCommunicateWithServer.isConnectionPending()) {//une connexion sur ce channel est-elle en cours?
                            channelToCommunicateWithServer.finishConnect();
                        }
                        System.out.println("La connexion etait en attente. Elle est maintenant établie");
                        key.interestOps(SelectionKey.OP_READ); // OK
                    }
                    if (key.isAcceptable()) {
                        System.out.println("L'evenement recu est : \"isAcceptable\"");
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        System.out.println("L'evenement recu est : \"isReadable\"");
                        handleDataIn(key);
                    } else if (key.isWritable()) {
                        System.out.println("L'evenement recu est : \"isWritable\"");
                        handleDataOut(key);
                    }

                }

            }

        } catch (IOException e) {
        }
    }

    /**
     * Send data on the client channel
     *
     * @param the data that should be sent
     */
    @Override
    public void send(byte[] data) {
        sendType = SendType.ONE;
        //String message;

        //socketChannel.register(selector, SelectionKey.OP_WRITE); grosse erreure !
        //wake up our selecting thread so it can make the required changes
        //outBuffer.put(data);
        outBuffer = ByteBuffer.allocate(data.length + 4 + 4); // il faudra ensuite remplacé par + 8
        System.out.println("La taille du message + type + contenu = " + (data.length + 4 + 4));
        outBuffer.clear();
        //outBuffer = ByteBuffer.wrap(new String("message salut : " + numeroMessage).getBytes()); !!! a ne pas utiliser !!!
        outBuffer.putInt(data.length); // met la taille du message dans le outBuffer
        System.out.println("Le type du message qu'on met dans outbuffer : " + typeOfMessage);
        outBuffer.putInt(typeOfMessage); // met le type du message a lire dans le outBuffer
        // il faudra faire un putInt(typeMessage)
        outBuffer.put(data); // met le contenu du message dans le outBuffer
        outBuffer.rewind();
        outBuffer.limit(data.length + 4 + 4);
        System.out.println("Si string dans le buffer, vaut en gros : " + new String(outBuffer.array()));
        //System.out.println("Si int dans le buffer, vaut : " + Integer.toString(java.nio.ByteBuffer.wrap(outBuffer.array()).getInt()));

        //socketChannel.write(outBuffer);
        //selector.wakeup();
    }

    /**
     * Send data on the each client channel (broadcast) but not on the client
     * channel that started the request
     *
     * @param the data that should be sent
     */
    @Override
    public void send(SocketChannel socketChannel, byte[] data) {
        sendType = SendType.BROADCAST;
        // SEND methode for server to broadcast receved messages

        for (ClientChannel clientChannel : registeredClients) {
            SocketChannel sock = clientChannel.socketChannel;
            if (sock != socketChannel) {
                numberOfUncompleteTransmission++;
                clientChannel.outBuffer = ByteBuffer.allocate(data.length + 4 + 4);
                //clientChannel.outBuffer = ByteBuffer.allocate(data.length + 4 + 4); // il faudra ensuite remplacé par + 8
                System.out.println("La taille du message + type + contenu = " + (data.length + 4 + 4));
                clientChannel.outBuffer.clear();
                clientChannel.outBuffer.putInt(data.length); // met la taille du message dans le outBuffer
                System.out.println("Le type du message qu'on met dans outbuffer : " + typeOfMessage);
                clientChannel.outBuffer.putInt(typeOfMessage);// met le type du message a lire dans le outBuffer
                clientChannel.outBuffer.put(data); // met le contenu du message dans le outBuffer
                clientChannel.outBuffer.rewind();
                clientChannel.outBuffer.limit(data.length + 4 + 4);
                System.out.println("Si string dans le buffer, vaut en gros : " + new String(clientChannel.outBuffer.array()));
            }
        }
    }

    @Override
    public void terminate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void attente(int ms) {
        // introduction d'une attente
        synchronized (this) {
            try {
                wait(ms);
            } catch (InterruptedException e) {
            }
        }
    }


    private void handleDataIn(SelectionKey key) throws IOException {
        System.out.println("DEBUT handledataIN");
        SocketChannel socketChannel = (SocketChannel) key.channel();//channel pour lequel la key a été créée
        int currentNumRead;
        if(!inBuffers.containsKey(socketChannel))
        	inBuffers.put(socketChannel, ByteBuffer.allocate(4));
        
        if (etat == etat.RECEPTION_TAILLE) {
            try {
            	currentNumRead = socketChannel.read(inBuffers.get(socketChannel));
                //currentNumRead = socketChannel.read(inBuffer); // devrait recevoir reellement
            } catch (IOException e) {
                System.out.println("HEYYYYYYYYYYYYYYYYYYYY close");
                key.cancel();
                socketChannel.close();
                return;
            }
            if (currentNumRead == -1) { // DOUBLON AVEC l'exception ?
                // Remote entity shut the socket down cleanly. Do the same from our end and cancel the key
                key.channel().close();
                key.cancel();
                return;
            }

            numRead += currentNumRead;
            System.out.println("currentNumRead = " + currentNumRead);
            System.out.println("numRead = " + numRead);

            if (numRead == 4) {
                System.out.println("#### TAILLE MESSAGE LU ENTIEREMENT ### OK");
                sizeOfMessageToRead = java.nio.ByteBuffer.wrap(inBuffers.get(socketChannel).array()).getInt();
                //sizeOfMessageToRead = java.nio.ByteBuffer.wrap(inBuffer.array()).getInt(); // stock la taille du message
                System.out.println("taille du message a lire : " + sizeOfMessageToRead);
                inBuffers.put(socketChannel, ByteBuffer.allocate(4));
                inBuffers.get(socketChannel).clear();
                //inBuffer = ByteBuffer.allocate(4);
                //inBuffer.clear();
                etat = etat.RECEPTION_TYPE;
                numRead = 0;
            }
        } else if (etat == etat.RECEPTION_TYPE) {
            try {
            	currentNumRead = socketChannel.read(inBuffers.get(socketChannel));
                //currentNumRead = socketChannel.read(inBuffer); // devrait recevoir reellement
            } catch (IOException e) {
                key.cancel();
                socketChannel.close();
                return;
            }
            if (currentNumRead == -1) { // DOUBLON AVEC l'exception ?
                // Remote entity shut the socket down cleanly. Do the same from our end and cancel the key
                key.channel().close();
                key.cancel();
                return;
            }

            numRead += currentNumRead;
            //inBuffer = ByteBuffer.allocate(5);
            System.out.println("currentNumRead = " + currentNumRead);
            System.out.println("numRead = " + numRead);
            //currentNumRead = 0;

            if (numRead == 4) {
                System.out.println("#### Type du MESSAGE LU ENTIEREMENT ### OK");
                typeOfMessage = java.nio.ByteBuffer.wrap(inBuffers.get(socketChannel).array()).getInt();
                System.out.println("<<<<<<<Type du message "+ typeOfMessage+">>>>>>>>>");
                //typeOfMessage = java.nio.ByteBuffer.wrap(inBuffer.array()).getInt(); // stock la taille du message
                System.out.println("type du message a lire : " + typeOfMessage);
                inBuffers.put(socketChannel, ByteBuffer.allocate(sizeOfMessageToRead));
                inBuffers.get(socketChannel).clear();
                //inBuffer = ByteBuffer.allocate(sizeOfMessageToRead); // agrandi le tableau
                //inBuffer.clear();
                etat = etat.RECEPTION_MESSAGE;
                numRead = 0;
            }

        } else { // etat == etat.RECEPTION_MESSAGE

            try {
            	currentNumRead = socketChannel.read(inBuffers.get(socketChannel));
                //currentNumRead = socketChannel.read(inBuffer); // devrait envoyer reellement
            } catch (IOException e) {
                key.cancel();
                socketChannel.close();
                return;
            }
            if (currentNumRead == -1) { // DOUBLON AVEC l'exception ?
                // Remote entity shut the socket down cleanly. Do the same from our end and cancel the key
                key.channel().close();
                key.cancel();
                return;
            }

            numRead += currentNumRead;
            System.out.println("currentNumReadReceptionMessage = " + currentNumRead);
            if (numRead == sizeOfMessageToRead) {
                System.out.println("#### TOUT A ETE LU (le messageeee) ### OK : " + new String(inBuffers.get(socketChannel).array()));

                // Process the received data, be aware that it may be incomplete
                if (isServer) {
                	processDataServer(inBuffers.get(socketChannel).array(), key);
                    //processDataServer(inBuffer.array(), key);
                } else {
                	processDataClient(inBuffers.get(socketChannel).array());
                    //processDataClient(inBuffer.array());
                }
                numRead = 0;
                //etat = etat.ENVOI_TAILLE;
                etat = etat.RECEPTION_TAILLE;
                inBuffers.put(socketChannel, ByteBuffer.allocate(4));
                //inBuffer = ByteBuffer.allocate(4); // remet la taille du tableau à 4 (int)
                //key.interestOps(SelectionKey.OP_WRITE);
            }
        }
    }

    private void handleDataOut(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        System.out.println("Entre dans la méthode handleDataOut");
        //if (side == Side.SERVER) {
        if (sendType == SendType.BROADCAST) {
            System.out.println("Envoi en broadcast");
            // if (etat == etat.ENVOI_TAILLE) { boolean
            //everithingSent = true; 
            for (ClientChannel clientChannel : registeredClients) {
                if (clientChannel.socketChannel != channelToCommunicateWithClient) {
                    clientChannel.socketChannel.write(clientChannel.outBuffer);
                    System.out.println("outBuffer position : " + clientChannel.outBuffer.position());
                    System.out.println("outBuffer limit : " + clientChannel.outBuffer.limit());
                    System.out.println("numberOfUncompleteTransmission : " + numberOfUncompleteTransmission);
                    // Be aware that the write may be incomplete
                    if (clientChannel.outBuffer.position() == clientChannel.outBuffer.limit()) {
                        numberOfUncompleteTransmission--;
                        if (numberOfUncompleteTransmission == 0) {
                            System.out.println("numberOfUncompleteTransmission = 0");
                            key.interestOps(SelectionKey.OP_READ);
                        }
                        // write bloquand si rien a transmettre dans le buffer ??????
                        //everithingSent = false; 
                    }
                }
            }
        } else {
            System.out.println("envoi point a point");
            socketChannel.write(outBuffer);
            if (outBuffer.position() == outBuffer.limit()) {
                System.out.println("#### TOUT A ETE TRANSMI (taille + contenu du message) ### OK");
                key.interestOps(SelectionKey.OP_READ);
                if (!running) {
                    key.channel().close(); // shutdown the channel properly
                    System.exit(0); // close application
                }
            }

        }
    }

    private void processDataServer(byte[] array, SelectionKey key) throws ClosedChannelException {
        channelToCommunicateWithClient = (SocketChannel) key.channel();
        String msg = new String(array);

        // existe déjà ?
        System.out.println("avant les checks");
        if (alreadyPresent(channelToCommunicateWithClient) == -1) {
            if (typeOfMessage == MESSAGE_TYPE_REGISTER) {
                registerClientChannel(msg, channelToCommunicateWithClient);
                if (registeredClients.size() > 1) {
                    send(channelToCommunicateWithClient, array);
                    //channelToCommunicateWithClient.register(selector, SelectionKey.OP_WRITE);
                    key.interestOps(SelectionKey.OP_WRITE);
                    selector.wakeup();
                }
            }
        } else { // deja present et actif
            if (typeOfMessage == MESSAGE_TYPE_SENDMSG) {
                //message a répliquer sur tout les clients
                System.out.println("Le serveur retourne : " + new String(array)); // a tronquer avec numRead et System
                if (registeredClients.size() > 1) {
                    send(channelToCommunicateWithClient, array);
                    key.interestOps(SelectionKey.OP_WRITE);
                    selector.wakeup();
                }
            } else if (typeOfMessage == MESSAGE_TYPE_LEAVE) {
                if (registeredClients.size() > 1) {
                    message = new String(registeredClients.get(alreadyPresent(channelToCommunicateWithClient)).name);
                    System.out.println("Le serveur retourne : " + new String(message.getBytes())); // a tronquer avec numRead et System
                    send(channelToCommunicateWithClient, message.getBytes());
                    key.interestOps(SelectionKey.OP_WRITE);
                    selector.wakeup();
                    registeredClients.remove(alreadyPresent(channelToCommunicateWithClient));
                }
            } else if (typeOfMessage == MESSAGE_TYPE_WHO) {
                message = who();
                System.out.println("Le serveur retourne la liste des clients : " + new String(message.getBytes())); // a tronquer avec numRead et System
                send(message.getBytes());
                key.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        }
    }

    private void processDataClient(byte[] array) {
        System.out.println("DEBUT processDataClient");
        if (typeOfMessage == NioEngine.MESSAGE_TYPE_SENDMSG) {
            chatClient.receivedCB(array, channelToCommunicateWithServer);
        } else if (typeOfMessage == NioEngine.MESSAGE_TYPE_REGISTER) {
            chatClient.receivedCB(array, channelToCommunicateWithServer);
        } else if (typeOfMessage == NioEngine.MESSAGE_TYPE_LEAVE) {
            chatClient.receivedCB(array, channelToCommunicateWithServer);
        } else if (typeOfMessage == NioEngine.MESSAGE_TYPE_WHO) {
            chatClient.receivedCB(array, channelToCommunicateWithServer);
        }

    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        // accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        // register the new SocketChannel with our Selector, indicating
        // we would like to be notified when there's data waiting to be read
        socketChannel.register(selector, SelectionKey.OP_READ);
        //key.interestOps(SelectionKey.OP_READ);
    }

    public int alreadyPresent(SocketChannel clientSocket) {
        int alreadyPresent = -1;
        int i = 0;;

        for (ClientChannel clientChannel : registeredClients) {

            if (clientChannel.socketChannel == clientSocket) {
                alreadyPresent = i;
                break;
            }
            i++;
        }

        return alreadyPresent; // if > 0, it exists at index alreadyPresent
    }

    // must check if it already exists
    public void registerClientChannel(String msg, SocketChannel clientSocket) {
        registeredClients.add(new ClientChannel(clientSocket, msg));
    }

    public String who() {
        String listeConnectes = new String("");

        for (ClientChannel clientChannel : registeredClients) {
            listeConnectes = listeConnectes + " \n- " + clientChannel.name;
        }

        return listeConnectes;
    }
}
