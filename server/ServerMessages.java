import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerMessages extends Thread {

    public String msg;
    private ArrayList<ServerThread> clients;

    ServerMessages(ArrayList<ServerThread> clients) {
        this.msg = "";
        this.clients = clients;
    }

    public boolean sendToClient(ServerThread client, Object msg) {
        if(client.getOnlineStatus() == true) {
            try {
                ObjectOutputStream out = client.getObjectOutputStream();
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                System.out.println("IOExeption occurred in sendServerMessage()" + e);
            }
            return true;
        }
        return false;
    }

    public boolean sentToClient(String usernName, Object msg) {
        ServerThread client = null; 
        for (ServerThread cl: clients) {
            if (cl.userName.equals(usernName)) {
                client = cl;
            }
        }

        if (client == null) {
            return false;
        }

        if(client.getOnlineStatus() == true) {
            try {
                ObjectOutputStream out = client.getObjectOutputStream();
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                System.out.println("IOExeption occurred in sendServerMessage()" + e);
            }
            return true;
        }
        return false;
    }

    public void sendToAllClients(Object msg) {
        System.out.println(clients != null);
        System.out.println(clients.isEmpty());
        for(ServerThread sT: clients) {
            sendToClient(sT, msg);
        }
        System.out.println("send to all Clients... done");
    }

    public void sendToRoom(String roomName, ArrayList<Room> rooms, Object msg) {
        Room room = null;
        for (Room r: rooms) {
            if (r.getName().equals(roomName)) {
                room = r;
            }
        }
        if (room == null) {
            System.out.println("Error: Room not found");
            return;
        }
        for (ServerThread sT: room.getUserList()) {
            sendToClient(sT, msg);
        }
    }

    public void removeUserFromRoom(String userName, String roomName, ArrayList<Room> rooms) {
        Room room = null;
        for (Iterator<Room> it = rooms.iterator(); it.hasNext();) {
            Room r = it.next();
            if (r.getName().equals(roomName)) {
                room = r;
            }
        }
        if (room == null) {
            System.out.println("Error: Room not found");
            return;
        }
        room.removeUser(userName);

    }

    public String generateUserList(ArrayList<ServerThread> clients) {
        if (clients == null) {
            return "";
        }
    
        StringBuilder users = new StringBuilder("Online:\n");
        int userNumber = 0;
        for (ServerThread sT : clients) {
            if (sT.getOnlineStatus()) {
                userNumber++;
                users.append(userNumber).append(". [").append(sT.userName).append("]\n");
            }
        }
        if (userNumber != clients.size()) {
            users.append("Offline:\n");
            for (ServerThread sT : clients) {
                if (!sT.getOnlineStatus()) {
                    userNumber++;
                    users.append(userNumber).append(". [").append(sT.userName).append("]\n");
                }
            }
        }
        return users.toString();    
    }

    public String gernerateUserListWithRoom(ArrayList<ServerThread> clients) {
        if (clients == null) {
            return "";
        }
    
        StringBuilder users = new StringBuilder("Online:\n");
        int userNumber = 0;
        for (ServerThread sT : clients) {
            if (sT.getOnlineStatus()) {
                userNumber++;
                users.append(userNumber).append(". [").append(sT.userName).append("]");
                if (sT.getRoomName().equals("")) {
                    users.append("\n");
                } else {
                    users.append(" @" + sT.getRoomName());
                }
            }
        }
        if (userNumber != clients.size()) {
            users.append("Offline:\n");
            for (ServerThread sT : clients) {
                if (!sT.getOnlineStatus()) {
                    userNumber++;
                    users.append(userNumber).append(". [").append(sT.userName).append("]\n");
                }
            }
        }
        return users.toString();
    }

    public String generateRoomList(ArrayList<Room> rooms) {
       
        StringBuilder roomList = new StringBuilder();
        
        for (Room room: rooms) {
            roomList.append(room.getName() + " (" + room.size() + " User)\n");
        }

        return roomList.toString();
    }

    public void setClientList(ArrayList<ServerThread> clinets) {
        this.clients = clinets;
    }

    public void sendToClient(String userName, String message) {
    }
}

