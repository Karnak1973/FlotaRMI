package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.IntCallbackCliente;

public class ImplCallbackCliente extends UnicastRemoteObject implements IntCallbackCliente{
	
	public ImplCallbackCliente() throws RemoteException{
		super();
	}
	
	public void notificame(String nombre) throws RemoteException{
		System.out.println(nombre+" ha aceptado tu partida");
	}
}
