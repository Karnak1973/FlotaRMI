# FlotaRMI
Project from EI1021-Sistemas Distribuidos. Battleship using RMI and Client-Side Callbacks

El proyecto se organiza en los siguientes paquetes:

- Common: contiene las interfaces que deberán los objetos remotos, Juego y Partida por parte del servidor y Callback por parte del cliente.
- Server: contiene las implementaciones de los objetos remotos, y el servidor, que registrara el objeto remoto de Juego (el de Partida se podrá obtener a partir de este). Además esta java.policy donde se conceden ciertos permisos de conexión, necesario al emplear un gestor de seguridad.
- Cliente: contiene la implementacion del objeto remoto callback, y el cliente que obtendrá una referencia al objeto remoto del servidor y crea el objeto remoto de callback, que se registrará a su debido tiempo.

Para ver el funcionamiento, arrancar el servidor y varios clientes (hay que indicar un nombre para cada cliente). Se podrá jugar las partidas sin problema. Para comprobar el uso de los callbacks habrá que seleccionar del menú 'multijugador' alguna opción:

- Proponer partida: registramos en el servidor el nombre junto con una referencia al objeto de callback.
- Borrar partida: borra el registro de callback del servidor, en caso de que exista.
- Listar partida: lista los jugadores que han propuesto partidas.
- Aceptar partida: acepta la partida de un jugador. Se empleará el callback para indicar al jugador que propuso la partida que otro jugador la ha aceptado.

Destacar que el modo 'Multijugador' es ficticio, tan solo es para el uso de los callback, no se implementa el que dos jugadores jueguen la partida por turnos.
