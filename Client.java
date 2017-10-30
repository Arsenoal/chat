import java.io.IOException;
import java.net.Socket;

public class Client extends Thread{
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) {
        new Client().run();
    }

    protected String getServerAddress(){
        ConsoleHelper.writeMessage("input server address");

        return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        ConsoleHelper.writeMessage("input server port");

        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        ConsoleHelper.writeMessage("input your name");

        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

    protected SocketThread getSocketThread(){
        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            Message message = new Message(MessageType.TEXT, text);

            connection.send(message);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("something went wrong while sending message");
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread{
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("%s joined our chat", userName));
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("%s left us", userName));
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;

            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message receivingMessage = connection.receive();

                MessageType typeReceived = receivingMessage.getType();

                if(typeReceived == MessageType.NAME_REQUEST){
                    String userName = getUserName();

                    Message sendUserName = new Message(MessageType.USER_NAME, userName);
                    connection.send(sendUserName);
                }else if (typeReceived == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                }else{
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            Message receiveMessage;

            while (true) {
                receiveMessage = connection.receive();

                if(receiveMessage.getType() == MessageType.TEXT){
                    processIncomingMessage(receiveMessage.getData());
                }else if(receiveMessage.getType() == MessageType.USER_ADDED){
                    informAboutAddingNewUser(receiveMessage.getData());
                }else if(receiveMessage.getType() == MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(receiveMessage.getData());
                }else{
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        @Override
        public void run() {
            super.run();
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();

            try{
                Socket socket = new Socket(serverAddress, serverPort);
                connection = new Connection(socket);

                clientHandshake();
                clientMainLoop();
            }catch (Exception e){
                notifyConnectionStatusChanged(false);
            }
        }
    }

    @Override
    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        try {
            synchronized (this) {
                wait();
            }
        }catch (Exception e){
            ConsoleHelper.writeMessage("thread exception...");
            System.exit(1);
        }

        if(clientConnected){
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду ‘exit’");

            String input;

            while (clientConnected){
                input = ConsoleHelper.readString();

                if (input.equals("exit"))
                    break;

                if (shouldSendTextFromConsole())
                    sendTextMessage(input);
            }
        }else{
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
    }
}
