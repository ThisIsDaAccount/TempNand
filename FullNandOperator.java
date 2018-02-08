
import java.io.*;
import java.nio.file.*;

public abstract class FullNandOperator extends BasicNandOperator
{
	protected String CID;
	protected String path;
	
	/**
     * Saves the changes made to a nand backup without
     * encrypting it and writes it to the Resources
     * folder
     * @throws IOException on write/read error
     * @return a String representing the saved
     * backup's location
     */
	public abstract String makeDecryptedNand () throws IOException; 
	
	/**
     * Creates the bytes that make up a nand's No$GBA footer
     * using its CID and consoleID.
     * @throws IOException on write/read error
     * @return a byte array containing the bytes that form
     * this nand's footer
     */
	protected byte[] createFooterArray() throws IOException
	{
		byte [] footer = {68, 83, 105, 32, 101, 77, 77, 67, 32, 67, 73, 68, 47, 67, 80, 85, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -86, -69, -69, -69, -69, -69, -69, -69, -69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		byte [] cidBytes = new byte [16];
		for (int x = 0; x < 32; x+=2)
		{
			cidBytes[x/2] = (byte) Integer.parseInt(CID.substring(x, x+2), 16);
		}
		byte [] consoleIDBytes = new byte [8];
		for (int x = 0; x < 16; x+=2)
		{
			consoleIDBytes[x/2] = (byte) Integer.parseInt(consoleID.substring(x, x+2), 16);
		}
		
		for(int x = 0; x < 16; x++)
		{
			footer[x+16] = cidBytes[x];
		}
		
		for(int x = 0; x < 8; x++)
		{
			footer[x+32] = consoleIDBytes[7-x];
		}
		return footer;
		
	}
	
	/**
     * Creates a nand's No$GBA footer using its CID 
     * and consoleID in the Resources folder
     * @throws IOException on write/read error
     * @return a String representing the footer file's path
     */
	public String makeFooter () throws IOException
	{
		Files.write(Paths.get(RESOURCES_FOLDER + "\\" + "footer.bin"), createFooterArray());
		return RESOURCES_FOLDER + "\\" + "footer.bin";
	}
	
	/**
     * Creates a nand's No$GBA footer using its CID 
     * and consoleID in the specified path
     * @param savePath THe path tov which the footer 
     * file will be saved
     * @throws IOException on write/read error
     */
	public void saveFooterTo (String savePath) throws IOException
	{
		Files.write(Paths.get(savePath), createFooterArray());
	}
	
	/**
     * Saves the changes made to a nand backup without
     * encrypting it and writes it to the folder in the
     * specified path
     * @param savePath the path to where the backup will
     * be saved
     * @throws IOException on write/read error
     */
	public void save (String savePath) throws IOException
	{
		String nandLocation = makeDecryptedNand();
		Files.move(Paths.get(nandLocation), Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
	}
	
	
	/**
     * Saves the changes made to a nand backup, encrypts
     * it, and then  writes it to the Resources folder.
     * A boolean value is also passed as a parameter, 
     * and if it is true the backup will be made with 
     * the No$GBA footer appended to it. A String value 
     * containing the backup's location is returned.
     * @param addFooter a boolean indicating whether
     * or not to append the No$GBA footer to the 
     * nand backup 
     * @throws IOException on write/read error
     * @throws InterruptedException on errors from 
     * TWLtool commands
     * @return a String containing the backup's path
     */
	public String makeEncryptedNand (boolean addFooter) throws IOException, InterruptedException
	{
		String decryptedNand = makeDecryptedNand();
		String encryptedNand = RESOURCES_FOLDER + "\\" + new File (path).getName() + ".encrypted";
		Files.move(Paths.get(decryptedNand), Paths.get(encryptedNand), StandardCopyOption.REPLACE_EXISTING);
		String command = "twltool nandcrypt --cid " + CID + " --consoleid " + consoleID + " --in " +  encryptedNand + " --out " + encryptedNand;
		Runtime rt = Runtime.getRuntime();
		Process p = rt.exec(command);
		p.waitFor();
		if(addFooter)
		{
			Files.write(Paths.get(encryptedNand), createFooterArray(), StandardOpenOption.APPEND);
		}
		return encryptedNand;
	}
	
	/**
     * Saves the changes made to a nand backup, encrypts
     * it, and then  writes it to the folder in the 
     * specified path. A boolean value is also passed
     * as a parameter, and if it is true the backup 
     * will be made with the No$GBA footer appended to it.
     * @param addFooter a boolean indicating whether
     * or not to append the No$GBA footer to the 
     * nand backup 
     * @param savePath the path the backup will be 
     * saved to
     * @throws InterruptedException on errors from 
     * TWLtool commands
     * @throws IOException on write/read error
     */
	public void encryptAndSave (String savePath, boolean addFooter) throws IOException, InterruptedException 
	{
		Files.move(Paths.get(makeEncryptedNand(addFooter)), Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
	}
	

	

}
