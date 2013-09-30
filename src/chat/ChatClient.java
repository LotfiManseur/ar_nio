/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.awt.Color;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import nioEngine.I_NioEngine;
import nioEngine.I_RecvMsgHandler;
import nioEngine.NioEngine;

/**
 *
 * @author Jah
 */
public class ChatClient implements I_ChatClient, I_RecvMsgHandler, Runnable {

    public ChatGUI gui;
    public NioEngine nioEngine; // reference to the nioEngine's client
    //public String clientName;

    public ChatClient(I_NioEngine nioEngine) {
        this.nioEngine = (NioEngine) nioEngine;
        this.nioEngine.chatClient = this;
        //this.clientName = null;
    }

    @Override // register message type : 1
    public void register(String clientName) throws ChatException {
        //if (this.clientName == null) {
        //this.clientName = clientName;
        //}

        //gui.text.append("yoyoyo");
            /*
         * gui.text.setText("wech"); gui.text.setText("a");
         * gui.text.setText("b");
         *
         */
        gui.text.append("[REGISTER] request to join the chat as \"" + clientName + "\" \n");
        //nioEngine.generateMessage();
        nioEngine.message = new String(clientName);
        nioEngine.typeOfMessage = NioEngine.MESSAGE_TYPE_REGISTER;
        nioEngine.send(nioEngine.message.getBytes());
        try {
            nioEngine.channelToCommunicateWithServer.register(nioEngine.selector, SelectionKey.OP_WRITE);
        } catch (ClosedChannelException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        nioEngine.selector.wakeup();

    }

    @Override // register message type : 2
    public void leave() throws ChatException {
        gui.text.append("[LEAVE] request to leave the chat\n");
        //nioEngine.generateMessage();
        nioEngine.message = new String(" ");
        nioEngine.typeOfMessage = NioEngine.MESSAGE_TYPE_LEAVE;
        nioEngine.send(nioEngine.message.getBytes());
        try {
            nioEngine.channelToCommunicateWithServer.register(nioEngine.selector, SelectionKey.OP_WRITE);
        } catch (ClosedChannelException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        nioEngine.selector.wakeup();
    }

    @Override // register message type : 3
    public void who() throws ChatException {
        nioEngine.message = " ";
        gui.text.append("[WHO] requesting the list of clients who are connected\n");
        System.out.println("[SENDING] " + nioEngine.message + "\n");
        nioEngine.typeOfMessage = NioEngine.MESSAGE_TYPE_WHO;
        nioEngine.send(nioEngine.message.getBytes());
        try {
            nioEngine.channelToCommunicateWithServer.register(nioEngine.selector, SelectionKey.OP_WRITE);
        } catch (ClosedChannelException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        nioEngine.selector.wakeup();
    }

    @Override // register message type : 4
    public void sendMsg(String msg) throws ChatException {
        nioEngine.message = msg;
        gui.text.append("[SENDING] " + nioEngine.message + "\n");
        System.out.println("[SENDING] " + nioEngine.message + "\n");

        nioEngine.typeOfMessage = NioEngine.MESSAGE_TYPE_SENDMSG;
        nioEngine.send(nioEngine.message.getBytes());
        try {
            nioEngine.channelToCommunicateWithServer.register(nioEngine.selector, SelectionKey.OP_WRITE);
        } catch (ClosedChannelException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        nioEngine.selector.wakeup();
    }

    @Override // register message type : 5
    public void quit() throws ChatException {
        nioEngine.running = false;
        leave();
        gui.frame.dispose();
        try {
            nioEngine.channelToCommunicateWithServer.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void receivedCB(byte[] data, SocketChannel socketChannel) {
        if (nioEngine.typeOfMessage == NioEngine.MESSAGE_TYPE_SENDMSG) {
            gui.text.append("[RECEVED] " + new String(data) + "\n");
        } else if (nioEngine.typeOfMessage == NioEngine.MESSAGE_TYPE_REGISTER) {
            gui.text.append("[REGISTER] " + new String(data) + " has joined the chat!\n");
        } else if (nioEngine.typeOfMessage == NioEngine.MESSAGE_TYPE_LEAVE) {
            gui.text.append("[LEAVE] " + new String(data) + " leaved the chat!\n");
        } else if (nioEngine.typeOfMessage == NioEngine.MESSAGE_TYPE_WHO) {
            gui.text.append("[WHO] list of connected people returned : " + new String(data) + "\n");
        }
    }

    @Override
    public void run() {
        gui = new ChatGUI();
        gui.setHandler(this);
    }
}
