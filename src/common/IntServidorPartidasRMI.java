package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

import server.Partida;

public interface IntServidorPartidasRMI extends Remote{

	public void nuevaPartida(int nf, int nc, int nBarcos)
		throws RemoteException;
	
	public int pruebaCasilla(int nf, int nc)
		throws RemoteException;
	
	public String getBarco(int idBarco)
		throws RemoteException;
	
	public String[] getSolucion()
		throws RemoteException;
	
}
