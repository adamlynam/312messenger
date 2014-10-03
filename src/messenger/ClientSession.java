/*
 * ClientSession.java
 *
 * Created on 30 March 2005, 13:55
 */

package messenger;
import java.io.*;
import java.net.*;

/**
 *
 * @author Mad_Fool
 */
public class ClientSession extends Thread
{
    public Socket clientSocket;
    private Server host;
    public String username;
    
    /** Creates a new instance of ClientSession */
    public ClientSession(Socket newClientSocket, Server newHost)
    {
        clientSocket = newClientSocket;
        host = newHost;
        username = "default user";
    }
    
    public void run()
    {
        try
        {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
            
            do
            {
                output.println("Please enter a username consisting of letters and numbers only");
                username = input.readLine();
            }while(!username.matches("^([A-z]|[0-9])([A-z]|[0-9])*$"));
                
            output.println("Your name for this session will be " + username);
            host.joining(this);
            output.println("You can begin chatting now");
            
            String message = input.readLine();
            while(message.compareTo("/quit") != 0)
            {
                host.textFromClient(message, username);
                message = input.readLine();
            }
            
            output.println("Ending chat");
            host.leaving(this);
            output.close();
            input.close();
            clientSocket.close();
        }
        catch (IOException e)
        {
            System.out.println("Couldnt create IO reader/writer");
        }
    }
}
