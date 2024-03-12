package root.entities;

import root.util.FileUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBeanClazz {

    /**
     * Get all attributes as map with name and value
     * @param instance the instance of a special class
     * @return a map of attributes
     * @param <T> a subclass extends AbstractBeanClazz
     */
    public <T extends AbstractBeanClazz> Map<String, Object> getMembersMap(T instance) {
        Map<String, Object> map = new HashMap<>();
        try{
            Field[] fields = instance.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(instance);
                map.put(name,value);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return map;
    }

    @Override
    public String toString() {
        return FileUtils.bean2Json(this);
    }
}
