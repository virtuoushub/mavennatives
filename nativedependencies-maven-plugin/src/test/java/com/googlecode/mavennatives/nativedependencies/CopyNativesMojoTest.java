package com.googlecode.mavennatives.nativedependencies;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.project.MavenProject;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

@RunWith(JMock.class)
public class CopyNativesMojoTest
{
	private final Mockery context = new Mockery()
	{
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};
	private CopyNativesMojo mojo;
	private JarUnpackable jarUnpacker;
	private MavenProject mavenProject;
	private File nativesTargetDir;
	private ArtifactStubFactory artifactFactory;
    private static final Log LOG = new SystemStreamLog();


    @Before
	public void setUp()
	{
		mojo = new CopyNativesMojo();
		mavenProject = context.mock(MavenProject.class);
		mojo.setMavenProject(mavenProject);
		jarUnpacker = context.mock(JarUnpackable.class);
		mojo.setJarUnpacker(jarUnpacker);
		nativesTargetDir = context.mock(File.class);
		mojo.setNativesTargetDir(nativesTargetDir);
		artifactFactory = new ArtifactStubFactory();
		mojo.setBuildContext(new DefaultBuildContext());
	}
	
	
	@Test
	public void executeWithoutDependenciesOnlyCreatesTheNativesDir() throws MojoExecutionException, MojoFailureException
	{
		final Set<Artifact> artifacts = new HashSet<>();
			
		context.checking(new Expectations()
		{
			{
                final boolean mkdirs = oneOf(nativesTargetDir).mkdirs();
                if(!mkdirs) {
                    LOG.debug(String.valueOf(mkdirs));
                }
                oneOf(mavenProject).getArtifacts();will(returnValue(artifacts));
			}
		});
		
		mojo.execute();
		
	}
	
	@Test
	public void executeWithoutNativeDependenciesOnlyCreatesTheNativesDir() throws MojoExecutionException, MojoFailureException, IOException
	{
		final Set<Artifact> artifacts = new HashSet<>();
			
		artifacts.add(artifactFactory.createArtifact("groupid1","artifactid1","1.0"));
		artifacts.add(artifactFactory.createArtifact("groupid2","artifactid2","2.0"));
		artifacts.add(artifactFactory.createArtifact("groupid3","artifactid3","3.0"));
		
		
		context.checking(new Expectations()
		{
			{
                final boolean mkdirs = oneOf(nativesTargetDir).mkdirs();
                if(!mkdirs) {
                    LOG.debug(String.valueOf(mkdirs));
                }
				oneOf(mavenProject).getArtifacts();will(returnValue(artifacts));
			}
		});
		
		mojo.execute();
	}
	
	@Test
	public void executeWithOneNativeDependenciesCallsTheUnpacker() throws MojoExecutionException, MojoFailureException, IOException
	{
		final Set<Artifact> artifacts = new HashSet<>();
			
		artifacts.add(artifactFactory.createArtifact("groupid1","artifactid1","1.0"));
		Artifact nativeArtifact = artifactFactory.createArtifact("groupid2","artifactid2","2.0","compile","jar","natives-windows");
		final File nativeFile = new File("test1");
		nativeArtifact.setFile(nativeFile);
		
		artifacts.add(nativeArtifact);
		artifacts.add(artifactFactory.createArtifact("groupid3","artifactid3","3.0"));
		
		
		context.checking(new Expectations()
		{
			{
                final boolean mkdirs = oneOf(nativesTargetDir).mkdirs();
                if(!mkdirs) {
                    LOG.debug(String.valueOf(mkdirs));
                }
                oneOf(mavenProject).getArtifacts();will(returnValue(artifacts));
				oneOf(jarUnpacker).copyJarContent(nativeFile, nativesTargetDir);
			}
		});
		
		mojo.execute();
	}

	@Test
	public void executeWithPlatformsSpecifiedNativeDependenciesCallsTheUnpacker() throws MojoExecutionException, MojoFailureException, IOException
	{
		mojo.setPlatforms(Arrays.asList("windows", "linux"));
		final Set<Artifact> artifacts = new HashSet<>();
			
		artifacts.add(artifactFactory.createArtifact("groupid1","artifactid1","1.0"));
		Artifact nativeArtifact = artifactFactory.createArtifact("groupid2","artifactid2","2.0","compile","jar","natives-windows");
		final File nativeFile = new File("test1");
		nativeArtifact.setFile(nativeFile);
		artifacts.add(nativeArtifact);

		Artifact nativeArtifact2 = artifactFactory.createArtifact("groupid2","artifactid2","2.0","compile","jar","natives-mac");
		final File nativeFile2 = new File("test2");
		nativeArtifact2.setFile(nativeFile2);
		artifacts.add(nativeArtifact2);

		Artifact nativeArtifact3 = artifactFactory.createArtifact("groupid2","artifactid2","2.0","compile","jar","natives-linux");
		final File nativeFile3 = new File("test3");
		nativeArtifact3.setFile(nativeFile3);
		artifacts.add(nativeArtifact3);

		artifacts.add(artifactFactory.createArtifact("groupid3","artifactid3","3.0"));
		
		context.checking(new Expectations()
		{
			{
                final boolean mkdirs = oneOf(nativesTargetDir).mkdirs();
                if(!mkdirs) {
                    LOG.debug(String.valueOf(mkdirs));
                }
                oneOf(mavenProject).getArtifacts();will(returnValue(artifacts));
				oneOf(jarUnpacker).copyJarContent(nativeFile, nativesTargetDir);
				oneOf(jarUnpacker).copyJarContent(nativeFile3, nativesTargetDir);
			}
		});
		
		mojo.execute();
	}	
}
