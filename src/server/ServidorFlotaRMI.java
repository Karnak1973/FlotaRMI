package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorFlotaRMI {
	
	public static void main (String args[]){
		try{
			
			if (System.getSecurityManager()==null){
				System.setSecurityManager(new SecurityManager());
			}
			
			startRegistry(1099);
			ImplServidorJuegoRMI exportedObj = new ImplServidorJuegoRMI();
			String registryURL = "rmi://localhost:1099/flota";
			Naming.rebind(registryURL, exportedObj);
			System.out.println("FlotaRMI Server ready.");
		}catch(Exception e){
			System.out.println("Exception in ServidorFlotaRMI: " + e);
		}
	}
	
	private static void startRegistry(int RMIPortNum)
		      throws RemoteException{
		try {
			Registry registry = LocateRegistry.getRegistry(RMIPortNum);
		    registry.list();  
		} catch (RemoteException e) {
		    System.out.println("RMI registry cannot be located at port " + RMIPortNum);
		    LocateRegistry.createRegistry(RMIPortNum);
		    System.out.println("RMI registry created at port " + RMIPortNum);
		}
	}
}
