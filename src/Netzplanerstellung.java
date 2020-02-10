import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Netzplanerstellung {
	
	private ArrayList<Integer> kritischerPfad = new ArrayList<>();
	private ArrayList<ArrayList<Integer>> kritischePfade = new ArrayList<ArrayList<Integer>>();
	private HashMap<Integer,Vorgang> vorgaenge = new HashMap<>();
	private ArrayList<Integer> idsAv = new ArrayList<>();
	private ArrayList<Integer> idsEv = new ArrayList<>();
	private Eingabe eing;
	private Ausgabe ausg;
	private boolean log;
	String titel;
	
	/**
	 * Erstellt das Objekt Netzplanerstellung mit vorgegebener Eingabe und Ausgabedatei.
	 * @param inputPath Pfad der Eingabedatei
	 * @param outputPath Pfad der Ausgabedatei
	 * @param log Boolean ob es eine Konsolenausgabe geben soll.
	 */
	public Netzplanerstellung(String inputPath, String outputPath, boolean log){
		eing = new Eingabe(inputPath);
		ausg = new Ausgabe(outputPath);
		this.log = log;
	}
	
	/**
	 * Erstellt das Objekt Netzplanerstellung mit vorgegebener Eingabedatei.
	 * Die Ausgabedatei hat die Endung .out und den selben Namen wie die Eingabedatei.
	 * @param inputPath Pfad der Eingabedatei
	 * @param log Boolean ob es eine Konsolenausgabe geben soll.
	 */
	public Netzplanerstellung(String inputPath, boolean log){
		eing = new Eingabe(inputPath);
		ausg = new Ausgabe(inputPath.replace(".in", ".out"));
		this.log = log;
	}
	
	/**
	 * Die Methode startet die komplette Netzplanerstellung
	 */
	public void run(){
		try{
			//Einlesen der Eingangsdatei
			vorgaenge = eing.lesen();
			//Einlesen des Titels
			titel = eing.getTitel();
		}catch(EingabeException e){
			/* 
			 * Im Falle eines Fehlers in der Eingabedatei wird dieser
			 * zusammen mit dem Inhalt der Eingabedatei in die 
			 * Ausgabedatei geschrieben. Ist log aktiviert, wird der 
			 * Fehler zusätzlich in der Konsole ausgegeben. 
			 */
			ausg.schreibe(eing.getDateiinhalt());
			ausg.schreibeException(e.toString());
			if(log){
				ausg.logDateiinhalt(eing.getDateiinhalt());
				ausg.logException(e.toString());
			}
			System.exit(1);
		}
		try{
			//Prüfung des Netzplans
			pruefeNetzplan();
		}catch(NetzplanException e){
			/* 
			 * Im Falle eines Fehlers im Netzplan wird dieser
			 * zusammen mit dem Inhalt der Eingabedatei in die 
			 * Ausgabedatei geschrieben. Ist log aktiviert, wird der 
			 * Fehler zusätzlich in der Konsole ausgegeben. 
			 */
			ausg.schreibe(eing.getDateiinhalt());
			ausg.schreibeException(e.toString());
			if(log){
				ausg.logDateiinhalt(eing.getDateiinhalt());
				ausg.logException(e.toString());
			}
			System.exit(1);
		}
		/*
		 * Die erste Phase des Algorithmus wird von jedem Endvorgang
		 * aus aufgerufen. Ist der Algorithmus mit einem Endvorgang
		 * fertig, wird dessen SEZ berechnet. Außerdem wird die ID
		 * jedes Endvorgangs in idsEv gespeichert. 
		 */
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
		    if(e.getValue().istEndvorgang()){
		    	this.idsEv.add(e.getKey());
		    	erstePhase(e.getValue());
		    	e.getValue().setSez(e.getValue().getFez());
		    }
		}
		/*
		 * Die zweite Phase des Algorithmus wird von jedem Startvorgang
		 * aus aufgerufen. Die ID jedes Startvorgangs wird in idsAv gespeichert. 
		 */
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
		    if(e.getValue().istStartvorgang()){
		    	this.idsAv.add(e.getKey());
		    	zweitePhase(e.getValue());
		    }
		}
		
		// Für die Ausgabe wird ein String mit allen Vorgängen generiert
		String strVorgaenge = "";
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
		    strVorgaenge += e.getValue().toString() + "\n";
		}
		
		/*
		 * Die Gesamtlaufzeit wird berechnet. Bei mehreren Endvorgängen
		 * wird das Maximum der Gesamtlaufzeiten ausgegeben. 
		 */
		double maxTime = 0;
		if(idsEv.size()>1){
			for(int i = 0; i<idsEv.size(); i++){
				if(vorgaenge.get(idsEv.get(i)).getSez() > maxTime)
					maxTime = vorgaenge.get(idsEv.get(i)).getSez();
			}
		}else{
			maxTime = vorgaenge.get(idsEv.get(idsEv.size()-1)).getSez();
		}
		
		/*
		 * Der Kritische Pfad wird von jedem Startvorgang aus berechnet.
		 * Alle gefundenen Pfade werden in der ArrayListe kritischePfade gespeichert.
		 */
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
		    if(e.getValue().istStartvorgang()){
				this.getKritischenPfad(e.getValue());
				kritischerPfad.clear();
		    }
		}
		
		// Ist log = True, wird dieser auf der Konsole ausgegeben.
		if(log)
			ausg.logNetzplanAusgabe(titel, strVorgaenge, idsAv, idsEv, maxTime, kritischePfade);
		// Schreiben der Ausgabedatei.
		ausg.schreibeNetzplanAusgabe(titel, strVorgaenge, idsAv, idsEv, maxTime, kritischePfade);
	}
	
	/**
	 * Die Methode berechnet ausgehend von einem Endvorgang
	 * rekursiv die erste Phase des Algorithmuses.
	 * Es werden FAZ und FEZ jedes Vorgangs berechnet. 
	 * @param v Endvorgang
	 * @return Gibt den FEZ des aktuellen Vorgangs zurück
	 */
	private double erstePhase(Vorgang v){
		double max = 0;
		if(v.getFaz() == -1){ //Es wird auf -1 überprüft, da der FAZ aller Vorgänge, außer der Startvorgänge mit -1 initialisiert werden.
			for(int i = 0; i<v.getVor().length; i++){
				double temp = erstePhase(vorgaenge.get(v.getVor()[i]));
				if(temp >= max){
					max = temp;
				}
			}
			v.setFaz(max);
			v.setFez(v.getFaz() + v.getD());
			return v.getFez();
		}
		else{
			//Abbruch Bedingung: Startvorgang
			v.setFez(v.getFaz() + v.getD());
			return v.getFez();
		}
	}
	
	/**
	 * Die Methode berechnet ausgehen von einem Startvorgang
	 * rekursiv die zweite Phase des Algorithmuses.
	 * Es werden die restlichen Werte berechnet (SEZ,SAZ,GP,FP,Kritisch)
	 * @param v Startvorgang
	 * @return gibt den SAZ des aktuellen Vorgangs zurück
	 */
	private double zweitePhase(Vorgang v){
		double min = Integer.MAX_VALUE;
		double minFaz = Integer.MAX_VALUE;
		if(v.getSez() == -1){//Es wird auf -1 überprüft, da der SEZ aller Vorgänge, außer der Endvorgänge mit -1 beträgt.
			for(int i = 0; i<v.getNach().length; i++){
				double temp = zweitePhase(vorgaenge.get(v.getNach()[i]));
				if(temp <= min){
					min = temp;
				}
				if(vorgaenge.get(v.getNach()[i]).getFaz() <= minFaz){
					minFaz = vorgaenge.get(v.getNach()[i]).getFaz();
				}
			}
			v.setSez(min);
			v.setSaz(v.getSez() - v.getD());
			v.setGp(v.getSez() - v.getFez());
			v.setFp(minFaz - v.getFez());
			if(v.getFp() == 0 && v.getGp() == 0){
				v.setKritisch(true);
			}
			return v.getSaz();
		}
		else{ //Abbruch Bedinung: Endvorgang
			v.setSaz(v.getSez() - v.getD());
			v.setGp(v.getSez() - v.getFez());
			v.setFp(0);
			if(v.getFp() == 0 && v.getGp() == 0){
				v.setKritisch(true);
			}
			return v.getSaz();
		}
	}
	
	/**
	 * Berechnet ausgehend von einem Startvorgang 
	 * rekursiv alle Kritische Pfade dieses Startvorgangs.
	 * Die methode benutzt die private Variable kritischerPfad 
	 * und speichert die gefundenen Pfade in der Variablen kritischePfade
	 * @param v Startvorgang
	 */
	private void getKritischenPfad(Vorgang v){
		if(v.istEndvorgang()){
			kritischerPfad.add(v.getId());
			kritischePfade.add(new ArrayList<Integer>(kritischerPfad));
			kritischerPfad.clear();
		}else{
			if(v.isKritisch()){
				if(!kritischerPfad.contains(v.getId()))
					kritischerPfad.add(v.getId());
				ArrayList<Integer> aktuellerPfad = new ArrayList<Integer>(kritischerPfad);
				for(int i = 0; i<v.getNach().length; i++){
					getKritischenPfad(vorgaenge.get(v.getNach()[i]));
					kritischerPfad = new ArrayList<Integer>(aktuellerPfad);
				}
			}
		}
		
	}
	
	/**
	 * Überprüft den Netzplan auf alle möglichen Fehlerfälle. 
	 * Für eine Liste und genauere Beschreibung der Fehlerfälle
	 * schauen Sie bitte in die Dokumentation.
	 * @throws NetzplanException
	 */
	private void pruefeNetzplan() throws NetzplanException{
		int anzEndvorgaenge= 0, anzStartvorgaenge = 0;
		ArrayList<Integer> endvorgaenge = new ArrayList<>();
		if(vorgaenge.size()<2)
			throw new NetzplanException("Es gibt weniger als zwei Vorgänge.");
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
			if(e.getValue().istStartvorgang() && e.getValue().istEndvorgang())
				throw new NetzplanException("Der Vorgang mit der Vorgangsnummer " + e.getKey() + " hat weder Vorgänger oder Nachfolger und kann daher nicht erreicht werden.");
		    if(e.getValue().istEndvorgang()){
		    	anzEndvorgaenge++;
		    	endvorgaenge.add(e.getKey());
		    }
		    if(e.getValue().istStartvorgang())
		    	anzStartvorgaenge++;
		}
		if(anzStartvorgaenge == 0)
			throw new NetzplanException("Der Netzplan enthält keinen Startvorgang.");
		if(anzEndvorgaenge == 0)
			throw new NetzplanException("Der Netzplan enthält keinen Endvorgang.");
		
		//Überprüfung auf Zyklen
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
			if(e.getValue().istEndvorgang()){
				pruefeZyklus(e.getValue(),new ArrayList<Integer>());
			}
		}
		
		//Überprüfung ob der Netzplan zusammenhängend ist.
		if(!pruefeZusammenhaengend(endvorgaenge))
			throw new NetzplanException("Der Netzplan ist nicht zusammenhängend.");
	}
	
	/**
	 * Prüft ob der Netzplan zusammenhängt.
	 * Grundgedanke des Verfahrens ist, dass nur Startvorgänge, die in mehr als einem 
	 * Endvorgang enden zwei Netzpläne miteinander verbinden können.
	 * Es wird daher ermittelt, welche Endzustände mit solchen Startvorgängen erreicht werden können.
	 * Gibt es Startvorgänge, dessen Endzustand nicht einer der erreichbaren Endzustände ist,
	 * kann der Netzplan nicht zusammenhängen. Für weitere Informationen lesen Sie bitte die Dokumentation.
	 * @param endvorgaenge Liste aller möglichen Endvorgaenge
	 * @return true wenn Netzplan zusammenhängt, false wenn nicht.
	 */
	private boolean pruefeZusammenhaengend(ArrayList<Integer> endvorgaenge){
		ArrayList<ArrayList<Integer>> listen = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> erreichbareEv = new ArrayList<>();
		ArrayList<Integer> nichtVerbunden = new ArrayList<>();
		/*
		 * Für jeden Startvorgang wird ermittelt, in welchen Endzuständen er endet.
		 * Diese Liste wird wiederum in einer weiteren Liste gespeichert, so dass
		 * am Ende in der Variablen listen so viele Einträge wie Startvorgänge enthalten sind.
		 * In den unterlisten stehen dann die entsprechenden Endzustände.
		 */
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
			if(e.getValue().istStartvorgang()){
				listen.add(getEndvorgaenge(e.getValue()));
			}
		}
		
		//Gibt es nur einen Startvorgang ist der Netzplan auf jedenfall zusammenhängend.
		if(listen.size() == 1)
			return true;
		
		for(int i = 0; i<listen.size();i++){
			if(listen.get(i).size() > 1){		//Jede Liste die mindestens 2 Endvorgänge hat
				if(erreichbareEv.isEmpty()){	//Die erste Liste darf alle übernehmen, da sie als "Ausgangsnetzplan" genutzt wird.
					erreichbareEv = listen.get(i);
				}else{
					boolean verknuepft = false;	//Überprüfen ob die aktuelle Liste einen der derzeit erreichbaren Endvorgänge beinhaltet.
					for(int j = 0; j<listen.get(i).size();j++){
						if(erreichbareEv.contains(listen.get(i).get(j))){
							verknuepft = true;
						}
					}
					if(verknuepft){				
						//Wenn ja, werden alle neuen Endvorgänge zu den erreichbaren Endvorgängen gespeichert.
						for(int j = 0; j<listen.get(i).size();j++){
							if(!erreichbareEv.contains(listen.get(i).get(j))){
								erreichbareEv.add(listen.get(i).get(j));
							}
						}
						//Nachdem neue Endvorgänge hinzugefügt wurden, wird kontrolliert ob ein nichtverbundender nun verbunden ist.
						for(int j = 0; j<nichtVerbunden.size();j++){
							for(int k = 0; k<listen.get(nichtVerbunden.get(j)).size();k++){
								if(!erreichbareEv.contains(listen.get(nichtVerbunden.get(j)).get(k))){
									erreichbareEv.add(listen.get(nichtVerbunden.get(j)).get(k));
									nichtVerbunden.remove(nichtVerbunden.get(j));
								}
							}
						}
					}else{
						/*
						 *  Enthält die aktuelle Liste keinen erreichbaren Endvorgang, 
						 *  wird diese als noch nicht Verbunden gespeichert.
						 */
						nichtVerbunden.add(i); 
					}
				}
			}
		}
		//Gibt es nur Anfangszustände mit einem Endzustand, müssen alle den gleichen Endzustand haben
		if(erreichbareEv.size() == 0){					
			for(int i = 0; i<listen.size()-1;i++){
				if(listen.get(i).equals(listen.get(i+1)))
					return true;
				else
					return false;
			}
		}
		//Die Liste aller erreichbarer Endzustände muss gleich der Liste aller Endzustände sein.
		if(endvorgaenge.equals(erreichbareEv))
			return true;
		else
			return false;
	}
	
	/**
	 * Gibt alle Endvorgänge zurück, die von einem Vorgang aus erreicht werden können
	 * @param v Vorgang dessen Endvorgänge berechnet werden sollen.
	 * @return Gibt eine Liste zurück, die die Ids aller möglichen Endvorgänge enthält
	 */
	private ArrayList<Integer> getEndvorgaenge(Vorgang v){
		if(v.istEndvorgang()){
			ArrayList<Integer> endvorgaenge = new ArrayList<>();
			endvorgaenge.add(v.getId());
			return endvorgaenge;
		}else{
			ArrayList<Integer> endvorgaenge = new ArrayList<>();
			for(int i = 0; i<v.getNach().length; i++){
				ArrayList<Integer> liste = getEndvorgaenge(vorgaenge.get(v.getNach()[i]));
				for(int j = 0; j<liste.size();j++){
					if(!endvorgaenge.contains(liste.get(j))){
						endvorgaenge.add(liste.get(j));
					}
				}
			}
			return endvorgaenge;
		}
	}
	
	/**
	 * Prüft ausgehend von einem Endvorgang rekursiv den aktuellen Netzplan auf Zyklen.
	 * Grundgedanke des Verfahrens ist, dass man nacheinander von jedem Endvorgang aus startet,
	 * und richtung Startvorgang durch den Netzplan geht. Dabei merkt man sich alle bereits erreichbaren
	 * Vorgänge. Bei jedem Vorgang wird geprüft ob er selbst bereits erreicht werden kann.
	 * Ist das der Fall, liegt ein Zyklus vor.
	 * @param v Endvorgang
	 * @param list Liste der bereits erreichbaren Vorgänge
	 */
	private void pruefeZyklus(Vorgang v,ArrayList<Integer> list){
		if(v.istStartvorgang()){
			if(list.contains(v.getId()))
				throw new NetzplanException("Der Netzplan enthält einen Zyklus. Betroffen ist der Vorgang mit der Vorgangsnummer " + v.getId());
		}else{
			if(list.contains(v.getId()))
				throw new NetzplanException("Der Netzplan enthält einen Zyklus. Betroffen ist der Vorgang mit der Vorgangsnummer " + v.getId());
			for(int i = 0; i<v.getNach().length;i++){
				if(!list.contains(v.getNach()[i]))
					list.add(v.getNach()[i]);
			}
			for(int i = 0; i<v.getVor().length;i++)
				pruefeZyklus(vorgaenge.get(v.getVor()[i]),new ArrayList<Integer>(list));
			
		}
	}
}
