import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;

import de.waldheinz.fs.*;
import de.waldheinz.fs.FileSystem;
import de.waldheinz.fs.util.*;

public class DecryptedNandOperator extends FullNandOperator
{
	protected boolean isOpen;
	
	public DecryptedNandOperator (String path, String CID, String consoleID) throws IOException
	{
		this.path = path;
		this.CID = CID;
		this.consoleID = consoleID;
		initialize();
	}
	
	
	/**
	 * Initializes the DecrypytedNandOperator by forming 
	 * its helper file in the Resources folder. 
     * @throws IOException on write/read error
     */
	protected void initializeAlt() throws IOException
	{
		byte [] bytes = Files.readAllBytes(Paths.get(path));
		byte [] partition0 = new byte [215945728];
		for (int x = 0; x < partition0.length; x++)
		{
			partition0[x] = bytes [x + 1109504];
		}
		Files.write(Paths.get(RESOURCES_FOLDER + "\\" + new File(path).getName()+".partition0"), partition0);
		isOpen = true;
	}
	protected void initialize () throws IOException
	{
	  	FileInputStream in = new FileInputStream(new File(path));
	   	FileOutputStream pre = new FileOutputStream(RESOURCES_FOLDER + "\\" + new File(new File(path).getName()+".pre"));
	   	FileOutputStream partition0 = new FileOutputStream(RESOURCES_FOLDER + "\\" + new File(new File(path).getName()+".partition0"));
	   	FileOutputStream post = new FileOutputStream(RESOURCES_FOLDER + "\\" + new File(new File(path).getName()+".post"));
	   	final int PRE_SIZE = 1109504;
	   	final int PARTITION0_SIZE = 215945728;
	   	final int POST_SIZE = (int)(new File(path).length()) - PRE_SIZE - PARTITION0_SIZE;
	   	byte [] b;

	   	int preSize = PRE_SIZE;
	   	while(preSize > 0)
	   	{
	   		if (preSize < 1000)
	   		{
	   			b = new byte [preSize];
	   			preSize -= preSize;
	   		}
	   		else
	   		{
	   			b = new byte [1000];
	   			preSize -= 1000;
	   		}
			in.read(b);
			pre.write(b);
	   	}
		
	   	int p0Size = PARTITION0_SIZE;
	   	while(p0Size > 0)
	   	{
	   		if (p0Size < 1000)
	   		{
	   			b = new byte [p0Size];
	   			p0Size -= p0Size;
	   		}
	   		else
	   		{
	   			b = new byte [1000];
	   			p0Size -= 1000;
	   		}
			in.read(b);
			partition0.write(b);
	   	}
		
	   	int postSize = POST_SIZE;
	   	while(postSize > 0)
	   	{
	   		if (postSize < 1000)
	   		{
	   			b = new byte [postSize];
	   			postSize -= postSize;
	   		}
	   		else
	   		{
	   			b = new byte [1000];
	   			postSize -= 1000;
	   		}
			in.read(b);
			post.write(b);
	   	}
	   	
		isOpen = true;
		in.close();
		partition0.close();
		pre.close();
		post.close();
	}
	
	public void write (String pathInComputer, String pathInNand) throws IOException
	{
		File aFile = new File(pathInComputer);
		
		//Set up the object for the fat drive using partition 0
		String fileName = new File (pathInNand).getName();
		String fileDirectory = new File (pathInNand).getParent();
		File file = new File(RESOURCES_FOLDER + "\\" + new File(this.path).getName()+".partition0");
		FileDisk device = new FileDisk(file, false);
		FileSystem fs = FileSystemFactory.create(device, false);
		
		//fetch the File Directory object for the target folder
		FsDirectory fd = fs.getRoot();
		String [] folders = fileDirectory.split("\\\\");
		for (String folder : folders)
		{
			if(fd.getEntry(folder) == null)
			{
				FsDirectory ff = fd.addDirectory(folder).getDirectory();
				ff.flush();
			}
			fd = fd.getEntry(folder).getDirectory();
		}
		if(fd.getEntry(fileName)!= null)
			fd.remove(fileName);
		// Create the entry on the drive
		FsDirectoryEntry driveEntry = fd.addFile(fileName);
		
		FsFile driveFile = driveEntry.getFile();
		
		// Copy the file over
		if (aFile.isFile()) 
		{
			FileInputStream fis= new FileInputStream(aFile);
			
			FileChannel fci = fis.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			
			long counter=0;
			
			// http://www.kodejava.org/examples/49.html
			// Here we start to read the source file and write it
			// to the destination file. We repeat this process
			// until the read method of input stream channel return
			// nothing (-1).
			while(true)
			{
				// read a block of data and put it in the buffer
				int read = fci.read(buffer);
				
				// did we reach the end of the channel? if yes
				// jump out the while-loop
				if (read == -1)
					break;
				
				// flip the buffer
				buffer.flip();
				
				// write to the destination channel
				driveFile.write(counter*1024, buffer);
				counter++;
				
				
				// clear the buffer and user it for the next read
				// process
				buffer.clear();
			}
			
			driveFile.flush();
			
			fis.close();
			fs.close();
			device.close();
		}
		
	}    
	
