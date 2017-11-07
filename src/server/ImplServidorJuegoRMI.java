package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import common.IntCallbackCliente;
import common.IntServidorJuegoRMI;
import common.IntServidorPartidasRMI;

public class ImplServidorJuegoRMI extends UnicastRemoteObject implements IntServidorJuegoRMI{

	private HashMap<String,IntCallbackCliente> mapCallbacks;
	
	public ImplServidorJuegoRMI() throws RemoteException{
		super();
		mapCallbacks = new HashMap<>();
	}
	
	
	public IntServidorPartidasRMI nuevoServidorPartidas() throws RemoteException {
		// TODO Auto-generated method stub
		return new ImplServidorPartidasRMI();
	}

	
	public synchronized boolean proponPartida(String nombreJugador, IntCallbackCliente callbackClientObject) throws RemoteException {
		// TODO Auto-generated method stub
		if (mapCallbacks.containsKey(nombreJugador)) {//Si ya tenia propuesta partida devuelve false
			System.out.println("El jugador "+nombreJugador+" ha intentado proponer una partida, pero ya tenía una.");
			return false;
		}
		mapCallbacks.put(nombreJugador, callbackClientObject);
		System.out.println("El jugador "+nombreJugador+" ha propuesto una partida.");
		return true;
	}

	
	public synchronized boolean borraPartida(String nombreJugador) throws RemoteException {
		// TODO Auto-generated method stub
		if (mapCallbacks.containsKey(nombreJugador)){ //Devuelve true si el jugador tenia una partida que borrar
			mapCallbacks.remove(nombreJugador);
			System.out.println("El jugador "+nombreJugador+" ha borrado su partida propuesta.");
			return true;
		}
		System.out.println("El jugador "+nombreJugador+" ha intentado borrar su partida, pero no ha propuesto ninguna.");
		return false;
	}

	
	public synchronized String[] listaPartidas() throws RemoteException {
		// TODO Auto-generated method stub
		System.out.println("Un jugador ha pedido la lista de partidas propuestas.");
		String[] res = new String[mapCallbacks.size()];
		int i=0;
		for (String partida : mapCallbacks.keySet()){
			res[i++]=partida;
		}
		return res;
	}

	
	public synchronized boolean aceptaPartida(String nombreJugador, String nombreRival) throws RemoteException {
		// TODO Auto-generated method stub
		if (!mapCallbacks.containsKey(nombreRival)) {//Si el rival no ha propuesto partida devuelve false
			System.out.println("El jugador "+nombreJugador+" ha intentado aceptar una partida de "+nombreRival+", pero este no tiene ninguna.");
			return false;
		}
		try{
			mapCallbacks.get(nombreRival).notificame(nombreJugador); //Intenta hacer callback, si no puede devuelve false
		}catch(Exception e){
			System.out.println("El rival "+ nombreRival+" no esta conectado y su partida ha sido borrada.");
			return false;
		}finally {
			mapCallbacks.remove(nombreRival); //Pase lo que pase, se elimina la partida, ya que o se acepta o el jugador no esta activo
		}
		System.out.println("Una partida entre "+nombreJugador+" y "+nombreRival+" se ha aceptado con exito.");
		return true;
	}

	
}
