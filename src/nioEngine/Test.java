package nioEngine;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import chat.ChatClient;

/**
 * Test class for NioEngine. This class allows to start an NioEngine server
 * (through the command java NioEngine.Test -s) and a NioEngine client (through
 * the command java NioEngine). The client and server interact infinitely,
 * pinponging a small message.
 */
public class Test implements I_RecvMsgHandler {

    I_NioEngine nioEngine;
    Side side;

    public Test(String[] args) throws IOException {



        // SERVEUR
        if (args.length > 0 && args[0].equalsIgnoreCase("-s")) {
            System.out.println("Starting nio server");
            side = side.SERVER;
            //InetAddress addr = InetAddress.getByName("localhost"); // ne marche pas pour loopback 
            InetAddress addr = InetAddress.getLocalHost();
            nioEngine = new NioEngine();
            //new Thread(nioEngine);
            nioEngine.InitializeAsServer(addr, 8888, this);
            nioEngine.mainloop();
            
        } else { // CLIENT
            
            
            
            
            
            System.out.println("Starting nio client");
            side = side.CLIENT;
            side.toString();
            //InetAddress addr = InetAddress.getByName("localhost");  // ne marche pas pour loopback
            InetAddress addr = InetAddress.getLocalHost();
            nioEngine = new NioEngine();
            nioEngine.InitializeAsClient(addr, 8888, this);
            
            // create ChatClient
            new Thread(new ChatClient(nioEngine)).start();
            
            nioEngine.mainloop();
            
            
            //nioEngine.send("This is a test message".getBytes());

        }
    }

    // pas utilisée pour le moment
    // le serveur et le client ne traitent pas les données reçu de la meme manière !
    // sauf pour découper le message : type + contenu du message
    public void receivedCB(byte[] data, SocketChannel socketChannel) {
        System.out.println(side.toString() + " received: " + new String(data));
        nioEngine.send(socketChannel, data);
    }

    public static void main(String[] args) {
        try {
            new Test(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

  
}