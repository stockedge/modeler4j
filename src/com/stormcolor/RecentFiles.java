package src.com.stormcolor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class RecentFiles {
	private Modeler modeler;

	public RecentFiles(Modeler modeler) {		
		this.modeler = modeler;
		
	}
	
	public String getRecentFiles() {
		// retrieve the last opened files
		String linePref = null;
		String propertyValue = "";
		try {
            FileReader fileReader = new FileReader("recentFiles.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((linePref = bufferedReader.readLine()) != null) {
            	propertyValue += linePref;
            }    

            bufferedReader.close();            
        }
        catch(Exception ex) {
        }
		
		return propertyValue;
	}
	
	public void addRecentFile(File file) {
		String files = getRecentFiles();
		
		files += ","+file.getAbsolutePath();
		
		String[] filesArray = files.split(",");		
		String propertyValueTMP = "";
		String sep = "";
		for(int n=((filesArray.length > 10)?1:0); n < filesArray.length; n++) {
			if(!filesArray[n].equals("")) {
				propertyValueTMP += sep+filesArray[n];
				sep = ",";
			}
		}
		files = propertyValueTMP;
		
		
		// write to file
		try {
            FileWriter fileWriter = new FileWriter("recentFiles.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(files);
            
            bufferedWriter.close();
        }
        catch(Exception ex) {
        }
	}
}
