package com.googlecode.mavennatives.nativedependencies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = IJarUnpacker.class)
public class JarUnpacker implements IJarUnpacker {
    private static final Log log = new SystemStreamLog();

    private static final List<String> IGNORED_FILES = new ArrayList<String>() {{
        add("META-INF");
        add("MANIFEST.MF");
    }};

    public void copyJarContent(File jarPath, File targetDir) throws IOException {
        log.info("Copying natives from " + jarPath.getName());
        JarFile jar = new JarFile(jarPath);

        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            final JarEntry file = entries.nextElement();

            final File f = new File(targetDir, file.getName());
            if (!IGNORED_FILES.contains(f.getName())) {
                log.info("Copying native - " + file.getName());

                final File parentFile = f.getParentFile();
                if (!parentFile.exists()) {
                    final boolean wereParentFileDirectoriesMade = parentFile.mkdirs();
                    if (!wereParentFileDirectoriesMade) {
                        throw new IOException("Unable to create directories.");
                    }
                }

                if (file.isDirectory() && !f.exists()) { // if its a directory, create it
                    final boolean wereFileDirectoriesMade = f.mkdir();
                    if (!wereFileDirectoriesMade) {
                        throw new IOException("Unable to create directories.");
                    }
                    continue;
                }

                try (InputStream is = jar.getInputStream(file);
                     FileOutputStream fos = new FileOutputStream(f)) {
                    IOUtils.copy(is, fos);
                }
            }
        }
    }

}
