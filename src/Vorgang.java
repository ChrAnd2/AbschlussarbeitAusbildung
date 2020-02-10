
public class Vorgang {

	private int id;
	private String bez;
	private double faz = -1,fez,saz,sez = -1,d,gp,fp;
	private boolean kritisch= false;
	private int[] vor,nach;
	
	/**
	 * Erstellt einen Vorgang mit den Übergebenen Werten
	 * @param id Vorgangsnummer
	 * @param bez Vorgangsbezeichnung
	 * @param d Dauer des Vorgangs
	 * @param vor IDs der Vorgänger des Vorgangs
	 * @param nach IDs der Nachfolger des Vorgangs
	 */
	public Vorgang(int id, String bez, double d, int[] vor, int[] nach){
		this.id = id;
		this.bez = bez;
		this.d = d;
		this.vor = vor;
		this.nach = nach;
	}
	
	/**
	 * Wandelt den Vorgang mit all seinen informationen in einen String um
	 */
	public String toString(){
		return id + "; " + bez + "; " + d + "; " + faz + "; " + fez + "; " + saz + "; " + sez + "; " + gp + "; " + fp;
	}
	
	/**
	 * Gibt an, ob der Vorgang ein Startvorgang ist.
	 * @return True falls Startvorgang
	 */
	public boolean istStartvorgang(){
		if(vor.length == 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Gibt an, ob der Vorgang ein Endvorgang ist.
	 * @return True falls Endvorgang
	 */
	public boolean istEndvorgang(){
		if(nach.length == 0)
			return true;
		else
			return false;
	}

	public double getFaz() {
		return faz;
	}

	public void setFaz(double faz) {
		this.faz = faz;
	}

	public double getFez() {
		return fez;
	}

	public void setFez(double fez) {
		this.fez = fez;
	}

	public double getSaz() {
		return saz;
	}

	public void setSaz(double saz) {
		this.saz = saz;
	}

	public double getSez() {
		return sez;
	}

	public void setSez(double sez) {
		this.sez = sez;
	}

	public double getGp() {
		return gp;
	}

	public void setGp(double gp) {
		this.gp = gp;
	}

	public double getFp() {
		return fp;
	}

	public void setFp(double fp) {
		this.fp = fp;
	}

	public boolean isKritisch() {
		return kritisch;
	}

	public void setKritisch(boolean kritisch) {
		this.kritisch = kritisch;
	}

	public int getId() {
		return id;
	}

	public String getBez() {
		return bez;
	}

	public double getD() {
		return d;
	}

	public int[] getVor() {
		return vor;
	}

	public int[] getNach() {
		return nach;
	}
	
	
}
