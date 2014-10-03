/*
 * Server.java
 *
 * Created on 30 March 2005, 13:38
 */

package messenger;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Mad_Fool
 */
public class Server {
    
    ArrayList clientSessions;
    
    /** Creates a new instance of Server */
    public Server()
    {
        clientSessions = new ArrayList();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        Server chatServer = new Server();
        
        chatServer.run();
    }
    
    public void run()
    {
        ServerSocket myserver = null;
        try
        {
            myserver = new ServerSocket(30100);
        }
        catch (IOException e)
        {
            System.out.println("Could not listen on port 30100");
            System.exit(1);
        }

        Socket clientSocket = null;

        while(true)
        {
            try
            {
                clientSocket = myserver.accept();
                ClientSession newClient = new ClientSession(clientSocket, this);
                newClient.start();
                clientSessions.add(newClient);
            }
            catch (IOException e)
            {
                System.out.println("Unable to accept connection at this time");
            }
        }
    }

    public void joining(ClientSession currentSession)
    {
        sendToAll(currentSession.username + " has joined the server.");
    }
    
    public void leaving(ClientSession currentSession)
    {
        sendToAll(currentSession.username + " has left the server.");
        clientSessions.remove(currentSession);
    }
    
    public void textFromClient(String message, String username)
    {
        if (message.startsWith("/"))
        {
            if(message.startsWith("/whisper"))
            {
                message = message.substring(9);
                int spaceAfterUserName = 0;
                while(message.charAt(spaceAfterUserName) != ' ')
                {
                    spaceAfterUserName++;
                }
                String whisperUsername = message.substring(0, spaceAfterUserName);
                message = message.substring(spaceAfterUserName + 1);
                whisperMessage(message, username, whisperUsername);
            }
            else if(message.startsWith("/list"))
            {
                message = "Users connected are : ";
                
                Iterator currentClient = clientSessions.iterator();
                while(currentClient.hasNext())
                {
                    message = message + ((ClientSession)currentClient.next()).username;
                    
                   if (currentClient.hasNext())
                   {
                        message = message + ", ";
                   }
                }
                
                whisperMessage(message, "ChatServer", username);
            }
        }
        else
        {
            sendToAll(username + " : " + message);
        }
    }
    
    public void whisperMessage(String message, String username, String whisperUsername)
    {
        boolean sentOk = false;
        
        Iterator currentClient = clientSessions.iterator();
        while(currentClient.hasNext())
        {
            ClientSession thisClient = (ClientSession)currentClient.next();
            if(thisClient.username.compareTo(whisperUsername) == 0)
            {
                try
                {
                    PrintWriter output = new PrintWriter(thisClient.clientSocket.getOutputStream(), true);
                    output.println("Whisper from " + username + " : " + message);
                    sentOk = true;
                }
                catch (IOException e)
                {
                    sentOk = false;
                }
            }
        }
        
        currentClient = clientSessions.iterator();
        while(currentClient.hasNext())
        {
            ClientSession thisClient = (ClientSession)currentClient.next();
            if(thisClient.username.compareTo(username) == 0)
            {
                try
                {
                    PrintWriter output = new PrintWriter(thisClient.clientSocket.getOutputStream(), true);
                    if(sentOk)
                    {
                        output.println("Whisper to " + whisperUsername + " sent");
                    }
                    else
                    {
                        output.println("Whisper to " + whisperUsername + " failed, may be invalid username");
                    }
                    return;
                }
                catch (IOException e)
                {

                }
            }
        }
    }
    
    public void sendToAll(String message)
    {
        Iterator currentClient = clientSessions.iterator();
        while(currentClient.hasNext())
        {
            try
            {
                PrintWriter output = new PrintWriter(((ClientSession)currentClient.next()).clientSocket.getOutputStream(), true);
                output.println(message);
            }
            catch (IOException e)
            {
                
            }
        }
    }
}
