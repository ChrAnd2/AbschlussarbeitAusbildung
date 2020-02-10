import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Eingabe {

	private String inputPath;
	private String titel;
	
	/**
	 * Erstellt das Objekt Eingabe. 
	 * @param inputPath Pfad der Eingabedatei
	 */
	public Eingabe(String inputPath){
		this.inputPath = inputPath;
	}
	
	/**
	 * Liest die Eingansdaten ein und überprüft diese auf Fehler.
	 * Für eine Liste und genauere Beschreibung der Fehler, die 
	 * auftreten können, lesen Sie bitte die Dokumentation.
	 * @return HashMap, dessen Key die ID jedes Vorgangs ist.
	 * @throws EingabeException
	 */
	public HashMap<Integer,Vorgang> lesen() throws EingabeException{
		HashMap<Integer,Vorgang> vorgaenge = new HashMap<>();
		ArrayList<Integer> ids = new ArrayList<>();
		ArrayList<Integer> vorNachIds = new ArrayList<>();
		int gotTitel = 0;
		int id;
		String bez;
		double d;
		int[] vor, nach;
		int countZeilen = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(inputPath)));
			while(br.ready()){
				String line = br.readLine();
				//Leerzeilen werden ignoriert
				if(line.equals(""))
					continue;
				
				//Titel steht in dieser Zeile
				if(line.length() > 3 && line.substring(0, 3).equals("//+")){ 			
					if(gotTitel == 0){
						titel = line.substring(3);
						titel = titel.trim();
						gotTitel++;
					}else{
						throw new EingabeException("Es ist mehr als ein Titel für das Testbeispiel angegeben");
					}
				}
				// Ist die Zeile auch keine Kommentarzeile, wird versucht ein Vorgang einzulesen.
				else if(!line.substring(0,2).equals("//")){
					line = line.trim();
					String temp[] = line.split(";");
					// Da es 5 Attribute gibt, die mit einem Semikolon getrennt sind muss die länge 5 sein.
					if(temp.length != 5){
						throw new EingabeException("Semikolonfehler im " + (countZeilen+1) + " Vorgang.");
					}
					try{
						//Leerzeichen am Anfand oder am Ende werden ignoriert
						temp[0] = temp[0].trim();
						id = Integer.parseInt(temp[0]);
					}
					catch(NumberFormatException e){
						throw new EingabeException("Vorgangsnummer des " + (countZeilen+1) + ". Vorgangs ist nicht ganzzahlig.");
					}
					if(id < 0)
						throw new EingabeException("Die ID des " + (countZeilen+1) + " Vorgangs ist negativ.");
					if(ids.contains(id))
						throw new EingabeException("Der Vorgang mit der Vorgangsnummer " + id + " existiert mehrmals.");
					else
						ids.add(id);
					
					//Die Bezeichnung wird ohne Leerzeichen am Anfang oder ende übernommen.
					bez = temp[1].trim();
					
					try{
						//Leerzeichen am Anfand oder am Ende werden ignoriert
						temp[2] = temp[2].trim();
						d = Double.parseDouble(temp[2]);
					}
					catch(NumberFormatException e){
						throw new EingabeException("Die Dauer des Vorgangs mit der Vorgangsnummer " + id + " ist keine Zahl.");
					}
					// Abschneiden der Dauer auf die zweite Nachkommastelle genau.
					d = ((int)(d*100))/100.0;
					if(d<0)
						throw new EingabeException("Die Dauer des Vorgangs mit der Vorgangsnummer " + id + " ist nicht größter gleich Null.");
					
					temp[3] = temp[3].trim();
					if(temp[3].equals("-")){
						vor = new int[0];
					}else{
						String temp2[] = temp[3].split(",");
						vor = new int[temp2.length];
						for(int i = 0; i<temp2.length; i++){
							try{
								temp2[i] = temp2[i].trim();
								if(temp2[i].contains(" "))
									throw new EingabeException("Vorgänger der Vorgangsnummer " + id + "enthalten ein Leerzeichen. Evlt. ein fehlendes Komma?");
								vor[i] = Integer.parseInt(temp2[i]);
								if(vor[i] == id)
									throw new EingabeException("Der Vorgang mit der Vorgangsnummer " + id + " ist sein eigener Vorgänger.");
								if(!vorNachIds.contains(vor[i]))
									vorNachIds.add(vor[i]);
							}
							catch(NumberFormatException e){
								throw new EingabeException("Vorgangsnummer eines Vorgängers des Vorgangs mit der Vorgangsnummer " + id + " ist keine ganzzahlige Zahl.");
							}
						}
					}
					
					temp[4] = temp[4].trim();
					if(temp[4].equals("-")){
						nach = new int[0];
					}else{
						String temp2[] = temp[4].split(",");
						nach = new int[temp2.length];
						for(int i = 0; i<temp2.length; i++){
							try{
								temp2[i] = temp2[i].trim();
								if(temp2[i].contains(" "))
									throw new EingabeException("Nachfolger der Vorgangsnummer " + (countZeilen+1) + " enthalten ein Leerzeichen. Evlt. ein fehlendes Komma?");
								nach[i] = Integer.parseInt(temp2[i]);
								if(nach[i] == id)
									throw new EingabeException("Der Vorgang mit der Vorgangsnummer " + id + " ist sein eigener Nachfolger.");
								if(!vorNachIds.contains(nach[i]))
									vorNachIds.add(nach[i]);
							}
							catch(NumberFormatException e){
								throw new EingabeException("Vorgangsnummer eines Nachfolgers des " + (countZeilen+1) + ". Vorgangs ist keine ganzzahlige Zahl.");
							}
						}
					}
					for(int i = 0; i<vor.length; i++){
						for(int j = 0; j<nach.length; j++){
							if(vor[i] == nach[j]){
								throw new EingabeException("Der Vorgang mit der Vorgangsnummer " + id + " hat einen gleichen Vorgänger und Nachfolger.");
							}
						}
					}
					vorgaenge.put(id, new Vorgang(id,bez,d,vor,nach));
					countZeilen++;
					
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			throw new EingabeException("Die Eingabedatei konnte nicht gefunden werden.");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * Wenn eine ID als Vorgänger oder Nachfolger angegeben ist die nicht existiert,
		 * wird diese hier abgefangen
		 */
		for(int i = 0; i<vorNachIds.size();i++){
			if(!ids.contains(vorNachIds.get(i))){
				throw new EingabeException("Der Vorgang mit der Vorgangsnummer " + vorNachIds.get(i) + " existiert nicht.");
			}
		}
		
		/*
		 * Ist ein Vorgang als Nachfolger angegeben, muss dieser
		 * auch als Vorgänger den anderen angegeben haben.
		 */
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
			for(int i = 0; i<e.getValue().getNach().length; i++){
				int tmp = e.getValue().getNach()[i];
				boolean gefunden = false;
				for(int j = 0; j<vorgaenge.get(tmp).getVor().length;j++){
					if( e.getKey() == vorgaenge.get(tmp).getVor()[j])
						gefunden = true;
				}
				if(gefunden == false)
					throw new EingabeException("Der Vorgang " + tmp + " ist Nachfolger von " + e.getKey() + ". " + e.getKey() + " ist aber nicht Vorgänger von Vorgang " + tmp + ".");
			}
		}
		if(gotTitel == 0)
			throw new EingabeException("Es wurde kein Titel für das Testbeispiel angegeben.");
		if(countZeilen == 0)
			throw new EingabeException("Die Datei enthält keine Vorgänge.");
		return vorgaenge;
	}
	
	/**
	 * Gibt den Dateiinhalt als String zurück.
	 * @return Dateiinhalt
	 */
	public String getDateiinhalt(){
		String dateiinhalt = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(inputPath)));
			while(br.ready()){
				dateiinhalt += br.readLine() + "\n";
			}
			br.close();
			return dateiinhalt;
			
		} catch (FileNotFoundException e) {
			throw new EingabeException("Die Eingabedatei konnte nicht gefunden werden.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dateiinhalt;
	}
	
	/**
	 * Gibt den Titel des Beispiels als String zurück
	 * @return Titel des Testbeispiels
	 */
	public String getTitel(){
		return titel;
	}
	
}
