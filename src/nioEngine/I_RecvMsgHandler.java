/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nioEngine;

import java.nio.channels.SocketChannel;

/**
 *
 * @author Jah
 */

public interface I_RecvMsgHandler {
	
	// callback called when an entire message has been received
	public void receivedCB(byte[] data, SocketChannel socketChannel);
}
