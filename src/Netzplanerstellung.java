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
			 * Fehler zus�tzlich in der Konsole ausgegeben. 
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
			//Pr�fung des Netzplans
			pruefeNetzplan();
		}catch(NetzplanException e){
			/* 
			 * Im Falle eines Fehlers im Netzplan wird dieser
			 * zusammen mit dem Inhalt der Eingabedatei in die 
			 * Ausgabedatei geschrieben. Ist log aktiviert, wird der 
			 * Fehler zus�tzlich in der Konsole ausgegeben. 
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
		 * fertig, wird dessen SEZ berechnet. Au�erdem wird die ID
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
		
		// F�r die Ausgabe wird ein String mit allen Vorg�ngen generiert
		String strVorgaenge = "";
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
		    strVorgaenge += e.getValue().toString() + "\n";
		}
		
		/*
		 * Die Gesamtlaufzeit wird berechnet. Bei mehreren Endvorg�ngen
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
	 * @return Gibt den FEZ des aktuellen Vorgangs zur�ck
	 */
	private double erstePhase(Vorgang v){
		double max = 0;
		if(v.getFaz() == -1){ //Es wird auf -1 �berpr�ft, da der FAZ aller Vorg�nge, au�er der Startvorg�nge mit -1 initialisiert werden.
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
	 * @return gibt den SAZ des aktuellen Vorgangs zur�ck
	 */
	private double zweitePhase(Vorgang v){
		double min = Integer.MAX_VALUE;
		double minFaz = Integer.MAX_VALUE;
		if(v.getSez() == -1){//Es wird auf -1 �berpr�ft, da der SEZ aller Vorg�nge, au�er der Endvorg�nge mit -1 betr�gt.
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
	 * �berpr�ft den Netzplan auf alle m�glichen Fehlerf�lle. 
	 * F�r eine Liste und genauere Beschreibung der Fehlerf�lle
	 * schauen Sie bitte in die Dokumentation.
	 * @throws NetzplanException
	 */
	private void pruefeNetzplan() throws NetzplanException{
		int anzEndvorgaenge= 0, anzStartvorgaenge = 0;
		ArrayList<Integer> endvorgaenge = new ArrayList<>();
		if(vorgaenge.size()<2)
			throw new NetzplanException("Es gibt weniger als zwei Vorg�nge.");
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
			if(e.getValue().istStartvorgang() && e.getValue().istEndvorgang())
				throw new NetzplanException("Der Vorgang mit der Vorgangsnummer " + e.getKey() + " hat weder Vorg�nger oder Nachfolger und kann daher nicht erreicht werden.");
		    if(e.getValue().istEndvorgang()){
		    	anzEndvorgaenge++;
		    	endvorgaenge.add(e.getKey());
		    }
		    if(e.getValue().istStartvorgang())
		    	anzStartvorgaenge++;
		}
		if(anzStartvorgaenge == 0)
			throw new NetzplanException("Der Netzplan enth�lt keinen Startvorgang.");
		if(anzEndvorgaenge == 0)
			throw new NetzplanException("Der Netzplan enth�lt keinen Endvorgang.");
		
		//�berpr�fung auf Zyklen
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
			if(e.getValue().istEndvorgang()){
				pruefeZyklus(e.getValue(),new ArrayList<Integer>());
			}
		}
		
		//�berpr�fung ob der Netzplan zusammenh�ngend ist.
		if(!pruefeZusammenhaengend(endvorgaenge))
			throw new NetzplanException("Der Netzplan ist nicht zusammenh�ngend.");
	}
	
	/**
	 * Pr�ft ob der Netzplan zusammenh�ngt.
	 * Grundgedanke des Verfahrens ist, dass nur Startvorg�nge, die in mehr als einem 
	 * Endvorgang enden zwei Netzpl�ne miteinander verbinden k�nnen.
	 * Es wird daher ermittelt, welche Endzust�nde mit solchen Startvorg�ngen erreicht werden k�nnen.
	 * Gibt es Startvorg�nge, dessen Endzustand nicht einer der erreichbaren Endzust�nde ist,
	 * kann der Netzplan nicht zusammenh�ngen. F�r weitere Informationen lesen Sie bitte die Dokumentation.
	 * @param endvorgaenge Liste aller m�glichen Endvorgaenge
	 * @return true wenn Netzplan zusammenh�ngt, false wenn nicht.
	 */
	private boolean pruefeZusammenhaengend(ArrayList<Integer> endvorgaenge){
		ArrayList<ArrayList<Integer>> listen = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> erreichbareEv = new ArrayList<>();
		ArrayList<Integer> nichtVerbunden = new ArrayList<>();
		/*
		 * F�r jeden Startvorgang wird ermittelt, in welchen Endzust�nden er endet.
		 * Diese Liste wird wiederum in einer weiteren Liste gespeichert, so dass
		 * am Ende in der Variablen listen so viele Eintr�ge wie Startvorg�nge enthalten sind.
		 * In den unterlisten stehen dann die entsprechenden Endzust�nde.
		 */
		for (Entry<Integer,Vorgang> e : vorgaenge.entrySet()){
			if(e.getValue().istStartvorgang()){
				listen.add(getEndvorgaenge(e.getValue()));
			}
		}
		
		//Gibt es nur einen Startvorgang ist der Netzplan auf jedenfall zusammenh�ngend.
		if(listen.size() == 1)
			return true;
		
		for(int i = 0; i<listen.size();i++){
			if(listen.get(i).size() > 1){		//Jede Liste die mindestens 2 Endvorg�nge hat
				if(erreichbareEv.isEmpty()){	//Die erste Liste darf alle �bernehmen, da sie als "Ausgangsnetzplan" genutzt wird.
					erreichbareEv = listen.get(i);
				}else{
					boolean verknuepft = false;	//�berpr�fen ob die aktuelle Liste einen der derzeit erreichbaren Endvorg�nge beinhaltet.
					for(int j = 0; j<listen.get(i).size();j++){
						if(erreichbareEv.contains(listen.get(i).get(j))){
							verknuepft = true;
						}
					}
					if(verknuepft){				
						//Wenn ja, werden alle neuen Endvorg�nge zu den erreichbaren Endvorg�ngen gespeichert.
						for(int j = 0; j<listen.get(i).size();j++){
							if(!erreichbareEv.contains(listen.get(i).get(j))){
								erreichbareEv.add(listen.get(i).get(j));
							}
						}
						//Nachdem neue Endvorg�nge hinzugef�gt wurden, wird kontrolliert ob ein nichtverbundender nun verbunden ist.
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
						 *  Enth�lt die aktuelle Liste keinen erreichbaren Endvorgang, 
						 *  wird diese als noch nicht Verbunden gespeichert.
						 */
						nichtVerbunden.add(i); 
					}
				}
			}
		}
		//Gibt es nur Anfangszust�nde mit einem Endzustand, m�ssen alle den gleichen Endzustand haben
		if(erreichbareEv.size() == 0){					
			for(int i = 0; i<listen.size()-1;i++){
				if(listen.get(i).equals(listen.get(i+1)))
					return true;
				else
					return false;
			}
		}
		//Die Liste aller erreichbarer Endzust�nde muss gleich der Liste aller Endzust�nde sein.
		if(endvorgaenge.equals(erreichbareEv))
			return true;
		else
			return false;
	}
	
	/**
	 * Gibt alle Endvorg�nge zur�ck, die von einem Vorgang aus erreicht werden k�nnen
	 * @param v Vorgang dessen Endvorg�nge berechnet werden sollen.
	 * @return Gibt eine Liste zur�ck, die die Ids aller m�glichen Endvorg�nge enth�lt
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
	 * Pr�ft ausgehend von einem Endvorgang rekursiv den aktuellen Netzplan auf Zyklen.
	 * Grundgedanke des Verfahrens ist, dass man nacheinander von jedem Endvorgang aus startet,
	 * und richtung Startvorgang durch den Netzplan geht. Dabei merkt man sich alle bereits erreichbaren
	 * Vorg�nge. Bei jedem Vorgang wird gepr�ft ob er selbst bereits erreicht werden kann.
	 * Ist das der Fall, liegt ein Zyklus vor.
	 * @param v Endvorgang
	 * @param list Liste der bereits erreichbaren Vorg�nge
	 */
	private void pruefeZyklus(Vorgang v,ArrayList<Integer> list){
		if(v.istStartvorgang()){
			if(list.contains(v.getId()))
				throw new NetzplanException("Der Netzplan enth�lt einen Zyklus. Betroffen ist der Vorgang mit der Vorgangsnummer " + v.getId());
		}else{
			if(list.contains(v.getId()))
				throw new NetzplanException("Der Netzplan enth�lt einen Zyklus. Betroffen ist der Vorgang mit der Vorgangsnummer " + v.getId());
			for(int i = 0; i<v.getNach().length;i++){
				if(!list.contains(v.getNach()[i]))
					list.add(v.getNach()[i]);
			}
			for(int i = 0; i<v.getVor().length;i++)
				pruefeZyklus(vorgaenge.get(v.getVor()[i]),new ArrayList<Integer>(list));
			
		}
	}
}
