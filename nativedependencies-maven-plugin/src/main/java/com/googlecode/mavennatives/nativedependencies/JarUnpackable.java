package com.googlecode.mavennatives.nativedependencies;

import java.io.File;
import java.io.IOException;

interface JarUnpackable {

	String ROLE = JarUnpackable.class.getName();
	
	public void copyJarContent(File jarPath, File targetDir) throws IOException;
}
