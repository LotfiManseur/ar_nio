package chat;


import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatGUI {

  public TextArea text;
  public TextField data, dataconnect;
  public Frame frame;
  I_ChatClient client;


  public ChatGUI() {
    frame = new Frame();
    frame.setLayout(new FlowLayout());
    
    text = new TextArea(10, 40);
    text.setEditable(false);
    text.setForeground(Color.orange);
    frame.add(text);

    data = new TextField(50);
    frame.add(data);

    Button enter_button = new Button("enter");
    enter_button.addActionListener(new enterListener(this));
    frame.add(enter_button);
    
    Button write_button = new Button("write");
    write_button.addActionListener(new writeListener(this));
    frame.add(write_button);

    Button who_button = new Button("who");
    who_button.addActionListener(new whoListener(this));
    frame.add(who_button);

    Button leave_button = new Button("leave");
    leave_button.addActionListener(new leaveListener(this));
    frame.add(leave_button);
    
    Button quit_button = new Button("quit");
    quit_button.addActionListener(new quitListener(this));
    frame.add(quit_button);

    frame.setSize(500, 300);
    text.setBackground(Color.black);
    frame.show();
  }

  /**
   * Set the handler for this chat.
   */
  public void setHandler(I_ChatClient client) {
    this.client = client;
  }

  /**
   * Prints a message in the message window.
   */
  public void printMsg(String msg) {
    try {
      this.text.append(msg + "\n");
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
  }


  /**
   * GUI event-handlers
   */
  class enterListener implements ActionListener {
    ChatGUI gui;
    public enterListener(ChatGUI i) {
    	gui = i;
    }
    public void actionPerformed(ActionEvent e) {
      try {
        client.register(gui.data.getText());
        gui.data.setText("");
      } catch (Exception ex) {}
    }
  }

  class writeListener implements ActionListener {
    ChatGUI gui;
    public writeListener(ChatGUI i) {
    	gui = i;
    }
    public void actionPerformed(ActionEvent e) {
      try {
        client.sendMsg(gui.data.getText());
        gui.data.setText("");
      } catch (Exception ex) {}
    }
  }

  class whoListener implements ActionListener {
    ChatGUI gui;
    public whoListener(ChatGUI i) {
    	gui = i;
    }
    public void actionPerformed(ActionEvent e) {
      try {
        client.who();   
      } catch (Exception ex) {}
    }
  }

  class leaveListener implements ActionListener {
    ChatGUI gui;
    public leaveListener(ChatGUI i) {
    	gui = i;
    }
    public void actionPerformed(ActionEvent e) {
      try {
        client.leave();
      } catch (Exception ex) {}
    } 
  }
  
  class quitListener implements ActionListener {
	    ChatGUI gui;
	    public quitListener(ChatGUI i) {
	    	gui = i;
	    }
	    public void actionPerformed(ActionEvent e) {
	      try {
	    	client.quit();
	      } catch (Exception ex) {}
	    } 
	  }
}

