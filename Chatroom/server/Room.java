package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Math;

public class Room implements AutoCloseable {
    private static SocketServer server;// used to refer to accessible server functions
    private String name;
    private final static Logger log = Logger.getLogger(Room.class.getName());

    // Commands
    private final static String COMMAND_TRIGGER = "/";
    private final static String CREATE_ROOM = "createroom";
    private final static String JOIN_ROOM = "joinroom";
    private final static String ROLL_DICE = "roll";
    private final static String FLIP_COIN = "flip";
    private final static String MUTE = "mute";
    private final static String UNMUTE = "unmute";
    
    private final static String ITALIC = "%%";
    private final static String BOLD = "!!";
    private final static String UNDERLINE = "__";
    private final static String STRIKE = "~~";
    
    private final static String PRIVATE = "@";
    

    public Room(String name) {
	this.name = name;
    }

    public static void setServer(SocketServer server) {
	Room.server = server;
    }

    public String getName() {
	return name;
    }

    private List<ServerThread> clients = new ArrayList<ServerThread>();

    protected synchronized void addClient(ServerThread client) {
	client.setCurrentRoom(this);
	if (clients.indexOf(client) > -1) {
	    log.log(Level.INFO, "Attempting to add a client that already exists");
	}
	else {
	    clients.add(client);
	    if (client.getClientName() != null) {
		client.sendClearList();
		sendConnectionStatus(client, true, "joined the room " + getName());
		updateClientList(client);
	    }
	}
    }

    private void updateClientList(ServerThread client) {
	Iterator<ServerThread> iter = clients.iterator();
	while (iter.hasNext()) {
	    ServerThread c = iter.next();
	    if (c != client) {
		boolean messageSent = client.sendConnectionStatus(c.getClientName(), true, null);
	    }
	}
    }

    protected synchronized void removeClient(ServerThread client) {
	clients.remove(client);
	if (clients.size() > 0) {
	    // sendMessage(client, "left the room");
	    sendConnectionStatus(client, false, "left the room " + getName());
	}
	else {
	    cleanupEmptyRoom();
	}
    }

