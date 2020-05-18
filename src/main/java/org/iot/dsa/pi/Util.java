package org.iot.dsa.pi;

import java.util.HashMap;
import java.util.Map;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSValueType;


public class Util {

    public static Object dsElementToObject(DSElement element) {
        if (element.isBoolean()) {
            return element.toBoolean();
        } else if (element.isNumber()) {
            return element.toInt();
        } else if (element.isList()) {
            DSList dsl = element.toList();
            String[] arr = new String[dsl.size()];
            int i = 0;
            for (DSElement e : dsl) {
                arr[i] = e.toString();
                i++;
            }
            return arr;
        } else {
            return element.toString();
        }
    }

    public static Map<String, String> dsMapToMap(DSMap dsMap) {
        Map<String, String> map = new HashMap<String, String>();
        for (Entry en : dsMap) {
            map.put(en.getKey(), en.getValue().toString());
        }
        return map;
    }

    public static boolean getBool(DSNode n, String key) {
        DSIObject obj = n.get(key);
        return obj instanceof DSBool ? ((DSBool) obj).toBoolean() : false;
    }

    public static DSMap makeColumn(String name, DSValueType type) {
        return new DSMetadata().setName(name).setType(type).getMap();
    }

    public static String nameToPath(String name) {
        return name.toLowerCase().replaceAll("\\s", "");
    }

    public static DSMap parseJsonMap(String s) {
        JsonReader reader = new JsonReader(s);
        DSMap m = reader.getMap();
        reader.close();
        return m;
    }
}
