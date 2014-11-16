package com.googlecode.mavennatives.nativedependencies;

import java.io.File;
import java.io.IOException;

interface IJarUnpacker
{
	String ROLE = IJarUnpacker.class.getName();
	
	public void copyJarContent(File jarPath, File targetDir) throws IOException;
}
