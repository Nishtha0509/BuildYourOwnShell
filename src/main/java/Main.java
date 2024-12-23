import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;

public class Main {

    private static String getPath(String input){
        for(String path : System.getenv("PATH").split(":")){
            Path fullPath = Path.of(path,input);
            if (Files.isRegularFile(fullPath)) {
                return fullPath.toString();
            }
        }
        return null;
    } 
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
        System.out.print("$ ");

        Scanner scanner = new Scanner(System.in);
        String input;
        Set<String> builtIn = Set.of("echo","exit","type");
        while((input = scanner.nextLine())!=null){
            if(input.contains("exit 0")){
                break;
            }
            else if(input.startsWith("echo")){
                System.err.println(input.substring(5));
            }
            else if(input.startsWith("type")){
                String param = input.substring(5);
                if(builtIn.contains(param)){
                    System.err.println(param + " is a shell builtin");
                }
                else{
                    String path = getPath(param);
                    if(path != null){
                        System.err.println(param + " is " + path);
                    }
                    else{
                        System.out.println(param + ": not found");
                    }
                }
            }
            else{
                String cmd = input.split(" ")[0];
                String path = getPath(cmd);
                if(path != null){
                    String fullPath = path + input.substring(cmd.length());
                    Process p = Runtime.getRuntime().exec(fullPath.split(" "));
                    p.getInputStream().transferTo(System.out);
                }
                else{
                    System.out.println(input + ": command not found");
                }
            }
            System.out.print("$ ");
        }
    }
}
