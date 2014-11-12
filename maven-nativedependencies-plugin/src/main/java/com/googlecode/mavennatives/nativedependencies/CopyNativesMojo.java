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

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Unpacks native dependencies
 *
 * @goal copy
 * @phase package
 * @requiresProject true
 * @requiresDependencyResolution test
 */
public class CopyNativesMojo extends AbstractMojo {
  /**
   * POM
   *
   * @parameter expression="${project}"
   * @readonly
   * @required
   */
  private MavenProject project;

  /**
   *
   * @parameter expression="${nativesTargetDir}" default-value="${project.build.directory}/natives"
   */
  private File nativesTargetDir;

  /**
   * @parameter expression="${separateDirs}" default-value="false"
   */
  private boolean separateDirs;

  /**
   * @parameter expression="${platforms}"
   */
  private List<String> platforms;

  /**
   * @component
   */
  private IJarUnpacker jarUnpacker;

  /**
   * @component
   * */
  private BuildContext buildContext;

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
      final Set<Artifact> artifacts = project.getArtifacts();
      nativesTargetDir.mkdirs();
      getLog().debug(String.format("Using "));
      for (Artifact artifact : artifacts) {
        String classifier = artifact.getClassifier();
        if (classifier != null && classifier.startsWith("natives-")) {
          String platform = classifier.substring("natives-".length());
          if (platformsActive && (!platforms.contains(platform))) {
            getLog().debug(String.format("Skipping other platform: G:%s - A:%s - C:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier()));
            continue;
          }
          getLog().info(String.format("G:%s - A:%s - C:%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier()));
          File artifactDir = nativesTargetDir;
          if (separateDirs) {
            artifactDir = new File(nativesTargetDir, platform);
            artifactDir.mkdirs();
          }
          jarUnpacker.copyJarContent(artifact.getFile(), artifactDir);
        }

      }
      buildContext.refresh(nativesTargetDir);
    } catch (Exception e) {
      throw new MojoFailureException("Unable to copy natives", e);
    }
  }

  public void setMavenProject(MavenProject mavenProject) {
    this.project = mavenProject;
  }

  public void setNativesTargetDir(File nativesTargetDir2) {
    this.nativesTargetDir = nativesTargetDir2;
  }

  public void setJarUnpacker(IJarUnpacker jarUnpacker) {
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
