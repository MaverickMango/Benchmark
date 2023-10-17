package root.generation.helper;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public class Helper {

    public static URL[] getURLs(Collection<String> paths) throws MalformedURLException {
        URL[] urls = new URL[paths.size()];
        int i = 0;
        for (String path : paths) {
            File file = new File(path);
            URL url = file.toURI().toURL();
            urls[i++] = url;
        }

        return urls;
    }

    public static boolean isAbstractClass(Class<?> target) {
        int mod = target.getModifiers();
        boolean isAbstract = Modifier.isAbstract(mod);
        boolean isInterface = Modifier.isInterface(mod);

        if (isAbstract || isInterface)
            return true;

        return false;
    }
}

