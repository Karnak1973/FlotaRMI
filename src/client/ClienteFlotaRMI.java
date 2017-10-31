package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import common.IntServidorJuegoRMI;
import common.IntServidorPartidasRMI;

public class ClienteFlotaRMI {
	
	/** Parametros por defecto de una partida */
	public static final int AGUA = -1, TOCADO = -2, HUNDIDO = -3;
	public static final int NUMFILAS=8, NUMCOLUMNAS=8, NUMBARCOS=6;
	public static final String SALIR="Salir", NUEVAPARTIDA="Nueva Partida", SOLUCION="Solucion";
	public static final String PROPON="Proponer partida", BORRA="Borrar partida", LISTA="Listar partidas", ACEPTA="Aceptar partida";

	private GuiTablero guiTablero;			
	private IntServidorPartidasRMI partida = null; 
	private IntServidorJuegoRMI serverJuego; // Objeto con los datos de la partida en juego
	private ImplCallbackCliente cb;
	
	/** Atributos de la partida guardados en el juego para simplificar su implementaciÃ³n */
	private int quedan = NUMBARCOS, disparos = 0;
	private boolean enJuego;
	
	private String nombre;  //Nombre que tendra el jugador
	private Scanner sc;
	
	public static void main (String args[]){
		ClienteFlotaRMI cliente = new ClienteFlotaRMI();
		cliente.ejecuta();
	}
	
