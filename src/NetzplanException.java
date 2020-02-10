
public class NetzplanException extends RuntimeException{

	public NetzplanException(){
		super("Es liegt ein Fehler im Netzplan vor.");
	}
	
	public NetzplanException(String msg){
		super(msg);
	}
}
