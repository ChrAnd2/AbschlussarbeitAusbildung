import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

public class Ausgabe {
	
	private File file;
	private BufferedWriter bw;
	
	/**
	 * Erstellt ein Objekt Ausgabe
	 * @param outputPath Pfad der Ausgabedatei
	 */
	public Ausgabe(String outputPath){
		file = new File(outputPath);
		try {
			if(file.exists()){
				file.delete();
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Schreibt einen String in die Ausgabedatei.
	 * Enthält der String "\n" wird an diesem eine neue Zeile bekommen.
	 * @param s zu schreibener Text
	 */
	public void schreibe(String s){
		String[] temp = s.split("\n");
		try {
			bw = new BufferedWriter(new FileWriter(file,true));
			for(int i = 0; i<temp.length;i++){
				bw.write(temp[i]);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Schreibt eine Exception und ihre Informationen in die Ausgabedatei.
	 * @param exception String der Exception bpsw. e.toString()
	 */
	public void schreibeException(String exception){
		exception = "\n\n*************************************\nEs ist ein Fehler aufgetreten.\n*************************************\n" + exception;
		String[] temp = exception.split("\n");
		try {
			bw = new BufferedWriter(new FileWriter(file,true));
			for(int i = 0; i<temp.length;i++){
				bw.write(temp[i]);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Schreibt die Ausgabe zu dem Übergebenen Netzplan
	 * @param titel Titel des Testbeispiels
	 * @param vorgaenge Alle Vorgänge als String mit \n getrennt
	 * @param av Liste aller Startvorgänge
	 * @param ev Liste aller Endvorgänge
	 * @param gesamtD Gesamtdauer des Netzplans
	 * @param kp Liste aller Kritischen Pfade
	 */
	public void schreibeNetzplanAusgabe(String titel, String vorgaenge, ArrayList<Integer> av, ArrayList<Integer> ev, double gesamtD, ArrayList<ArrayList<Integer>> kp){
		String ausgabe = titel + "\n\n" + "Vorgangsnummer; Vorgangsbezeichnung; D; FAZ; FEZ; SAZ; SEZ; GP; FP\n";
		ausgabe += vorgaenge + "\n";
		ausgabe += "Anfangsvorgang: ";
		for(int i = 0; i<av.size()-1; i++)
			ausgabe += av.get(i)+",";
		ausgabe += av.get(av.size()-1) + "\n";
		ausgabe += "Endvorgang: ";
		for(int i = 0; i<ev.size()-1; i++)
			ausgabe += ev.get(i)+",";
		ausgabe += ev.get(ev.size()-1) +"\n";
		if(ev.size()>1){
			ausgabe += "Maximale Gesamtdauer (da mehrere Endzustaende): " + gesamtD + "\n\n";
		}else{
			ausgabe += "Gesamtdauer: " + gesamtD + "\n\n";
		}
		if(kp.size()>1)
			ausgabe += "Kritische Pfade:\n";
		else
			ausgabe += "Kritischer Pfad:\n";
		
		for(int i = 0; i<kp.size(); i++){
			ausgabe += (i+1) +". ";
			for(int j = 0; j<kp.get(i).size()-1; j++){
				ausgabe += kp.get(i).get(j) + "->";
			}
			ausgabe += kp.get(i).get(kp.get(i).size()-1) + "\n";
		}
		this.schreibe(ausgabe);
	}
	
	/**
	 * Gibt die Ausgabe für den übergebenen Netzplan auf der Konsole aus.
	 * @param titel Titel des Testbeispiels
	 * @param vorgaenge Alle Vorgänge als String mit \n getrennt
	 * @param av Liste aller Startvorgänge
	 * @param ev Liste aller Endvorgänge
	 * @param gesamtD Gesamtdauer des Netzplans
	 * @param kp Liste aller Kritischen Pfade
	 */
	public void logNetzplanAusgabe(String titel, String vorgaenge, ArrayList<Integer> av, ArrayList<Integer> ev, double gesamtD, ArrayList<ArrayList<Integer>> kp){
		System.out.println(titel);
		System.out.println();
		System.out.println("Vorgangsnummer; Vorgangsbezeichnung; D; FAZ; FEZ; SAZ; SEZ; GP; FP");
		String[] temp = vorgaenge.split("\n");
		for (int i = 0; i<temp.length;i++){
		    System.out.println(temp[i]);
		}
		System.out.println();
		System.out.print("Anfangsvorgang: ");
		for(int i = 0; i<av.size()-1; i++)
			System.out.print(av.get(i)+",");
		System.out.println(av.get(av.size()-1));
		System.out.print("Endvorgang: ");
		for(int i = 0; i<ev.size()-1; i++)
			System.out.print(ev.get(i)+",");
		System.out.println(ev.get(ev.size()-1));
		if(ev.size()>1){
			System.out.println("Maximale Gesamtdauer (da mehrere Endzustaende): " + gesamtD);
		}else{
			System.out.println("Gesamtdauer: " + gesamtD);
		}
		System.out.println();
		if(kp.size()>1)
			System.out.println("Kritische Pfade:");
		else
			System.out.println("Kritischer Pfad:");
		
		for(int i = 0; i<kp.size(); i++){
			System.out.print((i+1) +". ");
			for(int j = 0; j<kp.get(i).size()-1; j++){
				System.out.print(kp.get(i).get(j) + "->");
			}
			System.out.print(kp.get(i).get(kp.get(i).size()-1));
			System.out.println();
		}
	}
	
	/**
	 * Gibt den Dateiinhalt, der sich im String s befindet in der Konsole aus
	 * @param s Dateiinhalt
	 */
	public void logDateiinhalt(String s){
		String[] temp = s.split("\n");
		for(int i = 0; i<temp.length;i++){
			System.out.println(temp[i]);
		}
	}
	
	/**
	 * Gibt eine aufgetretene Exception in der Konsole aus
	 * @param exception String der Exception. z.B. e.toString() 
	 */
	public void logException(String exception){
		exception = "\n\n*************************************\nEs ist ein Fehler aufgetreten.\n*************************************\n" + exception;
		String[] temp = exception.split("\n");
		for(int i = 0; i<temp.length;i++){
			System.out.println(temp[i]);
		}
	}

}
