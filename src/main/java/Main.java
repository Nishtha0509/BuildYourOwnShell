import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        try (Scanner scanner = new Scanner(System.in)) {
            String input;
            Set<String> builtIn = Set.of("echo","exit","type","pwd","cd","cat");
            String currDir = System.getProperty("user.dir");

            while((input = scanner.nextLine())!=null){
                String cmd = input.split(" ")[0];
                //System.out.println(cmd);
                String param = "";
                if(input.length()>cmd.length()){
                    param = input.substring(cmd.length()+1);
                }

                if(cmd.equals("exit") && param.equals("0")){
                    break;
                }
                else if (cmd.equals("echo")) {
                    if (param.startsWith("\"") && param.endsWith("\"")) {
                        param = param.substring(1, param.length() - 1); // Remove enclosing quotes
                        StringBuilder output = new StringBuilder();
                        boolean escape = false;
                
                        for (int i = 0; i < param.length(); i++) {
                            char c = param.charAt(i);
                
                            if (escape) {
                                switch (c) {
                                    case '\\':
                                        output.append('\\'); // Literal backslash
                                        break;
                                    case 'n':
                                        output.append('\n'); // Newline
                                        break;
                                    case '"':
                                        output.append('"'); // Escaped double quote
                                        break;
                                    case '$':
                                        output.append('$'); // Escaped dollar sign
                                        break;
                                    default:
                                        output.append('\\').append(c); // Preserve unknown escape sequences
                                        break;
                                }
                                escape = false;
                            } else if (c == '\\') {
                                escape = true; // Set escape flag
                            } else {
                                output.append(c);
                            }
                        }
                
                        // If the string ends with an unprocessed escape character
                        if (escape) {
                            output.append('\\');
                        }
                
                        param = output.toString();
                    } else if (param.startsWith("'") && param.endsWith("'")) {
                        // Single-quoted strings remain unchanged
                        param = param.substring(1, param.length() - 1);
                    } else {
                        // Non-quoted strings
                        param = param.replaceAll("\\s+", " ");
                        param = param.replaceAll("\\\\", "");
                    }
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
                else if(cmd.startsWith("cat")){
                    Pattern pattern=Pattern.compile("");
                    if(param.startsWith("\'"))
                        pattern = Pattern.compile("'(.*?)'");
                    else if(param.startsWith("\""))
                        pattern = Pattern.compile("\"(.*?)\"");
                    Matcher matcher = pattern.matcher(param);

                    List<String> params = new ArrayList<>();
                    while (matcher.find()) {
                        params.add(matcher.group(1));
                    }
                    StringBuilder output = new StringBuilder();
                    for (String arg : params) {
                        try {
                            String content = Files.readString(Paths.get(arg));
                            output.append(content);
                        } catch (NoSuchFileException e) {
                            System.out.println("cat: " + arg + ": No such file or directory");
                            return;
                        } catch (IOException e) {
                            System.out.println("cat: " + arg + ": An error occurred while reading the file");
                            return;
                        }
                    }
                    System.out.print(output.toString());
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
