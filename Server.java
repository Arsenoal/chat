import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        int serverPort = ConsoleHelper.readInt();
        try (ServerSocket serverSocket= new ServerSocket(serverPort)){
            ConsoleHelper.writeMessage("server is running");

            while (true){
                new Handler(serverSocket.accept()).start();
            }
        }catch (Exception e){
            ConsoleHelper.writeMessage("something went wrong, server socket closed");
        }
    }

    public static void sendBroadcastMessage(Message message){
        for(Map.Entry<String, Connection> entry: connectionMap.entrySet()){
            try {
                entry.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("cannot send message");
            }
        }
    }

    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{

            while (true){
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message clientResponse = connection.receive();

                if(clientResponse.getType().equals(MessageType.USER_NAME)){
                    String userName = clientResponse.getData();
                    if(!userName.isEmpty() && !connectionMap.containsKey(userName)){
                        connectionMap.put(userName, connection);

                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return userName;
                    }

                }
            }
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException{
            for(Map.Entry<String, Connection> entry: connectionMap.entrySet()){
                if(!userName.equals(entry.getKey())) {
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
                }

            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{

            while (true) {
                Message receiveMessage = connection.receive();
                if (receiveMessage != null && receiveMessage.getType() == MessageType.TEXT) {
                    sendBroadcastMessage(new Message(MessageType.TEXT, String.format("%s: %s", userName, receiveMessage.getData())));
                } else {
                    ConsoleHelper.writeMessage("it is not a text you are sending");
                }
            }
        }

        @Override
        public void run(){
            super.run();

            if(socket != null && socket.getRemoteSocketAddress() != null){
                ConsoleHelper.writeMessage("connection established: " + socket.getRemoteSocketAddress());
            }

            String newUserName = null;

            try(Connection connection = new Connection(socket)) {
                newUserName = serverHandshake(connection);

                sendBroadcastMessage(new Message(MessageType.USER_ADDED, newUserName));

                sendListOfUsers(connection, newUserName);

                serverMainLoop(connection, newUserName);

            }catch (IOException | ClassNotFoundException e){
                ConsoleHelper.writeMessage("error with remote socket address");
            }finally {
                if (newUserName != null) {
                    connectionMap.remove(newUserName);

                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, newUserName));
                }

                ConsoleHelper.writeMessage("connection is closed");
            }
        }
    }
}
