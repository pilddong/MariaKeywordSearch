package functions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

public class Filtering {
	static HashSet<String> filter;
	static boolean check = false;
	public Filtering() throws FileNotFoundException {
		if(check == false) {
			filter = new HashSet<String>();
	        Scanner scan = new Scanner(new File("./total_filtering.txt"));
	        while (scan.hasNext()) {
	            filter.add(scan.next());
	        }		
	        check = true;
		}
	}
	
	public static HashSet<String> filteringList() {
        return filter;
    }
	

}


