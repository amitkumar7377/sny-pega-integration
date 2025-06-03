package com.org.pega;

import com.pega.pegarules.pub.runtime.ParameterPage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.beanutils.BeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomStep {
    public void performStep(ParameterPage parameters) {
        String userInput = parameters.getString("input");

        // Unsafe: LazyMap - known RCE vector
        Map<String, String> baseMap = new HashMap<>();
        Map lazyMap = LazyMap.decorate(baseMap, input -> "default");
        lazyMap.put("key", userInput);

        // Unsafe: Untrusted deserialization
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("data.ser"));
            Object obj = ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Unsafe: Jackson with unvalidated user input
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<?,?> userData = mapper.readValue(userInput, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Unsafe: File path injection
        try {
            File file = new File("/app/data/" + userInput);
            BufferedReader reader = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Unsafe: BeanUtils (can be used for unsafe reflective manipulation)
        try {
            BeanUtils.setProperty(this, "dynamicProperty", userInput);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Processed: " + lazyMap.get("key"));
    }
}
