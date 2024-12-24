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
        System.out.print("$ ");

        try (Scanner scanner = new Scanner(System.in)) {
            String input;
            Set<String> builtIn = Set.of("echo","exit","type","pwd","cd");
            String currDir = System.getProperty("user.dir");

            while((input = scanner.nextLine())!=null){
                String cmd = input.split(" ")[0];
                String param = "";
                if(input.length()>cmd.length()){
                    param = input.substring(cmd.length()+1);
                }

                if(cmd.equals("exit") && param.equals("0")){
                    break;
                }
                else if(cmd.equals("echo")){
                    System.out.println(param);
                }
                else if(cmd.equals("type")){
                    if(builtIn.contains(param)){
                        System.out.println(param + " is a shell builtin");
                    }
                    else{
                        String path = getPath(param);
                        if(path != null){
                            System.out.println(param + " is " + path);
                        }
                        else{
                            System.out.println(param + ": not found");
                        }
                    }
                }
                else if(cmd.equals("pwd")){
                    System.out.println(currDir);
                }
                else if(cmd.equals("cd")){
                    if(param.equals("~")){
                        currDir = System.getenv("HOME");
                    }
                    else{
                        if(!param.startsWith("/")){
                            param = currDir + "/" + param;
                        }
    
                        if(Files.isDirectory(Path.of(param))){
                            currDir = Path.of(param).normalize().toString();
                        }
                        else{
                            System.out.println("cd: " + param + ": No such file or directory");
                        }
                    }
                }
                else{
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
}