    private void cleanupEmptyRoom() {
	// If name is null it's already been closed. And don't close the Lobby
	if (name == null || name.equalsIgnoreCase(SocketServer.LOBBY)) {
	    return;
	}
	try {
	    log.log(Level.INFO, "Closing empty room: " + name);
	    close();
	}
	catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    protected void joinRoom(String room, ServerThread client) {
	server.joinRoom(room, client);
    }

    protected void joinLobby(ServerThread client) {
	server.joinLobby(client);
    }

    /***
     * Helper function to process messages to trigger different functionality.
     * 
     * @param message The original message being sent
     * @param client  The sender of the message (since they'll be the ones
     *                triggering the actions)
     */
    private boolean processCommands(String message, ServerThread client) {
	boolean wasCommand = false;
	try {
	    if (message.indexOf(COMMAND_TRIGGER) > -1) {
				String[] comm = message.split(COMMAND_TRIGGER);
				log.log(Level.INFO, message);
				String part1 = comm[1];
				String[] comm2 = part1.split(" ");
				String command = comm2[0];
			if (command != null) {
			    command = command.toLowerCase();
			}
			String roomName;
			switch (command) {
			case CREATE_ROOM:
			    roomName = comm2[1];
			    if (server.createNewRoom(roomName)) {
			    	joinRoom(roomName, client);
			    }
			    wasCommand = true;
			    break;
			case JOIN_ROOM:
			    roomName = comm2[1];
			    joinRoom(roomName, client);
			    wasCommand = true;
			    break;
			case ROLL_DICE:
				int roll = (int)(Math.random() * 6) + 1;
				log.log(Level.INFO, client.getClientName() + "rolled a " + roll);
				String rollMes = "has rolled a " + roll;
				sendMessageHelper(client, "/roll");
				rollMes = "<b style=\"color:blue;\">" + rollMes + "</b>";
				sendMessageHelper(client, rollMes);
				wasCommand = true;
				break;
			case FLIP_COIN:
				int flip = (int)(Math.random() * 2);
				if (flip  == 0) {
					log.log(Level.INFO, client.getClientName() + " landed on heads");
					sendMessageHelper(client, "/flip");
					String headMes = "<b style=\"color:green;\">" + "has landed on heads" + "</b>";
					sendMessageHelper(client, headMes);
				}
				else {
					log.log(Level.INFO, client.getClientName() + " landed on tails");
					sendMessageHelper(client, "/flip");
					String tailMes = "<b style=\"color:red;\">" + "has landed on tails" + "</b>";
					sendMessageHelper(client, tailMes);
				}
				wasCommand = true;
				break;
			case MUTE:
			    //client.addMuted("hello");
			    break;
			case UNMUTE:
			    roomName = comm2[1];
			    joinRoom(roomName, client);
			    wasCommand = true;
			    break;
			}
	    }
	}
		catch (Exception e) {
		    e.printStackTrace();
		}
		return wasCommand;
    }
    
    // TODO add function similar to processCommands to check for special characters around words that can symbolize bold, italic, underline, etc...
    private boolean checkFormat(String message, ServerThread client) {
    	boolean wasFormat = false;
    	try {
    		String newMes = message;
    		if(newMes.indexOf(ITALIC) > -1 || newMes.indexOf(BOLD) > -1 || newMes.indexOf(UNDERLINE) > -1 || newMes.indexOf(STRIKE) > -1) {
    			wasFormat = true;
	    		if (newMes.indexOf(ITALIC) > -1) {
	    			
	    			String tempArr[] = newMes.split("%%");
	    			newMes = "";
	    			for(int i = 0; i < tempArr.length; i++) {
	    				if (i % 2 == 0) {
	    					newMes = newMes + tempArr[i]; 
	    				}
	    				else {
	    					newMes = newMes + "<i>" + tempArr[i] + "</i>";
	    				}
	    			}
	    		}
	    		if (newMes.indexOf(BOLD) > -1) {
	    			
	    			String tempArr[] = newMes.split("!!");
	    			newMes = "";
	    			for(int i = 0; i < tempArr.length; i++) {
	    				if (i % 2 == 0) {
	    					newMes = newMes + tempArr[i]; 
	    				}
	    				else {
	    					newMes = newMes + "<b>" + tempArr[i] + "</b>";
	    				}
	    			}
	    		}
	    		if (message.indexOf(UNDERLINE) > -1) {
	    			
	    			String tempArr[] = newMes.split("__");
	    			newMes = "";
	    			for(int i = 0; i < tempArr.length; i++) {
	    				if (i % 2 == 0) {
	    					newMes = newMes + tempArr[i]; 
	    				}
	    				else {
	    					newMes = newMes + "<u>" + tempArr[i] + "</u>";
	    				}
	    			}
	    		}
	    		if (message.indexOf(STRIKE) > -1) {
	    			
	    			String tempArr[] = newMes.split("~~");
	    			newMes = "";
	    			for(int i = 0; i < tempArr.length; i++) {
	    				if (i % 2 == 0) {
	    					newMes = newMes + tempArr[i]; 
	    				}
	    				else {
	    					newMes = newMes + "<strike>" + tempArr[i] + "</strike>";
	    				}
	    			}
	    		}
    			
    			if (message.substring(0, 1).equals("@")){
    				sendMessagePrivate(client, newMes);
    			}
    			else {
    				sendMessageHelper(client, newMes);
    			}
    			
    		}
    	}
    	catch (Exception e) {
		    e.printStackTrace();
		}
    	return wasFormat;
    }
    
    public boolean checkPrivate(String message, ServerThread client) {
    	boolean wasPrivate = false;
    	try {
    		if(message.indexOf(PRIVATE) == 0) {
    			wasPrivate = true;		
    		}
    	}
    	catch (Exception e) {
		    e.printStackTrace();
		}
    	return wasPrivate;
    }

    // TODO changed from string to ServerThread
    protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
		    ServerThread c = iter.next();
		    boolean messageSent = c.sendConnectionStatus(client.getClientName(), isConnect, message);
		    if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + c.getId());
			}
		}
    }

    /***
     * Takes a sender and a message and broadcasts the message to all clients in
     * this room. Client is mostly passed for command purposes but we can also use
     * it to extract other client info.
     * 
     * @param sender  The client sending the message
     * @param message The message to broadcast inside the room
     */
    protected void sendMessage(ServerThread sender, String message) {
	log.log(Level.INFO, getName() + ": Sending message to " + clients.size() + " clients");
		if (processCommands(message, sender)) {
		    // it was a command, don't broadcast
		    return;
		}
		if (checkFormat(message, sender)) {
		    // it was a special format, broadcast happened in the checkFormat method
		    return;
		}
		if (checkPrivate(message, sender)) {
		    // private message, message sent here
			sendMessagePrivate(sender, message);
		    return;
		}
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
		    ServerThread client = iter.next();
		    boolean messageSent = client.send(sender.getClientName(), message);
		    if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + client.getId());
		    }
		}
    }
    
    //this method just sends the messages to all in current room without checking commands,... etc
    protected void sendMessageHelper(ServerThread sender, String message) {
    	Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
		    ServerThread client = iter.next();
		    boolean messageSent = client.send(sender.getClientName(), message);
		    if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + client.getId());
		    }
		}
    }
    
    protected void sendMessagePrivate(ServerThread sender, String message) {
    	sender.send(sender.getClientName(), message);
		
		String arrTarget[] = message.split(" ");
		String target = arrTarget[0].substring(1, arrTarget[0].length());
		Iterator<ServerThread> iter = clients.iterator();
		while (iter.hasNext()) {
			ServerThread client = iter.next();
			if (client.getClientName().equals(target)) {
			    boolean messageSent = client.send(sender.getClientName(), message);
			    if (!messageSent) {
					iter.remove();
					log.log(Level.INFO, "Removed client " + client.getId());
			    }
			}
		}
    }
    

    /***
     * Will attempt to migrate any remaining clients to the Lobby room. Will then
     * set references to null and should be eligible for garbage collection
     */
    @Override
    public void close() throws Exception {
	int clientCount = clients.size();
	if (clientCount > 0) {
	    log.log(Level.INFO, "Migrating " + clients.size() + " to Lobby");
	    Iterator<ServerThread> iter = clients.iterator();
	    Room lobby = server.getLobby();
	    while (iter.hasNext()) {
		ServerThread client = iter.next();
		lobby.addClient(client);
		iter.remove();
	    }
	    log.log(Level.INFO, "Done Migrating " + clients.size() + " to Lobby");
	}
	server.cleanupRoom(this);
	name = null;
	// should be eligible for garbage collection now
    }

}