
public class EingabeException extends RuntimeException{

	public EingabeException(){
		super("Es liegt ein Fehler bei den Eingabedaten -paramtern vor.");
	}
	
	public EingabeException(String msg){
		super(msg);
	}
}
