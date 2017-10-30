import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message){
        System.out.println(message);
    }
    public static void writeError(String message){
        System.err.println(message);
    }

    public static String readString(){

        while (true) {
            try{
                return bufferedReader.readLine();
            }catch (IOException e){
                ConsoleHelper.writeError(e.getMessage());
            }
        }
    }

    public static int readInt(){
        while (true) {
            try{
                return Integer.parseInt(readString());
            }catch (NumberFormatException e){
                ConsoleHelper.writeError(e.getMessage());
            }
        }
    }
}
