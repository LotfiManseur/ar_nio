package nioEngine;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import nioEngine.I_RecvMsgHandler;




public interface I_NioEngine {
 
	/**
	 * Server-side NIO engine initialization
	 * @param the host address and port of the server
	 * @param handler for received messages, 
         * should implement @code IRecvMsgHandler interface
	 * @throws IOException 
	 */
	public void InitializeAsServer (
                InetAddress hostAddress, int port, I_RecvMsgHandler handler) 
			throws IOException;

	/**
	 * Client-side NIO engine initialization 
	 * Tries to connects to the server.
	 * @param the host address and port of the server
	 * @param handler for received messages, 
               should implement @code IRecvMsgHandler interface
	 * @throws IOException 
	 */
	public void InitializeAsClient (
                InetAddress hostAddress, int port, I_RecvMsgHandler handler) 
			throws IOException;

	
	/**
	 * NIO engine mainloop
	 * Manage message sending and receiving 
         * (see @code I_RecvMsgHandler interface),
	 * If the NioEngine runs as a server, 
         * also manage clients connections and deconnections.
	 */
	public void mainloop();


	/**
	 * Send data on the client channel (for client-side engine)
	 * @param the data that should be sent  
	 */
	public void send(byte[] data);
	
	
	/**
	 * Send data on the given channel (for server-side engine)
	 * @param the key of the channel on which data that should be sent
	 * @param the data that should be sent
	 */
	public void send(SocketChannel socketChannel, byte[] data);
	
	
	/**
	 * Close the client channel
	 * @param the channel to close
	 */
	public void terminate() ;
	
}



