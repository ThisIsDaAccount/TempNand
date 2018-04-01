import java.awt.FileDialog;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class NandUtilities 
{
	protected final static String RESOURCES_FOLDER = "DSi_Resources";
	public static String extractCID (String CIDFilePath) throws IOException
	{
		String CID = "";
		byte [] CIDBytes = Files.readAllBytes(Paths.get(CIDFilePath));
		for (byte CIDByte : CIDBytes) 
	    {
	        CID+=(String.format("%02X", CIDByte));
	    }
		return CID.toString();
	}
	
	public static String extractCID() throws IOException
	{
		FileDialog fd = new FileDialog((java.awt.Frame) null, "Please navigate to the CID.bin file", FileDialog.LOAD); 
		fd.setVisible(true);
		fd.dispose();
		if(fd.getFile()==null)
			throw new FileNotFoundException();
		return extractCID(fd.getDirectory()+fd.getFile());
	}
	
	public static String extractConsoleID (String SRLFilePath) throws IOException, InterruptedException
	{
		Files.copy(Paths.get(SRLFilePath), Paths.get("SRL.bin"));
		Runtime.getRuntime().exec("dsi_srl_extract.exe --basename=SRL_Extracted_File SRL.bin").waitFor();
		byte [] footerBytes = Files.readAllBytes(Paths.get("SRL_Extracted_File.footer"));
		File [] files = new File(".").listFiles();
		Files.delete(Paths.get("SRL.bin"));
		Files.delete(Paths.get("dsi_srl_extract.ini"));
		for (File file : files)
		{
			String fileName = file.getName();
			if (fileName.contains("SRL_Extracted_File") || fileName.contains(".nds"))
			{
				Files.delete(Paths.get(fileName));
			}
		}
		String consoleID = "";
		for(int x = 486; x < 502; x++)
		{
			consoleID += (char)footerBytes[x];
		}
		return consoleID;
	}
	
	public static String extractConsoleID() throws IOException, InterruptedException
	{
		FileDialog fd = new FileDialog((java.awt.Frame) null, "Please navigate to the DSiWare extract from System Settings", FileDialog.LOAD); 
		fd.setVisible(true);
		fd.dispose();
		if(fd.getFile()==null)
			throw new FileNotFoundException();
		return extractConsoleID(fd.getDirectory()+fd.getFile());
	}
}
