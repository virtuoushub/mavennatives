package com.googlecode.mavennatives.nativedependencies;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Unpacks native dependencies
 */
@Mojo(name = "copy", requiresProject = true, requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.PACKAGE)
final class CopyNativesMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/natives")
    private File nativesTargetDir;

    @Parameter(defaultValue = "false")
    private boolean separateDirs;

    @Parameter
    private List<String> platforms;

    /**
     * @component
     */
    @Component
    private JarUnpackable jarUnpacker;

    /**
     * @component
     */
    @Component
    private BuildContext buildContext;

    private static final Log LOG = new SystemStreamLog();


    /**
     * Type erasure in <code>final Set<Artifact> artifacts = project.getArtifacts();</code> is the reasons for @SuppressWarnings("unchecked")
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Saving natives in " + nativesTargetDir);
            if (separateDirs) {
                getLog().info("Storing artifacts in separate dirs according to classifier");
            }
            final boolean platformsActive = platforms != null && (!platforms.isEmpty());
            if (platformsActive) {
                getLog().info(String.format("Only copying the following platforms: %s", platforms));
            } else {
                getLog().info("Copying all platforms.");
            }
            @SuppressWarnings("unchecked")
            final Set<Artifact> artifacts = project.getArtifacts();
            final boolean wereNativesTargetDirectoriesMade = nativesTargetDir.mkdirs();
            if (!wereNativesTargetDirectoriesMade) {
                getLog().info("Unable to create directories(may already have existed): " + nativesTargetDir);
            }
            for (Artifact artifact : artifacts) {
                String classifier = artifact.getClassifier();
                if (classifier != null && classifier.startsWith("natives-")) {
                    String platform = classifier.substring("natives-".length());
                    if (platformsActive && (!platforms.contains(platform))) {
                        getLog().info(String.format("Skipping other platform: G:%s - A:%s - C:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier()));
                        continue;
                    }
                    getLog().info(String.format("G:%s - A:%s - C:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier()));
                    File artifactDir = nativesTargetDir;
                    if (separateDirs) {
                        artifactDir = new File(nativesTargetDir, platform);
                        final boolean wereArtifactDirectoriesMade = artifactDir.mkdirs();
                        if (!wereArtifactDirectoriesMade) {
                            getLog().info("Unable to create directories(may already have existed): " + nativesTargetDir);
                        }
                    }
                    jarUnpacker.copyJarContent(artifact.getFile(), artifactDir);
                }

            }
            buildContext.refresh(nativesTargetDir);
        } catch (IOException e) {
            try (final Writer sw = new StringWriter(); final Writer pw = new PrintWriter(sw)) {
                e.printStackTrace((PrintWriter) pw);
                throw new MojoFailureException("IllegalStateException prevented copying of natives: " + sw.toString(), e);
            } catch (IOException ioe) {
                throw new MojoFailureException("IOException prevented copying of natives: " + ioe.getLocalizedMessage(), e);
            }
        } catch (NullPointerException e) {
            try (final Writer sw = new StringWriter(); final Writer pw = new PrintWriter(sw)) {
                e.printStackTrace((PrintWriter) pw);
                throw new MojoFailureException("NullPointerException prevented copying of natives: " + sw.toString(), e);
            } catch (IOException ioe) {
                throw new MojoFailureException("IOException prevented copying of natives: " + ioe.getLocalizedMessage(), e);
            }
        } catch (SecurityException e) {
            throw new MojoFailureException("SecurityException prevented copying of natives.", e);
        } catch (Exception e) {
            throw new MojoFailureException("Exception prevented copying of natives: " + e.toString(), e);
        }
    }

    public void setMavenProject(MavenProject mavenProject) {
        this.project = mavenProject;
    }

    public void setNativesTargetDir(File nativesTargetDir2) {
        this.nativesTargetDir = nativesTargetDir2;
    }

    public void setJarUnpacker(JarUnpackable jarUnpacker) {
        this.jarUnpacker = jarUnpacker;
    }

    public void setBuildContext(BuildContext buildContext) {
        this.buildContext = buildContext;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }
}
