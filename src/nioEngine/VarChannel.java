/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nioEngine;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 * @author root
 */
// servira pour le multi client !
// Le serveur doit avoir une hashmap <channel> -> <VarChannel> 
// A chanque fois qu'un channel n'existe pas, créé un VarChannel et l'ajoute dans la hashmap, l'ajoute
// Quand le serveur reçoit un evenement (key), charge dans les variables locales inBuffer, outBuffer et etat
// les valeurs recuperees depuis la hashmap.
public class VarChannel {

    ByteBuffer inBuffer;
    ByteBuffer outBuffer;
    SocketChannel socketChannel;
    EtatReception etat;
    String name;
    //Boolean activated;

    public VarChannel(SocketChannel socketChannel, String name) {
        this.inBuffer = ByteBuffer.allocate(4);
        this.outBuffer = ByteBuffer.allocate(4);
        this.etat = etat.RECEPTION_TAILLE;
        this.socketChannel = socketChannel;
        this.name = name;
       // activated = true;
    }
}
