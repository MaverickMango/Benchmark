package root.script;

import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Morphia;

public class MongoDB {

    public static void main(String[] args) {
        Morphia morphia = new Morphia();
        morphia.mapPackage("de.ugoe.cs.smartshark.model");
        
        MongoClientURI uri = new MongoClientURI("mongodb://127.0.0.1:27017/?directConnection=true&serverSelectionTimeoutMS=2000");
    }
}
