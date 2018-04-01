import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.collections.ObservableList; 
import javafx.geometry.Insets; 

public class Main extends Application 
{
	
	static String CID;
	static String consoleID;
	DecryptedNandOperator nand;

	public static void main(String[] args) throws IOException, InterruptedException
	{

		if(Files.exists(Paths.get("nand.bin")))
			Files.delete(Paths.get("nand.bin"));
		if(Files.exists(Paths.get("DSi_Resources//nand.bin.pre")))
			Files.delete(Paths.get("DSi_Resources//nand.bin.pre"));
		if(Files.exists(Paths.get("DSi_Resources//nand.bin.post")))
			Files.delete(Paths.get("DSi_Resources//nand.bin.post"));
		if(Files.exists(Paths.get("DSi_Resources//nand.bin.partition0")))
			Files.delete(Paths.get("DSi_Resources//nand.bin.partition0"));
			
		if (Files.exists(Paths.get("consoleID.txt")))
		{
			Scanner ID = new Scanner (new File("consoleID.txt"));
			consoleID = ID.next();
			ID.close();
			
			
		}
		
		if (Files.exists(Paths.get("CID.txt")))
		{
			Scanner ID = new Scanner (new File("CID.txt"));
			CID = ID.next();
			ID.close();
			
			
		}
		//if(!Files.exists(Paths.get("DSi_Resources//tempNand_runSettings.dat")))
		if (true)
		{
			
			FileChannel channel = new RandomAccessFile(new File("tempNand_runSettings"), "rw").getChannel();
			FileLock lock = channel.tryLock();
			if(lock!=null)
				launch(args);
			else {
				System.exit(0);
			}
		}

	}

