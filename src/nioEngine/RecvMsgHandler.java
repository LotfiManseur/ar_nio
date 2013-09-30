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
public class RecvMsgHandler implements I_RecvMsgHandler{

    @Override
    public void receivedCB(byte[] data, SocketChannel socketChannel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
