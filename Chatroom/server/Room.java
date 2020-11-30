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
    
    private final static String START_ITALIC = " **";
    private final static String END_ITALIC = "** ";
    private final static String START_BOLD = " $$";
    private final static String END_BOLD = "$$ ";
    private final static String START_UNDERLINE = " __";
    private final static String END_UNDERLINE = "__ ";
    private final static String START_STRIKE = " ~~";
    private final static String END_STRIKE = "~~ ";

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
				client.send(client.getClientName(), "/roll");
				client.send(client.getClientName(), rollMes);
				wasCommand = true;
				break;
			case FLIP_COIN:
				int flip = (int)(Math.random() * 2) + 1;
				if (flip  == 0) {
					log.log(Level.INFO, client.getClientName() + " landed on heads");
					client.send(client.getClientName(), "/flip");
					client.send(client.getClientName(), "has landed on heads");
				}
				else {
					log.log(Level.INFO, client.getClientName() + " landed on tails");
					client.send(client.getClientName(), "/flip");
					client.send(client.getClientName(), "has landed on tails");
				}
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
    		if (message.indexOf(START_ITALIC) > -1 && message.indexOf(END_ITALIC) > -1) {
    			String partOne = message.substring(0, message.indexOf(START_ITALIC) + 1);
    			String partTwo = message.substring(message.indexOf(START_ITALIC) + 3, message.indexOf(END_ITALIC));
    			String partThree = message.substring(message.indexOf(END_ITALIC) + 2, message.length());
    			String formattedMes = partOne + "(ITALIC)" + partTwo + "(ITALIC)" + partThree;
    			client.send(client.getClientName(), formattedMes);
    			wasFormat = true;
    		}
    		if (message.indexOf(START_BOLD) > -1 && message.indexOf(END_BOLD) > -1) {
    			String partOne = message.substring(0, message.indexOf(START_BOLD) + 1);
    			String partTwo = message.substring(message.indexOf(START_BOLD) + 3, message.indexOf(END_BOLD));
    			String partThree = message.substring(message.indexOf(END_BOLD) + 2, message.length());
    			String formattedMes = partOne + "(BOLD)" + partTwo + "(BOLD)" + partThree;
    			client.send(client.getClientName(), formattedMes);
    			wasFormat = true;
    		}
    		if (message.indexOf(START_UNDERLINE) > -1 && message.indexOf(END_UNDERLINE) > -1) {
    			String partOne = message.substring(0, message.indexOf(START_UNDERLINE) + 1);
    			String partTwo = message.substring(message.indexOf(START_UNDERLINE) + 3, message.indexOf(END_UNDERLINE));
    			String partThree = message.substring(message.indexOf(END_UNDERLINE) + 2, message.length());
    			String formattedMes = partOne + "(UNDERLINE)" + partTwo + "(UNDERLINE)" + partThree;
    			client.send(client.getClientName(), formattedMes);
    			wasFormat = true;
    		}
    		if (message.indexOf(START_STRIKE) > -1 && message.indexOf(END_STRIKE) > -1) {
    			String partOne = message.substring(0, message.indexOf(START_STRIKE) + 1);
    			String partTwo = message.substring(message.indexOf(START_STRIKE) + 3, message.indexOf(END_STRIKE));
    			String partThree = message.substring(message.indexOf(END_STRIKE) + 2, message.length());
    			String formattedMes = partOne + "(STRIKE)" + partTwo + "(STRIKE)" + partThree;
    			client.send(client.getClientName(), formattedMes);
    			wasFormat = true;
    		}
    	}
    	catch (Exception e) {
		    e.printStackTrace();
		}
    	return wasFormat;
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