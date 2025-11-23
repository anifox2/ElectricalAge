package mods.eln.misc;

import mods.eln.misc.Obj3D.Obj3DPart;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class used to load all eln models and corresponding obj files.
 */
public class Obj3DFolder {

    private Map<String, Obj3D> nameToObjHash = new HashMap<String, Obj3D>();

    /**
     * Load all obj models available in the release mod asset folder.
     */
    public void loadAllElnModels() {
        try {
            java.net.URL url = mods.eln.Eln.class.getResource("/assets/eln/model");
            if (url == null) {
                Utils.println("Could not find /assets/eln/model resource");
                return;
            }
            
            Utils.println("DEBUG: Model URL: " + url);
            
            if (url.getProtocol().equals("jar")) {
                String path = url.getPath(); // file:/path/to/jar!/assets/eln/model
                if (path.startsWith("file:")) {
                    path = path.substring(5);
                }
                String jarPath = path.substring(0, path.indexOf("!"));
                jarPath = URLDecoder.decode(jarPath, "UTF-8");
                
                Utils.println("Loading models from JAR: " + jarPath);
                JarFile jarFile = new JarFile(jarPath);
                Enumeration<JarEntry> entries = jarFile.entries();
                int modelCount = 0;
                while (entries.hasMoreElements()) {
                    String filename = entries.nextElement().getName();
                    if (filename.startsWith("assets/eln/model/") && filename.toLowerCase().endsWith(".obj")) {
                        filename = filename.substring(filename.indexOf("/model/") + 7, filename.length());
                        Utils.println(String.format("Loading model %03d '%s'", ++modelCount, filename));
                        loadObj(filename);
                    }
                }
            } else if (url.getProtocol().equals("file")) {
                File modelFolder = new File(url.toURI());
                Utils.println("Loading models from folder: " + modelFolder.getAbsolutePath());
                if (modelFolder.isDirectory()) {
                    loadModelsRecursive(modelFolder, 0);
                }
            } else if (url.getProtocol().equals("union")) {
                // Handle Forge/ModLauncher union protocol in dev environment
                // Example: union:/path/to/build/resources/main/%23203!/assets/eln/model
                String path = url.getPath();
                path = URLDecoder.decode(path, "UTF-8");
                // Remove the artifact part like /#203!/
                path = path.replaceAll("/#[^/]*!/", "/");
                
                File modelFolder = new File(path);
                Utils.println("Loading models from UNION folder: " + modelFolder.getAbsolutePath());
                if (modelFolder.isDirectory()) {
                    loadModelsRecursive(modelFolder, 0);
                } else {
                    Utils.println("Union path is not a directory: " + modelFolder.getAbsolutePath());
                }
            } else {
                Utils.println("Unknown protocol: " + url.getProtocol());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadModelsRecursive(File folder, Integer modelCount) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                loadModelsRecursive(file, modelCount);
            } else if (file.getName().toLowerCase().endsWith(".obj")) {
                String filename = file.getPath().replaceAll("\\\\", "/");
                filename = filename.substring(filename.indexOf("/model/") + 7, filename.length());
                Utils.println(String.format("Loading model %03d '%s'", ++modelCount, filename));
                loadObj(filename);
            }
        }
    }

    /**
     * Load an obj file of a model.
     *
     * @param modelPath path inside model folder (ex. Vumeter/Vumeter.obj)
     */
    private void loadObj(String modelPath) {
        Obj3D obj = new Obj3D();
        if (obj.loadFile(modelPath)) {
            String tag = modelPath.replaceAll(".obj", "").replaceAll(".OBJ", "");
            tag = tag.substring(tag.lastIndexOf('/') + 1, tag.length());
            if (nameToObjHash.containsKey(tag)) {
                Utils.println("Double load of model " + tag);
            }
            nameToObjHash.put(tag, obj);    // name of the file, without extension
            Utils.println(String.format(" - model '%s' loaded", modelPath));
        } else {
            Utils.println(String.format(" - unable to load model '%s'", modelPath));
        }
    }

    public Obj3D getObj(String obj3DName) {
        return nameToObjHash.get(obj3DName);
    }

    public Obj3DPart getPart(String objName, String partName) {
        Obj3D obj = getObj(objName);
        if (obj == null) return null;
        return obj.getPart(partName);
    }

    public void draw(String objName, String partName) {
        Obj3DPart part = getPart(objName, partName);
        if (part != null) part.draw();
    }

    public Set<String> getObjectList() {
        return nameToObjHash.keySet();
    }
}
