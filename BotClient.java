import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        new BotClient().run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return String.format("date_bot_%s", (int)(Math.random()*100));
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hey chat. I'm a bot. Commands I can understand: date, day, month, year, time, hour, minuets, seconds.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            if (message != null) {
                ConsoleHelper.writeMessage(message);
                SimpleDateFormat format = null;
                if (message.contains(": ")) {
                    String[] arr = message.split(": ");
                    if (arr.length == 2 && arr[1] != null) {
                        String name = arr[0];
                        String text = arr[1];
                        switch (text) {
                            case "дата":
                                format = new SimpleDateFormat("d.MM.YYYY");
                                break;
                            case "день":
                                format = new SimpleDateFormat("d");
                                break;
                            case "месяц":
                                format = new SimpleDateFormat("MMMM");
                                break;
                            case "год":
                                format = new SimpleDateFormat("YYYY");
                                break;
                            case "время":
                                format = new SimpleDateFormat("H:mm:ss");
                                break;
                            case "час":
                                format = new SimpleDateFormat("H");
                                break;
                            case "минуты":
                                format = new SimpleDateFormat("m");
                                break;
                            case "секунды":
                                format = new SimpleDateFormat("s");
                                break;

                        }
                        if (format != null) {
                            sendTextMessage(String.format("Информация для %s: %s", name, format.format(Calendar.getInstance().getTime())));
                        }
                    }
                }
            }
        }
    }
}
