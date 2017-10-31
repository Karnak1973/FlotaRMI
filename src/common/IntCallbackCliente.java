package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IntCallbackCliente extends Remote{
	
	public void notificame(String nombre) throws RemoteException;

}
