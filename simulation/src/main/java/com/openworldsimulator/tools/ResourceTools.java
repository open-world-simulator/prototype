package com.openworldsimulator.tools;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResourceTools {

    public static List<String> getResourceFolderFiles(String folder) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(folder);
        String path = url.getPath();
        List<String> results = new ArrayList<>();

        for (File f : new File(path).listFiles()) {
            results.add(f.getName());
        }
        return results;
    }
}
