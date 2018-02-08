
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
public abstract class BasicNandOperator 
{
	protected final String RESOURCES_FOLDER = "DSi_Resources";
	protected String consoleID;
	

    /**
     * Checks if there is a file or directory in this path 
     * in the nand and returns a boolean describing the 
     * result.
     * @param pathInNand the path in the nand to be checked
     * @throws IOException on write/read error
     * @return a boolean containing the result
     */
	public abstract boolean exists (String pathInNand) throws IOException;
	
	/**
     * Checks if there is a file or directory in this path 
     * in the nand and deletes it if there is. A boolean 
     * value is also returned indicating whether a file/
     * directory was found in this path
     * @param pathInNand the path in the nand containing
     * the soon-to-be-deleted file
     * @throws IOException on write/read error
     * @return a boolean describing whether the file existed
     */
	public abstract boolean delete (String pathInNand) throws IOException;
	
	/**
     * Creates a folder in the specified path, deleting
     * whatever was there before.
     * @param pathInNand the path in the nand where the 
     * folder will be created
     * @throws IOException on write/read error
     */
	public abstract void createFolder (String pathInNand) throws IOException;
	
	/**
     * Copies the file in the specified path in the nand to the
     * specified path in the computer
     * @param pathInNand the path in the nand the file will be 
     * copied from
     * @param pathInComputer the path in the nand the file will be 
     * copied to
     * @throws IOException on write/read error
     */
	public abstract void read (String pathInComputer, String pathInNand) throws IOException;
	
	/**
     * Copies the file in the specified path in the computer to the
     * specified path in the nand
     * @param pathInNand the path in the nand the file will be 
     * copied to
     * @param pathInComputer the path in the nand the file will be 
     * copied from
     * @throws IOException on write/read error
     */
	public abstract void write (String pathInComputer, String pathInNand) throws IOException;
	
	/**
     * Returns a list with the names of all the files and 
     * subfolders inside a folder in the specified path. 
     * If there is nothing in that path, or if there is 
     * a file or not a folder, the list will be null.
     * @param pathInNand the path in the nand containing
     * the folder whose entries will be listed
     * @throws IOException on write/read error
     * @return a List containing the names of all the entries
     * in the folder (null if there is no folder there)
     */
	public abstract List <String> listEntries (String pathInNand) throws IOException;
	

    /**
     * Writes the .app file on the specified path on the nand by 
     * generating a new ticket from one already on the nand, grabbing
     * and trimming the tmd, and then transferring the .app file. 
     * @param shortID the shortID of the app to be installed
     * @param appPath the location on the computer where the 
     * app file file can be found
     * @throws InterruptedException if any twltool operations fail
     * @throws IOException on write or file generation error
     */
	public void installApp(String shortID, String appPath) throws IOException, InterruptedException
	{
	    delete("title\\00030004\\" + shortID + "\\content");
	    delete("title\\00030004\\" + shortID + "\\data");
	    createFolder("title\\00030004\\" + shortID + "\\data");
	    delete("ticket\\00030004\\" + shortID + ".tik");
	    String ticket = makeTicket(shortID);
		byte [] tmdBytes = Files.readAllBytes(Paths.get(RESOURCES_FOLDER + "\\tmds\\00030004" + shortID + ".tmd"));
		byte [] trimmedTmd = new byte [520];
	    for (int x = 0; x < trimmedTmd.length; x++)
	    {
	    	trimmedTmd [x] = tmdBytes [x];
	    }
	    Files.write(Paths.get(RESOURCES_FOLDER + "\\title.tmd"), trimmedTmd);
	    write(RESOURCES_FOLDER + "\\title.tmd", "title\\00030004\\" + shortID + "\\content\\title.tmd");
	    Files.delete(Paths.get(RESOURCES_FOLDER + "\\title.tmd"));
	    
	   // write(RESOURCES_FOLDER + "\\" +shortID + ".tik", "ticket\\00030004\\" + shortID + ".tik");
	    write(ticket, "ticket\\00030004\\" + shortID + ".tik");
	    Files.delete(Paths.get(ticket));
	    
	    String appFileNumber = Integer.toHexString(tmdBytes[487]);
	    if(appFileNumber.length() == 1)
	    appFileNumber = "0" + appFileNumber;
	    write(appPath, "title\\00030004\\" + shortID + "\\content\\000000" + appFileNumber + ".app");
	}
	

	
    /**
     * Installs an exploited save file on the nand for the game
     * with the specified shortID. 
     * @param shortID the shortID of the exploitable app
     * @throws IOException on write/read error, and possibly if the app
     * is not an exploitable app
     */
	public void installHaxSave(String shortID) throws IOException
	{
		write(RESOURCES_FOLDER + "\\hax_saves\\00030004" + shortID +".sav", "title\\00030004\\" + shortID + "\\data\\public.sav");
	}
	