	private void ejecuta(){
		try{	
			if (System.getSecurityManager()==null){		//Iniciamos el SecurityManager de Java
				System.setSecurityManager(new SecurityManager());
			}
			
			String registryURL = "rmi://localhost:1099/flota";
		
			serverJuego = (IntServidorJuegoRMI)Naming.lookup(registryURL);
			partida = serverJuego.nuevoServidorPartidas();
			partida.nuevaPartida(NUMFILAS, NUMCOLUMNAS, NUMBARCOS);
			cb = new ImplCallbackCliente();
			
			sc = new Scanner(System.in);
			System.out.print("Introduce tu nombre: ");
			nombre = sc.nextLine();
			
			enJuego=true;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					guiTablero = new GuiTablero(NUMFILAS, NUMCOLUMNAS);
					guiTablero.dibujaTablero();
				}
			});
		}catch(Exception e){
			System.out.println("Exception in ClienteFlotaRMI: " + e);
		}
	}
	
	/******************************************************************************************/
	/*********************  CLASE INTERNA GuiTablero   ****************************************/
	/******************************************************************************************/
	private class GuiTablero {

		private int numFilas, numColumnas;

		private JFrame frame = null;        // Tablero de juego
		private JLabel estado = null;       // Texto en el panel de estado
		private JButton buttons[][] = null; // Botones asociados a las casillas de la partida

		/**
         * Constructor de una tablero dadas sus dimensiones
         */
		GuiTablero(int numFilas, int numColumnas) {
			this.numFilas = numFilas;
			this.numColumnas = numColumnas;
			frame = new JFrame(nombre);
			//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.addWindowListener(new EscuchadorVentana());
		}

		/**
		 * Dibuja el tablero de juego y crea la partida inicial
		 */
		public void dibujaTablero() {
			anyadeMenu();
			anyadeGrid(numFilas, numColumnas);		
			anyadePanelEstado("Intentos: " + disparos + "    Barcos restantes: " + quedan);		
			frame.setSize(300, 300);
			frame.setVisible(true);	
		} // end dibujaTablero

		/**
		 * Anyade el menu de opciones del juego y le asocia un escuchador
		 */
		private void anyadeMenu() {
			Container fram= frame.getContentPane();
			
			JMenuBar menuBar= new JMenuBar();
			JMenu menu= new JMenu("Opciones");
			menuBar.add(menu);
			
			MenuListener listenerMenu= new MenuListener();
			
			JMenuItem salir= new JMenuItem(SALIR); //Se crea el boton Salir
			salir.setActionCommand(SALIR);
			salir.addActionListener(listenerMenu);
			
			JMenuItem nuevaPartida= new JMenuItem(NUEVAPARTIDA); //Se crea el boton NuevaPartida
			nuevaPartida.setActionCommand(NUEVAPARTIDA);
			nuevaPartida.addActionListener(listenerMenu);
			
			JMenuItem solucion= new JMenuItem(SOLUCION); //Se crea el boton Solucion
			solucion.setActionCommand(SOLUCION);
			solucion.addActionListener(listenerMenu);
			
			menu.add(salir);
			menu.add(nuevaPartida);
			menu.add(solucion);
			
			menu = new JMenu("Multijugador");
			menuBar.add(menu);
			
			MultijugadorListener listenerMulti = new MultijugadorListener();
			
			JMenuItem propon = new JMenuItem(PROPON); //Se crea el boton Proponer partida
			propon.setActionCommand(PROPON);
			propon.addActionListener(listenerMulti);
			
			JMenuItem borra = new JMenuItem(BORRA);	//Se crea el boton borrar partida
			borra.setActionCommand(BORRA);
			borra.addActionListener(listenerMulti);
			
			JMenuItem lista = new JMenuItem(LISTA); //Se crea el boton Listar partidas
			lista.setActionCommand(LISTA);
			lista.addActionListener(listenerMulti);
			
			JMenuItem acepta = new JMenuItem(ACEPTA);	//Se crea el boton Aceptar partida
			acepta.setActionCommand(ACEPTA);
			acepta.addActionListener(listenerMulti);
			
			menu.add(propon);
			menu.add(borra);
			menu.add(lista);
			menu.add(acepta);
			
			fram.add(menuBar,BorderLayout.NORTH);

			
		} // end anyadeMenu

		/**
		 * Anyade el panel con las casillas del mar y sus etiquetas.
		 * Cada casilla sera un boton con su correspondiente escuchador
		 * @param nf	numero de filas
		 * @param nc	numero de columnas
		 */
		private void anyadeGrid(int nf, int nc) {
			buttons = new JButton[nf][nc];
          	JPanel grid = new JPanel();
            grid.setLayout(new GridLayout(nf+1, nc+1));
            JButton boton;
            JLabel label;
            ButtonListener buttonList = new ButtonListener();
            
            grid.add(new JLabel());
            for (int col=1; col<=nc; col++) {
            	label= new JLabel(Integer.toString(col), JLabel.CENTER);
    			grid.add(label);
            }
            grid.add(new JLabel());
            
            for (int fila=0; fila<nf; fila++) {
            	char letra= (char) (fila+65);	
    			label= new JLabel(Character.toString(letra), JLabel.CENTER);
    			grid.add(label);
    			for (int col=0; col<nc; col++) {
    				boton = new JButton();
	    			boton.putClientProperty("Fila",fila);
	    			boton.putClientProperty("Columna",col);
	    			boton.addActionListener(buttonList);
	    			grid.add(boton); 
	    			buttons[fila][col] = boton;
    			}
    			label= new JLabel(Character.toString(letra), JLabel.CENTER);
    			grid.add(label);
            }
            
			frame.getContentPane().add(grid, BorderLayout.CENTER);
		} // end anyadeGrid

		/**
		 * Anyade el panel de estado al tablero
		 * @param cadena	cadena inicial del panel de estado
		 */
		private void anyadePanelEstado(String cadena) {	
			JPanel panelEstado = new JPanel();
			estado = new JLabel(cadena);
			panelEstado.add(estado);
			// El panel de estado queda en la posiciÃ³n SOUTH del frame
			frame.getContentPane().add(panelEstado, BorderLayout.SOUTH);
		} // end anyadePanel Estado

		/**
		 * Cambia la cadena mostrada en el panel de estado
		 * @param cadenaEstado	nuevo estado
		 */
		public void cambiaEstado(String cadenaEstado) {
			estado.setText(cadenaEstado);
		} // end cambiaEstado

		/**
		 * Muestra la solucion de la partida y marca la partida como finalizada
		 */
		public void muestraSolucion() {
			enJuego=false;
			//Primero se pintan todos los botones de azul
			for (int fila=0; fila<NUMFILAS; fila++) {
				for (int col=0; col<NUMCOLUMNAS; col++) {
					pintaBoton(buttons[fila][col], Color.CYAN);
				}
			}
			//Ahora se pintan solo los botones que sean barcos de rojo
			try {
				String solucion[]= partida.getSolucion();
				for(String idBarco : solucion) {
					pintaBarcoHundido(idBarco);
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			}
			
		} // end muestraSolucion


		/**
		 * Pinta un barco como hundido en el tablero
		 * @param cadenaBarco	cadena con los datos del barco codifificados como
		 *                      "filaInicial#columnaInicial#orientacion#tamanyo"
		 */
		public void pintaBarcoHundido(String cadenaBarco) {
			String[] datos = cadenaBarco.split("#");
            int filaIni = Integer.parseInt(datos[0]);
            int colIni = Integer.parseInt(datos[1]);
            char orientacion =  datos[2].charAt(0);
            int tamanyo = Integer.parseInt(datos[3]);
            
            JButton boton;
            if(orientacion=='H') {
            	for (int i=0; i<tamanyo; i++) {
            		boton= buttons[filaIni][colIni+i];
            		pintaBoton(boton, Color.RED);
            	}		
            }else {
            	for (int i=0; i<tamanyo; i++) {
            		boton= buttons[filaIni+i][colIni];
            		pintaBoton(boton, Color.RED);
            	}
            }
              		
            
		} // end pintaBarcoHundido

		/**
		 * Pinta un botÃ³n de un color dado
		 * @param b			boton a pintar
		 * @param color		color a usar
		 */
		public void pintaBoton(JButton b, Color color) {
			b.setBackground(color);
			// El siguiente cÃ³digo solo es necesario en Mac OS X
			b.setOpaque(true);
			b.setBorderPainted(false);
		} // end pintaBoton

		/**
		 * Limpia las casillas del tablero pintÃ¡ndolas del gris por defecto
		 */
		public void limpiaTablero() {
			for (int i = 0; i < numFilas; i++) {
				for (int j = 0; j < numColumnas; j++) {
					buttons[i][j].setBackground(null);
					buttons[i][j].setOpaque(true);
					buttons[i][j].setBorderPainted(true);
				}
			}
			enJuego=true;
		} // end limpiaTablero

		/**
		 * 	Destruye y libera la memoria de todos los componentes del frame
		 */
		public void liberaRecursos() {
			frame.dispose();
		} // end liberaRecursos


	} // end class GuiTablero

	/******************************************************************************************/
	/*********************  CLASE INTERNA MenuListener ****************************************/
	/******************************************************************************************/

	/**
	 * Clase interna que escucha el menu de Opciones del tablero
	 * 
	 */
	private class MenuListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
            	case SALIR:
            		guiTablero.liberaRecursos();
            		try {
            			serverJuego.borraPartida(nombre);
            		}catch(RemoteException ex) {
            			ex.printStackTrace();
            		}
            		System.exit(0);
            		break;
            	case NUEVAPARTIDA:
            		guiTablero.limpiaTablero();
            		quedan=NUMBARCOS;
            		disparos=0;
            		guiTablero.cambiaEstado("Intentos: " + disparos + "    Barcos restantes: " + quedan);
            		try {
            			partida.nuevaPartida(NUMFILAS, NUMCOLUMNAS, NUMBARCOS);
            		} catch (IOException ex) {
            			ex.printStackTrace();
            		}
            		break;
            	case SOLUCION:
            		guiTablero.muestraSolucion();
            		break;
            }
		} // end actionPerformed

	} // end class MenuListener



	/******************************************************************************************/
	/*********************  CLASE INTERNA ButtonListener **************************************/
	/******************************************************************************************/
	/**
	 * Clase interna que escucha cada uno de los botones del tablero
	 * Para poder identificar el boton que ha generado el evento se pueden usar las propiedades
	 * de los componentes, apoyandose en los metodos putClientProperty y getClientProperty
	 */
	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (quedan!=0 && enJuego) {
				JButton boton = (JButton)e.getSource();
				int fila = (int) boton.getClientProperty("Fila");
				int columna = (int) boton.getClientProperty("Columna");
				try {
					int res = partida.pruebaCasilla(fila, columna);
				
					switch (res){
					case AGUA: //Se ha tocado a agua
						guiTablero.pintaBoton(boton , Color.CYAN);
						break;
					case TOCADO: //Se ha tocado un barco
						guiTablero.pintaBoton(boton , Color.ORANGE);
						break;
					default: 
						if(res>AGUA) { //Se ha tocado un barco y pasa a hundido
							quedan--;
							guiTablero.pintaBarcoHundido(partida.getBarco(res));
							if(quedan==0) { //Se llama cuando acaba la partida
								guiTablero.muestraSolucion();
							}
						}
						break;
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				guiTablero.cambiaEstado("Intentos: " + ++disparos + "    Barcos restantes: " + quedan);
			}
        } // end actionPerformed

	} // end class ButtonListener
	
	/******************************************************************************************/
	/*********************  CLASE INTERNA WindowListener **************************************/
	/******************************************************************************************/
	/**
	 * Listener para escuchar la ventana de juego, si se cierra la ventana se cierra
	 * el socket y tras ello acaba el programa
	 */
	private class EscuchadorVentana extends WindowAdapter {
		
		@Override
		public void windowClosing(WindowEvent e) {
			try {
				serverJuego.borraPartida(nombre);
			}catch(RemoteException ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		}
		
	} //end class EscuchadorVentana
	
	//Clase interna para escuchar el menu Multijugador

	private class MultijugadorListener implements ActionListener{
		
		public void actionPerformed(ActionEvent e){
			try{
				switch(e.getActionCommand()){
				case PROPON:
					if (serverJuego.proponPartida(nombre, cb))
						System.out.println("Has propuesto una partida.");
					else
						System.out.println("Ya tienes propuesta una partida.");
					break;
				case BORRA:
					if (serverJuego.borraPartida(nombre))
						System.out.println("Se ha borrado tu partida propuesta.");
					else
						System.out.println("No tenias ninguna partida propuesta.");					
					break;
				case LISTA:
					String[] lista = serverJuego.listaPartidas();
					if (lista.length == 0) {
						System.out.println("No hay partidas propuestas.");
					}else {
						System.out.print("Hay las siguientes partidas propuestas: ");
						for (String nombre : lista){
							System.out.print(nombre+" ");
						}
						System.out.println("");
					}
					break;
				case ACEPTA:
					System.out.print("Introduce el nombre de partida que quieres aceptar: ");
					String otro = sc.nextLine();
					if (otro.equals(nombre)) { //Se hace aqui la comprobacion para poder mostrar distintos mensajes.
						System.out.println("No puedes aceptar tu propia partida.");
					}else if (serverJuego.aceptaPartida(nombre, otro)) {
						System.out.println("Has aceptado la partida de "+otro+".");
					}else {
						System.out.println("No se puede aceptar la partida de "+otro+". No hay partida o no esta en linea.");
					}
					break;
				}
			}catch(Exception ex){
				System.out.println("Exception MultijugadorListener.");
			}
		}
	}

}
