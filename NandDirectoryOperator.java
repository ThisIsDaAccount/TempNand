import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;


public class NandDirectoryOperator extends BasicNandOperator 
{
	String rootFolder;
	
	public NandDirectoryOperator (String consoleID, String rootFolder)
	{
		this.consoleID = consoleID;
		this.rootFolder = rootFolder;
	}
	
	public boolean exists(String pathInNand) throws IOException 
	{
		return Files.exists(Paths.get(rootFolder + pathInNand));
	}

	public boolean delete(String pathInNand) throws IOException 
	{
		boolean returnValue = exists(pathInNand);
		if(returnValue)
		{
			Files.delete(Paths.get(rootFolder+pathInNand));
		}
		return returnValue;
	}

	public void createFolder(String pathInNand) throws IOException 
	{
		Files.createDirectory(Paths.get(rootFolder + pathInNand));
	}

	public void read(String pathInComputer, String pathInNand) throws IOException 
	{
		Files.copy(Paths.get(rootFolder+pathInNand), Paths.get(pathInComputer));
	}
	
	public void write(String pathInComputer, String pathInNand) throws IOException 
	{
		Files.copy(Paths.get(pathInComputer), Paths.get(rootFolder+pathInNand));
	}

	public List<String> listEntries(String pathInNand) throws IOException 
	{
		return Arrays.asList(new File(pathInNand).list());
	}

}
