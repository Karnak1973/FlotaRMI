package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.IntServidorPartidasRMI;

public class ImplServidorPartidasRMI extends UnicastRemoteObject implements IntServidorPartidasRMI{

	private Partida partida;
	
	public ImplServidorPartidasRMI() throws RemoteException{
		super();
	}
	
	public void nuevaPartida(int nf, int nc, int nBarcos) throws RemoteException {
		// TODO Auto-generated method stub
		partida = new Partida(nf, nc, nBarcos);
	}

	
	public int pruebaCasilla(int nf, int nc) throws RemoteException {
		// TODO Auto-generated method stub
		return partida.pruebaCasilla(nf, nc);
	}

	
	public String getBarco(int idBarco) throws RemoteException {
		// TODO Auto-generated method stub
		return partida.getBarco(idBarco);
	}

	
	public String[] getSolucion() throws RemoteException {
		// TODO Auto-generated method stub
		return partida.getSolucion();
	}

}
