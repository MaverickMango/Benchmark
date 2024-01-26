package res1;

import java.io.*;

public class FileRead {
    public void read(File file) throws IOException {
        StringBuffer strbuf = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader(file));
        String str ;
        //...
        while ((str = in.readLine()) != null) {
            //...
            strbuf.append(str + "\n");
            //...
        }
        //...
        if (strbuf.length() > 0) {
            System.out.println(strbuf.toString());
        }
        in.close();
    }
}
