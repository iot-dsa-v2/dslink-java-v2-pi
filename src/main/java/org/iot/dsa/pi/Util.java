package org.iot.dsa.pi;

import java.util.HashMap;
import java.util.Map;

import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSValueType;


public class Util {
	
	public static DSMap makeColumn(String name, DSValueType type) {
		return new DSMetadata().setName(name).setType(type).getMap();
	}
	
	public static Map<String, String> dsMapToMap(DSMap dsMap) {
		Map<String, String> map = new HashMap<String, String>();
		for(int i=0; i<dsMap.size(); i++) {
			Entry en = dsMap.getEntry(i);
			map.put(en.getKey(), en.getValue().toString());
		}
		return map;
	}
	
	public static String nameToPath(String name) {
		return name.toLowerCase().replaceAll("\\s","");
	}
	
	public static DSMap parseJsonMap(String s) {
		JsonReader reader = new JsonReader(s);
		DSMap m = reader.getMap();
		reader.close();
		return m;
	}
}
