# NandInAJar
A reusable Java component for operations on DSi Nand backups

This is a Java library for editing DSi Nand Backups. 

Dependencies:
https://github.com/waldheinz/fat32-lib

What you can do with this:
Write and read individual files to/from a nand backup file
Create a new backup with the edits that you made, either decrypted, encrypted, or encrypted with the footer on it
Create a ticket for any title ID using a Nand's console ID
Install a new app to a backup by specifying the path to the app file and the short ID of the app
Downgrade individual system titles
Install system titles from a NUSDownloader "titles" folder 
Downgrade automatically to 1.4 using a NUSDownloader "titles" folder
Check if a file exists on the backup 

For the correct code usage, please check out the javaDoc documentation included in the code. 

Do note that if you make something with this, all of the files in the "Necessary files" pack will need to be present in the same folder as whatever you make. 


If you have any questions on anything, open an issue and I'll answer them there. 