    /**
     * Generates a ticket for the specified shortID in the Resources
     * folder using the passed shortID and the nand's consoleID
     * @param shortID the shortID for which the ticket will be made
     * @throws InterruptedException if any twltool operations fail
     * @throws IOException on write/read error
     * @return a String containing the location where the ticket 
     * is stored
     */
	public String makeTicket (String shortID) throws IOException, InterruptedException
	{
		List <String> tickets = listEntries("ticket\\00030004");
		String sampletik = null;
		Iterator <String> ticketIterator = tickets.iterator();
		while (ticketIterator.hasNext() && sampletik == null)
		{
			String ticket = ticketIterator.next();
			if (ticket.contains("tik"))
			{
				sampletik = ticket;
			}
		}
		if(sampletik == null)
		{
			throw new IOException ();
		}
		read(RESOURCES_FOLDER + "\\sample_ticket.tik", "ticket\\00030004\\" + sampletik);
		Runtime rt = Runtime.getRuntime();
		rt.exec("twltool syscrypt --consoleid " + consoleID + " --in " +RESOURCES_FOLDER + "\\sample_ticket.tik --out " + RESOURCES_FOLDER + "\\sampletik_dec.tik ").waitFor();
		Files.delete(Paths.get(RESOURCES_FOLDER + "\\" + "sample_ticket.tik"));
		byte [] ticketTemplate = Files.readAllBytes(Paths.get(RESOURCES_FOLDER + "\\sampletik_dec.tik"));
		Files.delete(Paths.get(RESOURCES_FOLDER + "\\" + "sampletik_dec.tik"));
		byte [] shortIDBytes = new byte [4];
		for (int x = 0; x < 8; x+=2)
		{
			shortIDBytes[x/2] = (byte) Integer.parseInt(shortID.substring(x, x+2), 16);
		}
		for (int x = 0; x < 4; x++)
		{
			ticketTemplate [x + 480] = shortIDBytes[x];
		}
		Files.write(Paths.get(RESOURCES_FOLDER + "\\" + shortID + "_dec.tik"), ticketTemplate);
		rt.exec("twltool syscrypt --consoleid " + consoleID + " --in " + RESOURCES_FOLDER + "\\" +  shortID + "_dec.tik --out " + RESOURCES_FOLDER + "\\" + shortID + ".tik --encrypt").waitFor();
		Files.delete(Paths.get(RESOURCES_FOLDER + "\\" + shortID + "_dec.tik"));
		return RESOURCES_FOLDER + "\\" + shortID + ".tik";
		
	}
	