	 @Override
	  public void start(Stage primaryStage) {


	    BorderPane root = new BorderPane();
	    Scene scene = new Scene(root/*, 300, 250, Color.WHITE*/);
	    
	 
		Button button1 = new Button("Downgrade to 1.4");  
	     button1.setDisable(true);
	     button1.setOnAction(new EventHandler <ActionEvent> () {

			public void handle(ActionEvent event) 
			{	
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle("Please select the NUSDownloader \"titles\" folder");
                File selectedDirectory = 
                        directoryChooser.showDialog(primaryStage);
                
                if(selectedDirectory != null){
                    try 
                    {
                    	nand.downgradeTo1_4FromNUS(selectedDirectory.getAbsolutePath());
					} catch (IOException e) 
                    {
						handleException(e);
					}
                }
			}
	    	 
	     });
	      //Creating button2 
	      Button button2 = new Button("Install app");       
	      button2.setDisable(true);
	      button2.setOnAction(new EventHandler <ActionEvent> () {

				public void handle(ActionEvent event) 
				{	
					TextInputDialog dialog = new TextInputDialog("");
					 
					dialog.setTitle("DsiWare app short ID");
					dialog.setHeaderText("Enter your DSiWare app's 8-digit short ID:");
					dialog.setContentText("DSiWare short ID:");
					 

					Optional<String> result = dialog.showAndWait();
					if(result.isPresent() && result.get().length()== 8)
					{
						FileDialog fd = new FileDialog((java.awt.Frame) null, "Please navigate to the DSiWare .app file", FileDialog.LOAD); 
						fd.setVisible(true);
						fd.dispose();
						if(fd.getFile()!=null)
							try {
								nand.installApp(result.get(), fd.getDirectory()+fd.getFile());
							} catch (IOException | InterruptedException e) {
								// TODO Auto-generated catch block
								handleException(e);
							}
					}
				}
		    	 
		     });
	      //Creating button3
	      Button button3 = new Button("Install exploit app");       
	      button3.setDisable(true);
	      button3.setOnAction(new EventHandler <ActionEvent> () {

			@Override
			public void handle(ActionEvent event) {
				Stage newWindow = new Stage();
				BorderPane root = new BorderPane();
		        ChoiceBox choiceBox = new ChoiceBox();

		        Map <String, String> haxApps = new TreeMap <String, String> ();
		        
		        haxApps.put("Sudoku (USA)", "4b344445");
		        haxApps.put("Sudoku (EUR)", "4b344456");
		        haxApps.put("FieldRunners (USA)", "4b464445");
		        haxApps.put("FieldRunners (EUR)", "4b464456");
		        haxApps.put("Legends of Exidia (USA)", "4b4c4545");
		        haxApps.put("Legends of Exidia (EUR)", "4b4c4556");
		        haxApps.put("Legends of Exidia (JAP)", "4b4c454a");
		        haxApps.put("Zelda - Four Swords (USA)", "4b513945");
		        haxApps.put("Zelda - Four Swords (EUR)", "4b513956");
		        
		        choiceBox.getItems().addAll(haxApps.keySet());
		        HBox hbox = new HBox(choiceBox);

		        Button button1 = new Button("Install");
		        button1.setOnAction(new EventHandler <ActionEvent> () {

					@Override
					public void handle(ActionEvent event) {
						if(choiceBox.getValue()!=null)
						{
							FileDialog fd = new FileDialog((java.awt.Frame) null, "Please navigate to the DSiWare .app file", FileDialog.LOAD); 
							fd.setVisible(true);
							fd.dispose();
							if(fd.getFile()!=null)
								try {
									nand.installApp(haxApps.get((String)choiceBox.getValue()), fd.getDirectory()+fd.getFile());
									nand.installHaxSave(haxApps.get((String)choiceBox.getValue()));
								} catch (IOException | InterruptedException e) {
									// TODO Auto-generated catch block
									handleException(e);
								}
								newWindow.close();
							}
					}
		        	
		        });
		        root.setBottom(button1);
		        root.setCenter(hbox);
                Scene secondScene = new Scene(root, 200, 75);
 
                // New window (Stage)
                
                newWindow.setTitle("Install exploit app");
                newWindow.setScene(secondScene);
 
                // Set position of second window, related to primary window.
                newWindow.setX(primaryStage.getX() + 200);
                newWindow.setY(primaryStage.getY() + 100);
 
                newWindow.show();
				
			}
	    	  
	      });
	      //Creating button4 
	     // Button button4 = new Button("Button4");       
	      //button4.setDisable(true);
	      //Creating a Flow Pane 
	      FlowPane flowPane = new FlowPane();    
	       
	      //Setting the horizontal gap between the nodes 
	      flowPane.setHgap(25); 
	       
	      //Setting the margin of the pane  
	      flowPane.setMargin(button1, new Insets(20, 0, 20, 20)); 
	       
	      //Retrieving the observable list of the flow Pane 
	      ObservableList list = flowPane.getChildren(); 
	      
	      //Adding all the nodes to the flow pane 
	      list.addAll(button1, button2, button3); 
	        
	      //Creating a scene object 
	    //  Scene otherscene = new Scene(flowPane);  
	      
	      //Setting title to the Stage 
	      primaryStage.setTitle("TempNand"); 
	    
	    
	    MenuBar menuBar = new MenuBar();
	    menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
	    root.setTop(menuBar);
	    root.setCenter(flowPane);
	    // File menu - new, save, exit
	    Menu fileMenu = new Menu("File");
	    
	    Menu nandOpenMenu = new Menu("Open");
	    MenuItem decrypted = new MenuItem("Open Decrypted Nand");
	    decrypted.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try 
                {
					setupNand(false);
				} catch (IllegalArgumentException e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("IDs not found");
					alert.setHeaderText("It appears that the CID and/or Console ID are not set up correctly.");

					alert.showAndWait();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					handleException(e);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					handleException(e);
				}
            }
        });
	    
	    MenuItem encrypted = new MenuItem("Open Encrypted Nand");
	    encrypted.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
					setupNand(true);
					if(nand!=null)
						openNandButtons();
					else
					{
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Nand not opened successfully");
						alert.setHeaderText("A nand file valid for the given IDs was not provided.");

						alert.showAndWait();
					}
				} catch (IllegalArgumentException e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("IDs not found");
					alert.setHeaderText("It appears that the CID and/or Console ID are not set up correctly.");

					alert.showAndWait();
					
				} catch (IllegalStateException e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Improper Encryption");
					alert.setHeaderText("The nand seems to be valid, but not properly encrypted.");

					alert.showAndWait();
                } catch (IOException e) {
					// TODO Auto-generated catch block
					handleException(e);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					handleException(e);
				}
            }

			private void openNandButtons() 
			{
				button1.setDisable(false);
				button2.setDisable(false);
				button3.setDisable(false);
			//	button4.setDisable(false);
			}
        });
	    
	    nandOpenMenu.getItems().addAll(encrypted);
	    
	    MenuItem saveMenuItem = new MenuItem("Save as");
	    saveMenuItem.setOnAction(new EventHandler <ActionEvent> () {

			@Override
			public void handle(ActionEvent event) {
				if(nand!=null)
				{
					FileDialog fd = new FileDialog((java.awt.Frame) null, "Save nand to?", FileDialog.SAVE); 
					fd.setVisible(true);
					fd.dispose();
					try {
						if(fd.getFile()!=null)
							nand.encryptAndSave(fd.getDirectory()+fd.getFile(), false);
					} 
					catch(NoSuchFileException e)
					{
						
					}
					catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						handleException(e);
					}
				}
				
			}
	    	
	    });
	    MenuItem saveNoCashItem = new MenuItem("Save for No$GBA");
	    saveNoCashItem.setOnAction(new EventHandler <ActionEvent> () {

			@Override
			public void handle(ActionEvent event) {
				if (nand != null)
				{
					FileDialog fd = new FileDialog((java.awt.Frame) null, "Save No$GBA nand to?", FileDialog.SAVE); 
					fd.setVisible(true);
					fd.dispose();
					try 
					{
						if(fd.getFile()!=null) 
						{
							nand.encryptAndSave(fd.getDirectory()+fd.getFile(), true);
						}
					} 
					catch(NoSuchFileException e)
					{
						
					}
					catch (IOException | InterruptedException e) {
						// TODO Auto-generated catch block
						handleException(e);
					}
				}
			}
	   
	    });
	    MenuItem saveDecrypted = new MenuItem("Save decrypted nand as");
	    saveDecrypted.setOnAction(new EventHandler <ActionEvent> () {

			@Override
			public void handle(ActionEvent event) {
				if (nand != null)
				{
					FileDialog fd = new FileDialog((java.awt.Frame) null, "Save No$GBA nand to?", FileDialog.SAVE); 
					fd.setVisible(true);
					fd.dispose();
					try 
					{
						if(fd.getFile()!=null) 
						{
							nand.save(fd.getDirectory()+fd.getFile());
							nand.makePost0(fd.getDirectory()+fd.getFile());
						}
							//nand.encryptAndSave(fd.getDirectory()+fd.getFile(), true);
					} 
					catch(NoSuchFileException e)
					{
						
					}
					catch (IOException e/* | InterruptedException e*/) {
						// TODO Auto-generated catch block
						handleException(e);
					}
				}
				
			}
	    	
	    });
	    MenuItem exitMenuItem = new MenuItem("Exit");
	    exitMenuItem.setOnAction(actionEvent -> Platform.exit());

	    fileMenu.getItems().addAll(nandOpenMenu, saveMenuItem, saveNoCashItem, saveDecrypted);

	    Menu setupMenu = new Menu("Setup");
	    Menu CID = new Menu("CID");
	    MenuItem typeCID = new MenuItem("Type in CID");
	    MenuItem fetchCID = new MenuItem("Get CID from file");
	    fetchCID.setOnAction(new EventHandler <ActionEvent> () {

			@Override
			public void handle(ActionEvent event) {
				try {
					verifyAndEnterCID(NandUtilities.extractCID());
				} 
				
				catch (FileNotFoundException e)
				{
					
				}
				
				catch (IOException e) {
					
					handleException(e);
				}
				
				catch (IllegalStateException e)
				{
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Failed to obtain CID");
					alert.setHeaderText("It appears that the file is an invalid CID.bin file");

					alert.showAndWait();
				}
				
			}
	    	
	    });
	    typeCID.setOnAction(new EventHandler <ActionEvent> () {

			@Override
			public void handle(ActionEvent event) {try {
				TextInputDialog dialog = new TextInputDialog("");
				 
				dialog.setTitle("CID");
				dialog.setHeaderText("Enter your CID:");
				dialog.setContentText("CID:");
				 

				Optional<String> result = dialog.showAndWait();
				verifyAndEnterCID(result.get());
				}
				catch(NoSuchElementException e)
				{
					
				}
			catch(IllegalStateException e)
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Not a valid ID");
				alert.setHeaderText("The CID entered is invalid");

				alert.showAndWait();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				handleException(e);
			}
			}
	    	
	    });
	    CID.getItems().addAll(
	    		fetchCID,
		        typeCID);
	    Menu consoleID = new Menu("Console ID");
	    MenuItem typeConsoleID = new MenuItem("Type in Console ID");
	    typeConsoleID.setOnAction(new EventHandler <ActionEvent> () {

			@Override
			public void handle(ActionEvent event) { 
			try {
					TextInputDialog dialog = new TextInputDialog("");
					 
					dialog.setTitle("Console ID");
					dialog.setHeaderText("Enter your Console ID:");
					dialog.setContentText("Console ID:");
					 
	
					Optional<String> result = dialog.showAndWait();
					verifyAndEnterConsoleID(result.get());
				
			}

			catch(NoSuchElementException e)
			{
				
			}
			catch(IllegalStateException e)
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Not a valid ID");
				alert.setHeaderText("The Console ID entered is invalid");

				alert.showAndWait();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				handleException(e);
			}
			}
	    });
	    MenuItem fetchConsoleID = new MenuItem("Get ConsoleID from file");
	    fetchConsoleID.setOnAction(new EventHandler <ActionEvent> () {

			
			public void handle(ActionEvent event) 
			{
				try 
				{
					verifyAndEnterConsoleID(NandUtilities.extractConsoleID());
					
					
				} 
				catch(FileNotFoundException e)
				{
					
				}
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					handleException(e);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					handleException(e);
				}
				catch(IllegalStateException e)
				{
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Not a valid ID");
					alert.setHeaderText("It seems this file is not a valid SRL backup");

					alert.showAndWait();
				}
				
			}
	    
	    });
	    consoleID.getItems().addAll(
	    		fetchConsoleID,
		        typeConsoleID);
	    
	    setupMenu.getItems().addAll(CID, consoleID);
	    menuBar.getMenus().addAll(fileMenu, setupMenu);

	    primaryStage.setScene(scene);
	    primaryStage.setOnCloseRequest(new EventHandler <WindowEvent> () {

			@Override
			public void handle(WindowEvent event) {
				if(nand!=null)
					try {		
						if(Files.exists(Paths.get("nand.bin")))
							Files.delete(Paths.get("nand.bin"));/*
						if(Files.exists(Paths.get("DSi_Resources//nand.bin.pre")))
							Files.delete(Paths.get("DSi_Resources//nand.bin.pre"));
						if(Files.exists(Paths.get("DSi_Resources//nand.bin.post")))
							Files.delete(Paths.get("DSi_Resources//nand.bin.post"));
						if(Files.exists(Paths.get("DSi_Resources//nand.bin.partition0")))
							Files.delete(Paths.get("DSi_Resources//nand.bin.partition0"));
						*/
						nand.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						handleException(e);
					}
				
			}});
	    primaryStage.show();
	  }
    
	private static void setupConsoleID() throws IOException, InterruptedException, IllegalStateException
	{
		FileDialog fd = new FileDialog((java.awt.Frame) null, "Please navigate to the DSiWare extract from System Settings", FileDialog.LOAD); 
		fd.setVisible(true);
		fd.dispose();
		String path = fd.getDirectory()+fd.getName();
		String consoleID = NandUtilities.extractCID(path);
		verifyAndEnterConsoleID(consoleID);
	}

	private static void setupCID() throws IOException, InterruptedException, IllegalStateException
	{
		FileDialog fd = new FileDialog((java.awt.Frame) null, "Please navigate to the CID.bin file", FileDialog.LOAD); 
		fd.setVisible(true);
		fd.dispose();
		String path = fd.getDirectory()+fd.getName();
		String CID = NandUtilities.extractConsoleID(path);
		if(CID == null || CID.length()!=32)
		{
			throw new IllegalStateException();
		}
	}
	
	private static void verifyAndEnterConsoleID(String possibleID) throws IOException
	{
		if(possibleID == null || possibleID.length()!=16)
		{
			throw new IllegalStateException();
		}
		else
		{
			consoleID = possibleID;
			if (Files.exists(Paths.get("consoleID.txt")))
			{
				Files.delete(Paths.get("consoleID.txt"));
			}
			PrintStream consoleIDFile = new PrintStream(new File("consoleID.txt"));
			consoleIDFile.print(consoleID);
		}
	}
	
	
	
	private static void verifyAndEnterCID(String possibleID) throws IOException
	{
		if(possibleID == null || !Pattern.compile("^([A-Fa-f0-9]{32})$").matcher(possibleID).matches())
		{
			throw new IllegalStateException();
		}
		else
		{
			CID = possibleID;
			if (Files.exists(Paths.get("CID.txt")))
			{
				Files.delete(Paths.get("CID.txt"));
			}
			PrintStream CIDFile = new PrintStream(new File("CID.txt"));
			CIDFile.print(CID);
		}
	}

	private void setupNand(boolean encrypted) throws IOException, IllegalStateException, InterruptedException
	{
		if (CID == null || consoleID==null)
		{
			throw new IllegalArgumentException();
		}
		FileDialog fd = new FileDialog((java.awt.Frame) null, "Please navigate to the encrypted nand backup", FileDialog.LOAD); 
		fd.setVisible(true);
		fd.dispose();
		
		if(fd.getFile()!=null)
		{
			Files.copy(Paths.get(fd.getDirectory()+fd.getFile()), Paths.get("nand_init.bin"));
			if(encrypted)
			{
				String command = "twltool nandcrypt --cid " + CID + " --consoleid " + consoleID + " --in nand_init.bin --out nand.bin";
				Runtime rt = Runtime.getRuntime();
				Process p = rt.exec(command);
				p.waitFor();
				Files.delete(Paths.get("nand_init.bin"));
			}
			else
			{
				Files.move(Paths.get("nand_init.bin"), Paths.get("nand.bin"));
			}
			if(new File("nand.bin").length()!=0)
			{
				nand = new DecryptedNandOperator("nand.bin", CID, consoleID);
			}
			Files.delete(Paths.get("nand.bin"));
		}
	}
	
	public static void handleException(Exception e) 
	{
		
		Alert alert = 
		        new Alert(AlertType.ERROR, 
		            "An error has occurred, a description will be generated. Would you like to save the description?",
		             ButtonType.OK, 
		             ButtonType.CANCEL);
		alert.setTitle("An error has occurred");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {
			try
			{
				FileDialog fd = new FileDialog((java.awt.Frame) null, "Save error description to?", FileDialog.SAVE); 
				fd.setVisible(true);
				fd.dispose();
				
				if(fd.getFile()!=null)
				{
					PrintStream f = new PrintStream(new File(fd.getDirectory()+fd.getFile()));
					e.printStackTrace(f);
				}
				
			}
			catch (FileNotFoundException ex)
			{
				Alert alert1 = new Alert(AlertType.ERROR);
				alert1.setTitle("Error");
				alert1.setHeaderText("The error description could not be saved.");

				alert1.showAndWait();
			}
		}
		
	}
}
