package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import common.IntCallbackCliente;
import common.IntServidorJuegoRMI;
import common.IntServidorPartidasRMI;

public class ImplServidorJuegoRMI extends UnicastRemoteObject implements IntServidorJuegoRMI{

	private static HashMap<String,IntCallbackCliente> mapCallbacks;
	
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
		if (mapCallbacks.containsKey(nombreJugador)) //Si ya tenia propuesta partida devuelve false
			return false;
		mapCallbacks.put(nombreJugador, callbackClientObject);
		return true;
	}

	
	public synchronized boolean borraPartida(String nombreJugador) throws RemoteException {
		// TODO Auto-generated method stub
		if (mapCallbacks.containsKey(nombreJugador)){ //Devuelve true si el jugador tenia una partida que borrar
			mapCallbacks.remove(nombreJugador);
			return true;
		}
		return false;
	}

	
	public String[] listaPartidas() throws RemoteException {
		// TODO Auto-generated method stub
		String[] res = new String[mapCallbacks.size()];
		int i=0;
		for (String partida : mapCallbacks.keySet()){
			res[i++]=partida;
		}
		return res;
	}

	
	public boolean aceptaPartida(String nombreJugador, String nombreRival) throws RemoteException {
		// TODO Auto-generated method stub
		if (!mapCallbacks.containsKey(nombreRival)) //Si el rival no ha propuesto partida devuelve false
			return false;
		try{
			mapCallbacks.get(nombreRival).notificame(nombreJugador); //Intenta hacer callback, si no puede devuelve false
		}catch(Exception e){
			return false;
		}finally {
			mapCallbacks.remove(nombreRival); //Pase lo que pase, se elimina la partida, ya que o se acepta o el jugador no esta activo
		}
		return true;
	}

	
}
