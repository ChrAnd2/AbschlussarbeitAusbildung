import java.util.Date;

public class Main {
	
	/**
	 * Startet die Netzplanerstellung. Es muss mindestens die Eingabedatei als Übergabeparameter angegeben sein.
	 * @param args enthält die Übergabeparameter. Für die verschiedenen möglichkeiten kann -h für Help aufgerufen werden.
	 */
	public static void main(String args[]){
		boolean log = false;
		String outputPath = "";
		String inputPath = "";
		if(args.length == 0){
			throw new IllegalArgumentException("Es muss eine Eingabedatei angegeben werden!");
		}else{
			for(int i = 0; i<args.length-1;i++){
				switch(args[i].toLowerCase()){
				case "-h": showHelp(); break;
				case "-l": log = true; break;
				case "-o": outputPath = args[i+1]; i++; break;
				case "-t": 
					String path = System.getProperty("user.dir");
					path = path.replace("dist", "Tests");
					inputPath = path + "\\";
				}
			}
			inputPath += args[args.length-1];
		}
		Netzplanerstellung npe;
		if(outputPath.equals(""))
			npe = new Netzplanerstellung(inputPath,log);
		else
			npe = new Netzplanerstellung(inputPath, outputPath, log);
		npe.run();
	}

	/**
	 * Zeigt die Hilfe für die Übergabeparameter an.
	 */
	public static void showHelp(){
		System.out.println("-l aktiviert die Ausgabe in der Konsole.");
		System.out.println("-o [outputPath] Die Ausgabe wird in die übergebene Datei outputPath geschrieben.");
		System.out.println("-t Die Eingabedatei muss nicht den kompletten Pfad enthalten. Es wird davon ausgegangen, dass Sie sich in 'Tests' befindet.");
		System.exit(0);
	}
}
