#Introducción

El sistema implementado corresponde a un buscador que permite obtener información básica de títulos pertenecientes a diferentes generos (Series, Peliculas, Animes, Videojuegos,etc). Para esto, se hace uso de la base de datos disponible en Imdb (https://www.imdb.com/interfaces/), la cual contiene una gran cantidad de títulos.

Este buscadoe se encuentra desplegado sobre un cluster de Google Cloud, usando kubernetes como herramienta principal para orquestar contenedores (Google Cloud kubernetes Engine).

# Diseño Arquitectural

En el siguiente diagrama se puede apreciar el diseño arquitectural del despliegue de la solución.

![Diagrama arquitectural](index/arquitectura.png)

Tal como se puede ver, este consiste en un cluster, en el que se puede encontrar un numero definido de PODS. Cada uno de estos encapsula una instancia de la App encargada de procesar las consultas y un Cache ( Redis) que permite acelerar el proceso.

Por ultimo, el principal componenete usado para soportar las consultas corresponde a un indice invertido, el cual puede ser accedido por cualquiera de las replicas existentes en cada Pod. Para dar persistencia a dicho indice, se hace uso de los volumenes persistentes de kubernete.

# Kubernetes

Tal como se menciona anteriormente, el despliegue se hace sobre kubernetes. El uso de esta herramienta representa dos importantes ventajas:

 - Control de Trafico: Permite a los desarrolladores abstraerse a la hora de controlar las diferentes peticiones realizadas a la applicación. Esto a causa de que kubernetes implementa su propio balanceador de carga, para cada servicio, así como tambien su propio proxy encargado de determinar a que nodo del cluster enviar la consulta.
 - Disponibilidad: kubernetes se encarga de la gestion de los contenedores que forman parte del cluster. Específicamente, permite que se mantenga el estado "ideal" o "deseado" definido en la configuración, por lo que si se producen problemas (como puede ser la caida de un nodo), kubernet se encargara de reubicarlos en los nodos existentes. De esta forma, se mantiene una mejor disponibilidad.

 # Uso del buscador

 Para hacer uso del buscador se cuenta con un cliente en la siguiente dirección. https://moviesappsd.herokuapp.com. En este solo se debe ingresar el nombre de algún título junto con el numero maximo de resultados que se desea obtener. Como resultado se obtendrá una lista de títulos de diferentes generos.