	public void read (String pathInComputer, String pathInNand) throws IOException
	{
		File aFile = new File(pathInComputer);
		aFile.createNewFile();
		//Set up the object for the fat drive using partition 0
		String fileName = new File (pathInNand).getName();
		String fileDirectory = new File (pathInNand).getParent();
		File file = new File(RESOURCES_FOLDER + "\\" + new File(this.path).getName()+".partition0");
		FileDisk device = new FileDisk(file, false);
		FileSystem fs = FileSystemFactory.create(device, false);
		
		//fetch the File Directory object for the target folder
		FsDirectory fd = fs.getRoot();
		String [] folders = fileDirectory.split("\\\\");
		for (String folder : folders)
		{
			if(fd.getEntry(folder) != null)
			{
				fd = fd.getEntry(folder).getDirectory();
			}
		}
		
		// Create the entry on the drive
		FsDirectoryEntry driveEntry = null;
		if (fd != null)
		{
			driveEntry = fd.getEntry(fileName);
		}
		if(driveEntry!= null)
		{
			FsFile driveFile = driveEntry.getFile();
			FileOutputStream fis= new FileOutputStream(aFile);
			
			FileChannel fci = fis.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate((int) driveFile.getLength());
			
			long counter=0;
			
			// http://www.kodejava.org/examples/49.html
			// Here we start to read the source file and write it
			// to the destination file. We repeat this process
			// until the read method of input stream channel return
			// nothing (-1).
			///while(true)
		//	{
				// read a block of data and put it in the buffer
				//int read = fci.read(buffer);
				driveFile.read(counter, buffer);
			
				// did we reach the end of the channel? if yes
				// jump out the while-loop
			//	if (!(driveFile.getLength()>=counter))
			//		break;
				buffer.flip();
				// flip the buffer
				// write to the destination channel
				fci.write(buffer);
				buffer.flip();
				counter++;
				
				
				// clear the buffer and user it for the next read
				// process
				buffer.clear();

			fis.close();
		}
		fs.close();
		device.close();
	}
	

	
	public List <String> listEntries (String pathInNand) throws IOException
	{
		File file = new File(RESOURCES_FOLDER + "\\" + new File(this.path).getName()+".partition0");
		FileDisk device = new FileDisk(file, false);
		FileSystem fs = FileSystemFactory.create(device, false);
		FsDirectory fd = fs.getRoot();
		String [] folders = pathInNand.split("\\\\");
		for (String folder : folders)
		{
			if(fd.getEntry(folder) == null)
			{
				fs.close();
				device.close();
				return null;
			}
			fd = fd.getEntry(folder).getDirectory();
		}
		Iterator <FsDirectoryEntry> iterator = fd.iterator();
		List <String> names = new ArrayList <String>();
		while(iterator.hasNext())
		{
			names.add(iterator.next().getName());
		}
		fs.close();
		device.close();
		return names;
	}
	
	public void createFolder (String pathInNand) throws IOException
	{
		String fileName = new File (pathInNand).getName();
		String fileDirectory = new File (pathInNand).getParent();
		File file = new File(RESOURCES_FOLDER + "\\" + new File(this.path).getName()+".partition0");
		FileDisk device = new FileDisk(file, false);
		FileSystem fs = FileSystemFactory.create(device, false);
		FsDirectory fd = fs.getRoot();
		String [] folders = fileDirectory.split("\\\\");
		for (String folder : folders)
		{
			if(fd.getEntry(folder) == null)
			{
				FsDirectory ff = fd.addDirectory(folder).getDirectory();
				ff.flush();
			}
			fd = fd.getEntry(folder).getDirectory();
		}
		if(fd.getEntry(fileName) == null)
		{
			FsDirectory ff = fd.addDirectory(fileName).getDirectory();
			ff.flush();
		}
		fs.close();
		device.close();
	}
	
	public boolean delete (String pathInNand) throws IOException
	{
		String fileName = new File (pathInNand).getName();
		String fileDirectory = new File (pathInNand).getParent();
		File file = new File(RESOURCES_FOLDER + "\\" + new File(this.path).getName()+".partition0");
		FileDisk device = new FileDisk(file, false);
		FileSystem fs = FileSystemFactory.create(device, false);
		FsDirectory fd = fs.getRoot();
		String [] folders = fileDirectory.split("\\\\");
		for (String folder : folders)
		{
			if(fd.getEntry(folder) == null)
			{
				fs.close();
				device.close();
				return false;
			}
			fd = fd.getEntry(folder).getDirectory();
		}
		if(fd.getEntry(fileName) == null)
		{
			fs.close();
			device.close();
			return false; 
		}
		fd.remove(fileName);
		fs.close();
		device.close();
		return true;
	}
	
