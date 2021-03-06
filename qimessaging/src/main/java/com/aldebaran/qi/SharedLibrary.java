/*
 * * Copyright (C) 2015 Aldebaran Robotics* See COPYING for the license
 */
package com.aldebaran.qi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import com.aldebaran.qi.util.resources.ResourceDirectory;
import com.aldebaran.qi.util.resources.ResourceElement;
import com.aldebaran.qi.util.resources.Resources;
import com.aldebaran.qi.util.resources.ResourcesSystem;

/**
 * Class responsible for managing the dynamic libraries used by the qi Framework
 * according to the system.
 */
public class SharedLibrary {
    private static String osName = System.getProperty("os.name");
    private static String tmpDir = System.getProperty("java.io.tmpdir");

    public static String getLibraryName(String name) {
        return getPrefix() + name + getSuffix();
    }

    public static void overrideTempDirectory(File newValue) {
        tmpDir = newValue.getAbsolutePath();
    }

    public static boolean loadLib(String name) {
        boolean firstTry = false;
        String libName = getLibraryName(name);
        firstTry = loadLibHelper(libName);
        if (firstTry) {
            return true;
        }
        // Try in debug too on windows:
        if (!osName.startsWith("Windows")) {
            return false;
        }
        String debugLibName = getLibraryName(name + "_d");
        return loadLibHelper(debugLibName);
    }

    public static boolean loadLibs(String... names) {

        boolean allLoaded = true;

        try {
            Resources resources = new Resources(SharedLibrary.class);
            ResourcesSystem resourcesSystem = resources.obtainResourcesSystem();
            ResourceDirectory directory = (ResourceDirectory) resourcesSystem.obtainElement(getSubDir());
            List<ResourceElement> elements = resourcesSystem.obtainList(directory);
            boolean found;
            String resourceName;

            for (String name : names) {
                found = false;

                for (ResourceElement resourceElement : elements) {
                    resourceName = resourceElement.getName();

                    if (resourceName.contains(name) && !resourceName.contains("_d.")) {
                        allLoaded &= loadLibHelper(resourceName);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    for (ResourceElement resourceElement : elements) {
                        resourceName = resourceElement.getName();

                        if (resourceName.contains(name)) {
                            allLoaded &= loadLibHelper(resourceName);
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    allLoaded = false;
                    System.out.println("WARNING not found : " + name);
                }
            }
        }
        catch (IOException e) {
            return false;
        }

        return allLoaded;
    }

    private static boolean loadLibHelper(String libraryName) {
        System.out.format("Loading %s\n", libraryName);
        String resourcePath = "/" + getSubDir() + "/" + libraryName;
        URL libraryUrl = SharedLibrary.class.getResource(resourcePath);
        if (libraryUrl == null) {
            System.out.format("No such resource %s\n", resourcePath);
            boolean found = false;

            try {
                Resources resources = new Resources(SharedLibrary.class);
                ResourcesSystem resourcesSystem = resources.obtainResourcesSystem();
                ResourceDirectory directory = (ResourceDirectory) resourcesSystem.obtainElement(getSubDir());
                List<ResourceElement> elements = resourcesSystem.obtainList(directory);

                for (ResourceElement resourceElement : elements) {
                    if (resourceElement.getName().startsWith(libraryName)) {
                        System.out.println("Found instead: " + resourceElement.getPath());
                        libraryUrl = SharedLibrary.class.getResource("/" + resourceElement.getPath());
                        System.out.println("libraryUrl=" + libraryUrl);

                        if (libraryUrl != null) {
                            found = true;
                            break;
                        }
                    }
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

            if (!found) {
                return false;
            }
        }

        String libPath = libraryUrl.getPath();
        File libFile = new File(libPath);
        if (libFile.exists()) {
            // first, try as a real file
            System.out.format("Loading %s from filesystem\n", libraryUrl);
            System.load(libPath);
            return true;
        }
        else {
            // then, try as a file in the jar
            return extractAndLoad(libraryUrl, libraryName);
        }
    }

    private static String getSubDir() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        if (osName.contains("mac")) {
            return "mac64";
        }
        else if (osName.contains("win")) {
            if (osArch.contains("x86")) {
                return "win32";
            }
            if (osArch.contains("64")) {
                return "win64";
            }
        }
        else if (osName.contains("linux")) {
            return "linux64";
        }
        return "";
    }

    private static boolean extractAndLoad(URL libraryUrl, String libraryName) {

        String tmpLibraryPath = tmpDir + File.separator + libraryName;
        File libraryFile = new File(tmpLibraryPath);
        if (libraryFile.exists()) {
            libraryFile.delete();
        }
        // re-open it
        libraryFile = new File(tmpLibraryPath);
        libraryFile.deleteOnExit();
        InputStream in;
        OutputStream out;
        try {
            in = libraryUrl.openStream();
        }
        catch (IOException e) {
            System.out.format("Could not open %s for reading. Error was: %s\n", libraryUrl, e.getMessage());
            return false;
        }
        try {
            out = new BufferedOutputStream(new FileOutputStream(libraryFile));
        }
        catch (IOException e) {
            System.out.format("Could not open %s for writing. Error was: %s\n", libraryFile, e.getMessage());
            return false;
        }

        try {
            // Extract it in a temporary file
            int len = 0;
            byte[] buffer = new byte[10000];
            while ((len = in.read(buffer)) > -1) {
                out.write(buffer, 0, len);
            }

            // Close files
            out.close();
            in.close();
        }
        catch (IOException e) {
            System.out.format("Could not copy from %s to %s: %s\n", libraryUrl, tmpLibraryPath, e.getMessage());
            return false;
        }

        System.out.format("Loading: %s \n", tmpLibraryPath);
        System.load(tmpLibraryPath);

        return true;
    }

    private static String getPrefix() {
        if (osName.startsWith("Linux") || osName.startsWith("Mac")) {
            return "lib";
        }
        return "";
    }

    private static String getSuffix() {
        if (osName.startsWith("Linux")) {
            return ".so";
        }
        if (osName.startsWith("Mac")) {
            return ".dylib";
        }
        if (osName.startsWith("Windows")) {
            return ".dll";
        }
        // Android, atom, ....
        return ".so";
    }

}