    /**
     * Generates a ticket for the specified shortID using the passed 
     * shortID and the nand's consoleID, and saves it to the specified
     * path on the computer
     * @param shortID the shortID for which the ticket will be made
     * @param path The path on which the ticket will be saved 
     * @throws InterruptedException if any twltool operations fail
     * @throws IOException on write/read error
     */
	public void saveTicketTo(String shortID, String path) throws IOException, InterruptedException
	{
		makeTicket(shortID);
		Files.move(Paths.get(shortID + ".tik"), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
	}

	

	
	/**
     * Installs a system firmware title of the specified long ID
     * using the .app file and tmd in the folder on the specified
     * path in the computer. 
     * @param title the long ID of the system title
     * @param folderPath the path to the folder containing the 
     * title files
     * @throws IOException on write/read error
     */
	public void installVersionTitle(String title, String folderPath) throws IOException
	{
		String longIDPrefix = title.substring(0, 8);
		String shortID = title.substring(8, title.length());
		List <String> entries = listEntries("title\\" + longIDPrefix + "\\" + shortID + "\\content");
		for (File file : new File(folderPath).listFiles())
		{
			if(file.getName().contains("app"))
			{
				for (String titleFile : entries)
				{
					if(titleFile.contains("app"))
					{
						delete("title\\" + longIDPrefix + "\\" + shortID + "\\content\\" + titleFile);
					}
				}
				write(file.getAbsolutePath(), "title\\" + longIDPrefix + "\\" + shortID + "\\content\\" + file.getName());
			}
			if(file.getName().contains("tmd"))
			{
				for (String titleFile : entries)
				{
					if(titleFile.contains("tmd"))
					{
						delete("title\\" + longIDPrefix + "\\" + shortID + "\\content\\" + titleFile);
					}
				}
				write(file.getAbsolutePath(), "title\\" + longIDPrefix + "\\" + shortID + "\\content\\title.tmd");
				
			}
		}
	}
	
	/**
     * Installs a new whitelist using the .app file and
     * tmd in the folder on the specified path in the computer. 
     * @param whitelistFolderPath the path to the folder containing the 
     * title files
     * @throws IOException on write/read error
     */
	public void installWhitelist (String whitelistFolderPath) throws IOException
	{
		installVersionTitle("0003000f484e4841", whitelistFolderPath);
	}
	
	
	/**
     * Downgrades to 1.4 using a "titles" folder from NUSDownloader 
     * (found in the specified path) that should have all of the files 
     * already downloaded.
     * @param NUSTitlesPath the path to a "titles" folder from NUSDownloader
     * that has all of the 1.4 downgrade files in it. 
     * @throws IOException on write/read error
     */
	public void downgradeTo1_4FromNUS (String NUSTitlesPath) throws IOException
	{
		downgradeFromNUS("4", "512", "256", "512", NUSTitlesPath);
	}
	
	

	
	/**
     * Installs the system titles with the specified versions 
     * using a "titles" folder from NUSDownloader (found in the
     * specified path) that should have all of the files
     * already downloaded.
     * If any of the version Strings are null or blank, or 
     * if the folders are missing files, that title will be
     * skipped. 
     * @param versionDataVersion the desired version for the 
     * version data title
     * @param launcherVersion the desired version for the 
     * title launcher
     * @param whitelistVersion the desired version for the 
     * whitelist
     * @param settingsVersion the desired version for system
     * settings
     * @param NUSTitlesPath the location in the computer of 
     * NUSDownloader's title folder
     * @throws IOException on write/read error
     */
	public void downgradeFromNUS (String versionDataVersion, String launcherVersion, String whitelistVersion, String settingsVersion, String NUSTitlesPath) throws IOException
	{
		
		String [] regions = {"43", "45", "4A", "4B", "50", "55"};
		for (String region : regions)
		{
			if (exists("title\\00030017\\484e41" + region))
			{
				String [] versions = {versionDataVersion, launcherVersion, whitelistVersion, settingsVersion};
				String [] titles = {"0003000f484e4c" + region, "00030017484e41" + region, "0003000f484e4841", "00030015484e42" + region};
				for (int x = 0; x < titles.length; x++)
				{
					if(verifyNUSTitle(titles[x], versions[x], NUSTitlesPath))
					{
						installVersionTitle(titles[x], NUSTitlesPath + "\\" + titles[x] + "\\" + versions[x]);
					}
				}
			}
		}
	}
	
	/**
     * Checks to make sure that a title from NUS about to be 
     * installed is present and complete. 
     * @param title The title that will be verified 
     * @param version The version of the title 
     * @param NUSTitlesPath the location in the computer of 
     * NUSDownloader's title folder
     * @throws IOException on write/read error
     */
	private boolean verifyNUSTitle (String title, String version, String NUSTitlesPath) throws IOException
	{
		if (version == null || version.equals(""))
		{
			return false;
		}
		boolean containsTMD = false;
		boolean containsApp = false;
		File titleFolder = new File(NUSTitlesPath + "\\" + title + "\\" + version);
		if (!titleFolder.exists() || !titleFolder.isDirectory())
		{
			return false;
		}
		for (File file : titleFolder.listFiles())
		{
			if(file.getName().contains("app"))
			{
				containsApp = true;
			}
			if(file.getName().contains("tmd"))
			{
				containsTMD = true;
			}
		}
		return containsTMD && containsApp;
	}
	
}