	public boolean exists (String pathInNand) throws IOException
	{

		String fileName = new File (pathInNand).getName();
		String fileDirectory = new File (pathInNand).getParent();
		File file = new File(RESOURCES_FOLDER + "\\" + new File(this.path).getName()+".partition0");
		FileDisk device = new FileDisk(file, false);
		FileSystem fs = FileSystemFactory.create(device, false);
		FsDirectory fd = fs.getRoot();
		String [] folders = fileDirectory.split("\\\\");
		for (String folder : folders)
		{
			if(fd.getEntry(folder) == null)
			{
				fs.close();
				device.close();
				return false;
			}
			fd = fd.getEntry(folder).getDirectory();
		}
		if(fd.getEntry(fileName) == null)
		{
			fs.close();
			device.close();
			return false; 
		}
		else 
		{
			fs.close();
			device.close();
			return true;
		}
		
	}
	
	public String altDecNand () throws IOException
	{
		File decrypted = new File(path);
		String partition0file = RESOURCES_FOLDER + "\\" + new File(path).getName()+".partition0";
		byte [] partition0 = Files.readAllBytes(Paths.get(partition0file));
		byte [] nand = Files.readAllBytes(Paths.get(path));
		for (int x = 0; x < partition0.length; x++)
		{
			nand[x + 1109504] = partition0[x];
		}
		Files.write(Paths.get(RESOURCES_FOLDER + "\\" + decrypted.getName() + ".decrypted"), nand);
		return RESOURCES_FOLDER + "\\" + decrypted.getName() + ".decrypted";
		
	}
	
	public String makeDecryptedNand () throws IOException 
	{
		FileOutputStream in = new FileOutputStream(new File(RESOURCES_FOLDER + "\\" + new File(path).getName() + ".decrypted"));
		FileInputStream pre = new FileInputStream(RESOURCES_FOLDER + "\\" + new File(new File(path).getName()+".pre"));
		FileInputStream partition0 = new FileInputStream(RESOURCES_FOLDER + "\\" + new File(new File(path).getName()+".partition0"));
	   	FileInputStream post = new FileInputStream(RESOURCES_FOLDER + "\\" + new File(new File(path).getName()+".post"));
	   	final int PRE_SIZE = 1109504;
	   	final int PARTITION0_SIZE = 215945728;
	   	final int POST_SIZE = (int)(new File(path).length()) - PRE_SIZE - PARTITION0_SIZE;
	   	byte [] b;

	   	int preSize = PRE_SIZE;
	   	while(preSize > 0)
	   	{
	   		if (preSize < 1000)
	   		{
	   			b = new byte [preSize];
	   			preSize -= preSize;
	   		}
	   		else
	   		{
	   			b = new byte [1000];
	   			preSize -= 1000;
	   		}
	   		
			pre.read(b);
			in.write(b);
	   	}
		
	   	int p0Size = PARTITION0_SIZE;
	   	while(p0Size > 0)
	   	{
	   		if (p0Size < 1000)
	   		{
	   			b = new byte [p0Size];
	   			p0Size -= p0Size;
	   		}
	   		else
	   		{
	   			b = new byte [1000];
	   			p0Size -= 1000;
	   		}
			partition0.read(b);
			in.write(b);
	   	}
	   	int postSize = POST_SIZE;
	   	while(postSize > 0)
	   	{
	   		if (postSize < 1000)
	   		{
	   			b = new byte [postSize];
	   			postSize -= postSize;
	   		}
	   		else
	   		{
	   			b = new byte [1000];
	   			postSize -= 1000;
	   		}
			post.read(b);
			in.write(b);
	   	}
	   	
		isOpen = true;
		in.close();
		partition0.close();
		pre.close();
		post.close();
		return RESOURCES_FOLDER + "\\" + new File(path).getName() + ".decrypted";
	}
	/**
	 * Closes the DecrypytedNandOperator by deleting 
	 * its helper file in the Resources folder. 
	 * After it is closed, almost all methods will
	 * likely fail
     * @throws IOException on write/read error
     */
	public void close () throws IOException
	{
		Files.delete(Paths.get(RESOURCES_FOLDER + "\\" + new File(path).getName() + ".partition0"));
		isOpen = false;
	}
	
	/**
	 * @return A boolean specifying whether the 
	 * DecryptedNandOperator is open.
     */
	public boolean isOpen ()
	{
		return isOpen;
	}
}
