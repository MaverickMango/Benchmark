import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileRead2 {
    public void read(File file) throws IOException {
        StringBuffer strbuf = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader(file));
        String str ;
        //...
        while ((str = in.readLine()) != null) {
            //...
            strbuf.append(str);
            //...
        }
        //...
        if (strbuf.length() > 0 && strbuf.substring(strbuf.length() - 1, strbuf.length()).equals("")) {
            System.out.println(strbuf);
        }
        in.close();
    }
}
