package chat;

/**
 * The IChatClient interface defines the callback methods
 * for the GUI of the chat application (see @code ChatGUI}
 */

public interface I_ChatClient {

	  public void register (String clientName) throws ChatException;

	  public void leave () throws ChatException;
	  
	  public void who () throws ChatException;

	  public void sendMsg (String msg) throws ChatException;
	  
	  public void quit() throws ChatException;

}
