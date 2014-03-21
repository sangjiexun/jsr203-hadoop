package testhdfs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import hdfs.jsr203.HadoopFileSystemProvider;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.hadoop.hdfs.MiniDFSCluster;

public class FileSystemTest {

	private static int port = 8020;
	private static String host = "nc-h04";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		port = 8020;
		host = "nc-h04";
	}
	
	private MiniDFSCluster startMini(String testName) throws IOException {
		File baseDir = new File("./target/hdfs/" + testName).getAbsoluteFile();
		FileUtil.fullyDelete(baseDir);
		/*conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath());
		MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(conf);
		MiniDFSCluster hdfsCluster = builder.build();
		String hdfsURI = "hdfs://localhost:"+ hdfsCluster.getNameNodePort() + "/";*/
		MiniDFSCluster cluster = new MiniDFSCluster();
		return cluster;
	}

	/**
	 * Check that a FileSystemProvider handle <code>hdfs</code> scheme.
	 */
	@Test
	public void testAutoRegister() {

		boolean found = false;
		for (FileSystemProvider fp : FileSystemProvider.installedProviders())
			if (fp.getScheme().equals(HadoopFileSystemProvider.SCHEME))
				found = true;
		// Check auto register of the provider
		assertTrue(found);
	}
	
	@Test
	public void testProvider() throws URISyntaxException {
		URI uri = new URI("hdfs://" + host + ":" + port + "/tmp/test_file");
		Path path = Paths.get(uri);
		assertNotNull(path.getFileSystem());
		assertNotNull(path.getFileSystem().provider());
	}

	@Test(expected = NoSuchFileException.class)
	public void testNoSuchFileExceptionOnDelete() throws URISyntaxException,
			IOException {
		// start the demo cluster
		//MiniDFSCluster cluster = startMini("testNoSuchFileExceptionOnDelete");
		//URI uri = new URI("hdfs://" + host + ":" + cluster.getNameNodePort() + "/tmp/test_file");
		
		
		URI uri = new URI("hdfs://" + host + ":" + port + "/tmp/test_file");
		Path path = Paths.get(uri);
		
		Assume.assumeTrue(!Files.exists(path));
		
		Files.createFile(path);
		assertTrue(Files.exists(path));
		Files.delete(path);
		assertFalse(Files.exists(path));

		try 
		{
			Files.delete(path); // this one generate the exception
		}
		finally
		{
			//cluster.shutdown();
		}
	}

	@Test(expected = DirectoryNotEmptyException.class)
	public void testDirectoryNotEmptyExceptionOnDelete()
			throws URISyntaxException, IOException {
		// Create the directory
		URI uriDir = new URI("hdfs://" + host + ":" + port + "/tmp/test_dir");
		Path pathDir = Paths.get(uriDir);
		// Check that directory doesn't exists
		if (Files.exists(pathDir))
			Files.delete(pathDir);
		
		
		Files.createDirectory(pathDir);
		assertTrue(Files.exists(pathDir));
		// Create the file
		URI uri = new URI("hdfs://" + host + ":" + port
				+ "/tmp/test_dir/test_file");
		Path path = Paths.get(uri);
		Files.createFile(path);
		assertTrue(Files.exists(path));

		Files.delete(pathDir); // this one generate the exception

	}

	@Test
	public void testsetLastModifiedTime() throws URISyntaxException,
			IOException {
		URI uri = new URI("hdfs://" + host + ":" + port + "/tmp/test_file");
		Path file = Paths.get(uri);
		Files.createFile(file);
		assertTrue(Files.exists(file));
		BasicFileAttributes attr = Files.readAttributes(file,
				BasicFileAttributes.class);
		assertNotNull(attr);
		long currentTime = System.currentTimeMillis();
		FileTime ft = FileTime.fromMillis(currentTime);
		Files.setLastModifiedTime(file, ft);
		
		Files.delete(file);
	}
}