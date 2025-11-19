import java.util.LinkedList;
import java.util.Stack;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.AbstractMap;
import java.util.*;
import java.io.*;
import java.util.Map;

/**
 Universidad Diego Portales - EDA 2025
 LABORATORIO 4: Montones, HashT y ABB en una Simulacion Hospitalaria
 UNIVERSIDAD DIEGO PORTALES
 PROFESOR: Cristian Llull
 INTEGRANTES:
 -Cinthya Fuentealba Bravo
 -Ignacia Reyes Ojeda
 */
//===============================================================================================
//CLASE PACIENTE
class Paciente implements Comparable<Paciente>{
    //Atributos clase paciente
    private String id;//Rut del paciente
    private String nombre;//Nombre del paciente
    private int categoria;//Categorizacion del paciente del 1 al 5(mas a menos prioridad)
    private long tiempoLlegada;//Tiempo de ingreso del paciente
    Stack<String>historialCambios;//Pila con registro de los cambios de estado del paciente

    //Metodos clase paciente

    //Constructor
    public Paciente(String id, String nombre, int categoria, long tiempoLlegada, Stack<String> historialCambios) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.tiempoLlegada = tiempoLlegada;
        this.historialCambios = new Stack<>();

        //Si se recibe una pila,se usa, si no, creamos una nueva y vacía
        if (historialCambios != null) {
            this.historialCambios = historialCambios;
        } else {
            this.historialCambios = new Stack<>();
        }
    }

    //Setters y Getters
    public String getId() {return id;}
    public String getNombre() {return nombre;}
    public int getCategoria() {return categoria;}
    public long getTiempoLlegada() {return tiempoLlegada;}
    public Stack<String> getHistorialCambios() {return historialCambios;}

    public void setId(String id) {this.id = id;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public void setCategoria(int categoria) {this.categoria = categoria;}
    public void setTiempoLlegada(long tiempoLlegada) {this.tiempoLlegada = tiempoLlegada;}
    public void setHistorialCambios(Stack<String> historialCambios) {this.historialCambios = historialCambios;}

    //Comparador por categoria
    /*Compara primero por categoria (1 mas prioridad que 5)*/
    public int compareTo(Paciente p) {
        //Si tienen categoria diferente, se prioriza al que tenga mas prioridad
        if(this.categoria != p.categoria){
            return Integer.compare(this.categoria, p.categoria);
        }
        //si tienen categoria igual se debe priorizar por el tiempo de llegada
        else{
            return Long.compare(this.tiempoLlegada, p.tiempoLlegada);
        }
    }

    //Registra un cambio en el historial del paciente
    public void registrarCambio(String descripcion){
        historialCambios.add(descripcion);
    }
}
//===============================================================================================
//CLASE COLA PRIORIDAD PACIENTES
class ColaPrioridadPacientes{
    //Atributos clase cola prioridad pacientes
    private Paciente[] pacientes;//Arreglo que almacena pacientes
    private int tamano;//Cantidad actual de pacientes
    private int capacidad;//Tamaño maximo que puede tener el arreglo

    //Constructor
    /*Constructor que comienza con capcidad 100 y tamaño cero(indicacion guia)*/
    public ColaPrioridadPacientes() {
        this.tamano = 0;//Al inicio sin pacientes
        this.capacidad = 100;//Capcidad inicial de 100 segun guia
        this.pacientes = new Paciente[capacidad];//Creacion del arreglo
    }

    //Metodo que inserta un paciente en la cola de prioridad
    public void insertar(Paciente p){
        //Si el arreglo esta lleno, se debe aumentar su tamaño
        if(tamano == capacidad){
            cambiarTamano();
        }

        //Se agrega al paciente al final del arreglo
        pacientes[tamano] = p;

        //Se sube al paciente hasta su posicion correcta, segun prioridad y tiempor de llegada
        subirNodo(tamano);

        //Se aumenta la cantidad de pacientes
        tamano++;
    }

    //Metodo que retorna el paciente de mayor prioridad, sin ser sacado
    public Paciente obtenerMin(){
        //Si no existen pacientes, no existe un minimo, por lo tanto retorna null
        if(tamano == 0){
            return null;
        }

        //La raiz esta siempre en la posicion 0
        return pacientes[0];
    }

    //Metodo que saca y retorna el paciente de mayor prioridad
    public Paciente extraerMin(){
        //Si esta vacio, no se puede sacar nada y retorna null
        if (estaVacia()){
            return null;
        }

        //Se guarda el minimo actual(raiz)
        Paciente min = pacientes[0];

        //Se mueve el ultimo paciente a la raiz(posicion 0)
        pacientes[0] = pacientes[tamano-1];

        //Se elimina la referencia de la ultima posicion
        pacientes[tamano-1] = null;

        //Se disminuye el tamaño ya que ahora hay un elemento menos
        tamano--;

        //Si no esta vacia, bajamos la raiz y se repara la propíedad del monton
        if(!estaVacia()){
            bajarNodo(0);
        }

        //Se retorna al paciente minimo
        return min;
    }

    //Otros metodos

    //Metodo que aumenta la capacidad del arreglo al doble cuando este lleno el original
    private void cambiarTamano(){
        int newCapacidad = capacidad * 2;

        //Se crea un arreglo con el doble del tamaño
        Paciente[] newPacientes = new Paciente[newCapacidad];

        //Se recorre el arreglo para copiar todos los pacientes al nuevo arreglo
        for(int i = 0; i < tamano; i++){
            newPacientes[i] = pacientes[i];
        }

        //Se reemplaza lo antiguo por lo nuevo
        pacientes = newPacientes;
        capacidad = newCapacidad;
    }

    //Metodo que retorna el indice del padre de la posicion i en el monton
    private int padre(int i){
        return (i-1)/2;
    }

    //Metodo que retorna el indice del hijo izquierdo de la posicion i
    private int hijoIzquierdo(int i){
        return 2*i+1;
    }

    //Metodo que retorna el indice del hijo derecho de la posicion i
    private int hijoDerecho(int i){
        return 2*i+2;
    }

    //Metodo que sube el nodo en el monton
    /*Mientras el paciente tenga mayor prioridad que el padre, se intercambian posiciones*/
    private void subirNodo(int i){
        //Mientras no se este en la raiz(indice 0)
        while(i>0){
            int papa =padre(i);

            //Comparamos: ¿paciente i tiene mayor prioridad que el padre?
            if(pacientes[i].compareTo(pacientes[papa]) < 0){
                //Se intercambian paciente actual con el padre
                swap(i, papa);
                //Se actualiza i para seguir subiendo (si es necesario)
                i = papa;
            }
            //Si no tiene mas prioridad que el padre, se termina el ciclo con break
            else{
                break;
            }
        }
    }

    //Metodo que baja el nodo en el monton
    /*Compara el nodo con los hijos y si los hijos tienen mayor prioridad, el padre baja*/
    private void bajarNodo(int i){
        while(true){
            int izq = hijoIzquierdo(i);
            int der = hijoDerecho(i);

            //Definimos el que el menor(es el que tiene mas prioridad) por ende es el mismo i
            int menor = i;

            //Si existe hijo izquierdo se compara con el menor
            if(izq < tamano && pacientes[izq].compareTo(pacientes[menor]) < 0){
                menor = izq;
            }

            //si existe hijo derecho se compara con el actual menor
            if(der < tamano && pacientes[der].compareTo(pacientes[menor]) < 0){
                menor = der;
            }

            //Si el menor ya no es i, uno de los hijos tiene mas prioridad y se debe intercambiar
            if(menor != i){
                swap (i, menor);
                //Se sigue bajando desde la nueva posicion
                i = menor;
            }
            //Si i es menor que sus hijos, el monton esta ordenado y se termina el ciclo con un break
            else{
                break;
            }
        }
    }

    //Metodo que intercambia dos pacientes en el arreglo
    private void swap(int i, int j){
        Paciente tmp = pacientes[i];
        pacientes[i] = pacientes[j];
        pacientes[j] = tmp;
    }

    //Revisa si esta vacio el arreglo(¿Existen pacientes?)
    public boolean estaVacia(){
        return this.tamano == 0;
    }
}
//===============================================================================================
//CLASE HASH PACIENTES ENCADENADO
class HashPacientesEncadenado implements Map<String, Paciente>{
    //Creacion del nodo que guardara clave->valor
    private static class Nodo{
        String clave;//Clave:id del paciente
        Paciente valor;//Valor:objeto Paciente

        Nodo(String clave, Paciente valor){
            this.clave = clave;
            this.valor = valor;
        }
    }
    //Atributos
    //Arreglo de listas enlazadas(encadenamiento)
    private LinkedList<Nodo>[] tabla;

    //Cantidad de elementos que se guardaron
    private int size;

    //Capacidad del arreglo
    private int capacidad;

    //Contructores
    //Constructor vacio
    public HashPacientesEncadenado(){}

    //Constructor que permite indicar la capacidad
    public HashPacientesEncadenado(int capacidad){
        this.capacidad = capacidad;
        this.size = 0;
        this.tabla = new LinkedList[capacidad];

        //Se inician las posiciones con una lista vacia
        for(int i = 0; i < capacidad; i++){
            tabla[i] = new LinkedList<>();
        }
    }

    //Convierte una clave en un indice del arreglo
    private int hash(String clave){
        int h=clave.hashCode();
        //Asegurar de que sea positivo y dentro del rango de la tabla(0, capacidad-1)
        return Math.abs(h%capacidad);
    }

    //Metodos Pedidos
    //Agregar paciente o actualizar en la tabla
    public Paciente put(String clave, Paciente valor){
        //Se calcula en que casilla va
        int indice = hash(clave);

        //Se obtienen la linta encadenada de esa casilla
        LinkedList<Nodo> lista = tabla[indice];

        //Se recorre la lista para ver si existe la clave, de ser asi se acrtualiza el valor
        for(Nodo nodo :lista){
            if(nodo.clave.equals(clave)){
                //Si se encuentra la clave, actualiza el valor y se devuelve el antiguo
                Paciente antiguo = nodo.valor;
                nodo.valor = valor;
                return antiguo;
            }
        }
        //Si la clave no existe se agrega un nuevo nodo a la lista
        lista.add(new Nodo(clave,valor));
        //aumentamos la cantidad de elementos
        size++;
        //No existia un valor anterior
        return null;
    }

    //Obtener el paciente dando su rut
    public Paciente get(Object clave) {
        //Si el rut no es string, no valido
        if (!(clave instanceof String)) {
            return null;
        }

        String key = (String) clave;
        int indice = hash(key);
        LinkedList<Nodo> lista = tabla[indice];

        //Se recorre la lista buscando el indice
        for (Nodo nodo : lista) {
            if (nodo.clave.equals(key)) {
                return nodo.valor;
            }
        }
        //No se encontro
        return null;
    }

    //Eliminar paciente con el rut y mostrar el valor eliminado
    public Paciente remove(Object clave) {
        //Se valida que la clave sea de tipo string
        if(!(clave instanceof String)){
            return null;
        }
        String key = (String) clave;
        int indice = hash(key);
        LinkedList<Nodo> lista = tabla[indice];

        //Se usa un iterador para eliminar mientras se recorre la lista
        Iterator<Nodo> iterator = lista.iterator();
        while(iterator.hasNext()){
            Nodo nodo = iterator.next();
            if(nodo.clave.equals(key)){
                //Se guarda el paciente antes de que se elimine
                Paciente eliminado = nodo.valor;
                //Se elimina el nodo de la lista
                iterator.remove();
                //Se actualiza el tamaño como se elimino se resta
                size--;
                return eliminado;
            }
        }
        //No estaba la clave en la tabla, no se encontro
        return null;
    }
    //Retorna la cantidad de elementos guardados en el hash.
    public int size(){
        return size;
    }
    //Retorna true si el hash no tiene ningun paciente guardado y false en caso contrario
    public boolean isEmpty(){
        return size==0;
    }

    //Otros metodos
    //Revisar si existe un rut en la tabla
    public boolean containsKey(Object clave){
        return get(clave)!=null;
    }

    //Revisa si existe un objeto paciente en la tabla(comparado por equals)
    public boolean containsValue(Object value){
        if(!(value instanceof Paciente)){
            return false;
        }

        Paciente p =(Paciente) value;
        //Se recorren todas las casillas de la tabla
        for(int i = 0; i < capacidad; i++){
            //Se recorre cada nodo de la lista en esa casilla
            for (Nodo nodo : tabla[i]){
                if(nodo.valor.equals(p)){
                    return true;
                }
            }
        }
        return false;
    }

    //Borrar el contenido de la tabla
    public void clear(){
        for(int i = 0; i < capacidad; i++){
            tabla[i].clear();
        }
        size = 0;
    }

    //
    public void putAll(Map m){
        //Recorremos todas las claves del mapa m
        for(Object obj : m.keySet()){
            Map.Entry entry = (Map.Entry) obj;
            //Clave un string (rut del paciente)
            String clave = (String) entry.getKey();
            //El valor es un Paciente
            Paciente valor = (Paciente) entry.getValue();
            //Usamos el propio put para guardarlo en la tabla
            put(clave,valor);
        }
    }

    //Retorna un Set con todas las claves (rut)
    public Set<String> keySet(){
        Set<String> claves = new HashSet<>();
        //Se recorre todas las caillas de la tabla
        for(int i = 0; i < capacidad; i++){
            for(Nodo nodo : tabla[i]){
                claves.add(nodo.clave);
            }
        }
        return claves;
    }

    //Retorna una coleccion con todos los pacientes almacenados en la tabla
    public Collection<Paciente> values(){
        List<Paciente> valores = new ArrayList<>();

        //Se recorre toda la tabla
        for(int i = 0; i < capacidad; i++){
            for(Nodo nodo : tabla[i]){
                valores.add(nodo.valor);
            }
        }
        return valores;
    }

    //Retorna un set con entradas clave->valor
    public Set<Entry<String, Paciente>> entrySet(){
        Set<Entry<String, Paciente>> entradas = new HashSet<>();

        //Se recorre la tabla y se crea una entrada por cada nodo
        for(int i = 0; i < capacidad; i++){
            for(Nodo nodo : tabla[i]){
                entradas.add(new AbstractMap.SimpleEntry<>(nodo.clave,nodo.valor));
            }
        }
        return entradas;
    }
}
//===============================================================================================
//CLASE HASH PACIENTES SONDEO LINEAL
class HashPacientesSondeoLineal implements Map<String, Paciente>{
    /*Estados posibles de cada un de las casillas:
    * 0 -> Vacia
    * 1 -> Ocupada
    * 2 -> Borrada*/

    private int[] estados;//Arreglo que guarda el estado de cada casilla (0 /1 /2)
    private String[] claves;//Arreglo con las claves (RUT del paciente)
    private Paciente[] valores;//Arreglo con los valores (objetos Paciente)
    private int capacidad;//Capacidad total de la tabla (tamaño de los arreglos)
    private int size;//Cantidad de elementos actualmente almacenados

    //Constructor con capacidad
    public HashPacientesSondeoLineal(int capacidad){
        this.capacidad = capacidad;
        this.size = 0;

        //Todos los estados comienzan en cero(vacio)
        this.estados = new int[capacidad];
        this.claves = new String[capacidad];
        this.valores = new Paciente[capacidad];
    }

    //Funcion que convierte clave en indice
    private int hash(String clave){
        //Usa hashCode y lo ajusta al rango (0, capacidad -1)
        int h = clave.hashCode();
        return Math.abs(h) % capacidad;
    }

    //Buscar el indice de una clave que ya existe en la tabla
    private int buscarIndiceExistente(String clave){
        int indice = hash(clave);//Se comienza desde el indice base
        int pasos = 0;//Contador para evitar ciclos infinitos

        //Mientras la casilla no este vacia y no se haya recorrido toda la tabla
        while(estados[indice] != 0 && pasos < capacidad){
            //Si la clave ya existe y esta ocupada y la clave coincide
            if(estados[indice] == 1 && claves[indice].equals(clave)){
                //Encontrada
                return indice;
            }
            //Si no se avanza a la siguiente casilla
            indice = (indice + 1) % capacidad;
            pasos++;
        }
        //Clave no encontrada
        return -1;
    }

    //Buscar indice para insertar (o actualizar si la clave ya existe)
    private int buscarIndiceParaInsertar(String clave) {
        int indice = hash(clave);
        int pasos = 0;
        int primeraBorrada = -1; //Para reutilizar una casilla si la encontramos

        while (pasos < capacidad) {
            //Casilla vacia: aqui se puede insertar
            if (estados[indice] == 0) {
                //Si antes vimos una borrada, usamos esa, si no, usamos esta vacia
                return (primeraBorrada != -1) ? primeraBorrada : indice;
            }

            //Casilla ocupada: ve si es la misma clave, si es asi se actualiza
            if (estados[indice] == 1 && claves[indice].equals(clave)) {
                return indice;
            }

            //Casilla borrada: guardamos la primera casilla disponible
            if (estados[indice] == 2 && primeraBorrada == -1) {
                primeraBorrada = indice;
            }

            indice = (indice + 1) % capacidad;
            pasos++;
        }

        //Si recorrimos completo: si habia una borrada, se usa; si no, la tabla esta llena
        if (primeraBorrada != -1) {
            return primeraBorrada;
        }
        return -1;
    }

    //Otros metodos
    //Retorna el tamaño
    public int size(){return size;}

    //Verifica si esta vacia o no
    public boolean isEmpty(){return size==0;}

    //Revisa si existe una clave en el hash
    public boolean containsKey(Object key){
        if(!(key instanceof String)){return false;}
        String clave = (String) key;
        return buscarIndiceExistente(clave)!=-1;
    }
    //Revisa si existe un Paciente en la tabla(compara con equals)
    public boolean containsValue(Object value){
        if(!(value instanceof Paciente)){return false;}
        Paciente p = (Paciente) value;
        //Se recorre todas las casillas
        for(int i = 0; i < capacidad; i++){
            //Solo se ven las casillas ocupadas(estado 1)
            if(estados[i]==1 && valores[i] != null && valores[i].equals(p)){
                return true;
            }
        }
        return false;
    }

    //Se obtiene el Paciente asociado a una clave (RUT)
    public Paciente get(Object key){
        if(!(key instanceof String)){return null;}
        String clave = (String) key;
        int indice = buscarIndiceExistente(clave);
        if(indice==-1){return null;}
        return valores[indice];
    }

    //Inserta o actualiza un paciente asociado a una clave (RUT)
    public Paciente put(String key, Paciente value){
        //1)Si esta la clave se actualiza
        int indice = buscarIndiceExistente(key);
        if (indice != -1) {
            Paciente antiguo = valores[indice];
            valores[indice] = value;
            claves[indice] = key;
            //Estados(indice) ya es 1
            return antiguo;
        }

        //2)No existe: se busca un indice para insertar
        indice = buscarIndiceParaInsertar(key);
        if (indice == -1) {
            //Tabla llena
            throw new IllegalStateException("Tabla de Hash llena, no se puede insertar más");
        }

        claves[indice] = key;
        valores[indice] = value;

        //Si estaba vacía o borrada, ahora pasa a ocupada
        if (estados[indice] != 1) {
            estados[indice] = 1;
            size++;
        }

        //No habia valor antes
        return null;
    }
    //Elimina la clave(RUT) y devuelve el paciente eliminado
    public Paciente remove(Object key){
        if(!(key instanceof String)){return null;}
        String clave = (String) key;
        int indice = buscarIndiceExistente(clave);
        if(indice==-1){return null;}

        //Se guarda al paciente antes de eliminarlo
        Paciente eliminado=valores[indice];

        //Se limpia la casilla
        valores[indice] = null;
        claves[indice] = null;

        //Casilla marcada como eliminada(borrada)(estado 2)
        estados[indice] = 2;
        size--;
        return eliminado;
    }

    //Inserta todos los pares clave->valor de otro mapa en este hash
    public void putAll(Map<? extends String, ? extends Paciente> m) {
        for (Map.Entry<? extends String, ? extends Paciente> par : m.entrySet()) {
            put(par.getKey(), par.getValue());
        }
    }

    //Se limpia toda la tabla, se deja completamente en vacio
    public void clear(){
        this.estados = new int[capacidad];
        this.claves = new String[capacidad];
        this.valores = new Paciente[capacidad];
        this.size = 0;
    }

    //Retorna un conjunto con todas las claves guardadas
    public Set<String> keySet() {
        Set<String> conjunto = new HashSet<>();
        for (int i = 0; i < capacidad; i++) {
            if (estados[i] == 1 && claves[i] != null) {
                conjunto.add(claves[i]);
            }
        }
        return conjunto;
    }

    //Retorna una coleccion con todos los pacientes guardados
    public Collection<Paciente> values() {
        //Devolvemos una lista con todos los pacientes almacenados
        List<Paciente> lista = new ArrayList<>();

        for (int i = 0; i < capacidad; i++) {
            if (estados[i] == 1 && valores[i] != null) {
                lista.add(valores[i]);
            }
        }
        return lista;
    }

    //Retorna un set con las entradas clave->valor.
    public Set<Map.Entry<String, Paciente>> entrySet() {
        throw new UnsupportedOperationException("entrySet no implementado");
    }

}
//===============================================================================================
//CLASE ARBOL PACIENTES (BST)
class ArbolPacientes {
    // Creacion del nodo interno
    private static class NodoArbol {
        Paciente dato;//Paciente almacenado en este nodo
        NodoArbol izq;//Hijo izuqierdo
        NodoArbol der;//Hijo derecho

        NodoArbol(Paciente p) {
            this.dato = p;
        }
    }

    //Atributo
    //Raiz del arbol
    private NodoArbol raiz;

    //Constructor que crea un arbol vacio
    public ArbolPacientes() {
        this.raiz = null;
    }

    //Metodo para insertar un nuevo paciente
    public void insertar(Paciente p) {
        //si existe el rut se actusaliza la informacion del paciente(nodo)
        //Se llama a la version recursiva
        raiz = insertarRec(raiz, p);
    }

    //Insercion recursiva en el arbol
    private NodoArbol insertarRec(NodoArbol actual, Paciente p) {
        //Caso base: si el nodo actual es null, en esta parte va el nuevo paciente
        if (actual == null) {
            return new NodoArbol(p);
        }

        //Se compara por rut para mantener el orden del arbol
        String idNuevo = p.getId();
        String idActual = actual.dato.getId();

        //Si el rut del nuevo es menor que el actual, se debe ir al hijo izquierdo
        if (idNuevo.compareTo(idActual) < 0) {
            actual.izq = insertarRec(actual.izq, p);
        }

        //Si el rut es mayor que el actual, debo ir al hijo derecho
        else if (idNuevo.compareTo(idActual) > 0) {
            actual.der = insertarRec(actual.der, p);
        }

        //Si el nuevo rut es igual, el paciente ya existia y se actualiza la informacion
        else {
            actual.dato = p;
        }
        //Se retorna el nodo actual
        return actual;
    }

    //Metodo para buscar por rut
    public Paciente buscar(String id) {
        NodoArbol nodo = buscarRec(raiz, id);
        //Si se retorna null se devuelve null, en caso contrario se devuelve el dato del paciente
        if (nodo == null) {
            return null;
        }
        else{
            return nodo.dato;
        }
    }

    //Busqueda recursiva en el arbol
    private NodoArbol buscarRec(NodoArbol actual, String id) {
        //Caso base: el nodo esta vacio o no se encontro
        if (actual == null) {
            return null;
        }

        String idActual = actual.dato.getId();

        //Si se encuentra el rut, se retorna ese nodo
        if (id.equals(idActual)) {
            return actual;
        }

        //Si el nodo buscado es menor que el actual, se busca en el hijo izquierdo
        else if (id.compareTo(idActual) < 0) {
            return buscarRec(actual.izq, id);
        }

        //Si es mayor se busca en el hijo derecho
        else {
            return buscarRec(actual.der, id);
        }
    }

    //Recorrido In Order
    //Retorna lista de paciente ascendente por el recorrido In-Order
    public List<Paciente> obtenerPacientesEnOrden() {
        List<Paciente> lista = new ArrayList<>();
        inOrder(raiz, lista);
        return lista;
    }

    //Recorrido In-Order recursivo
    private void inOrder(NodoArbol actual, List<Paciente> lista) {
        //Caso base: Si el nodo actual es null, no hay nada
        if (actual == null) return;

        //Recorrido In Order
        //1)Se recorre el hijo izquierdo
        inOrder(actual.izq, lista);

        //2)Se recorre el nodo actual
        lista.add(actual.dato);

        //3)Se recorre el hijo derecho
        inOrder(actual.der, lista);
    }
}
//===============================================================================================
//CLASE HOSPITAL
class Hospital {
    //Sala de espera: HashTable.  Mapa que asocia el id (rut) del paciente con el objeto Paciente
    private Map<String, Paciente> salaEspera;

    //Historico: BST de pacientes dados de alta
    private ArbolPacientes historicoPacientes;

    //Cola de atencion: Min-Monton
    private ColaPrioridadPacientes colaAtencion;

    //Constructor
    public Hospital(Map<String, Paciente> implSalaEspera) {
        this.salaEspera = implSalaEspera;//HashEncadenado o SondeoLineal (mapa para la sala de espera)
        this.historicoPacientes = new ArbolPacientes();//BST(arbol para el historico)
        this.colaAtencion = new ColaPrioridadPacientes();//Monton (cola de prioridad basada en un monton binario)
    }

    //Registrar a un paciente nuevo en la urgencia
    public void registrarPaciente(Paciente p) {
        //Entra a la cola de prioridad, para que el paciente sea atendido
        colaAtencion.insertar(p);

        //Se agrega a la sala de espera (HashTable)
        salaEspera.put(p.getId(), p);
    }

    //Metodo para atender al siguiente paciente
    public Paciente atenderSiguiente() {
        //Se saca el de mayor prioridad del montón
        Paciente siguiente = colaAtencion.extraerMin();
        if (siguiente == null) {
            return null;
        }

        //Se elimina de la sala de espera
        salaEspera.remove(siguiente.getId());

        //Se mueve al historico (BST)
        historicoPacientes.insertar(siguiente);

        return siguiente;
    }

    //Buscar paciente que aun esta en la sala de espera(activo)
    public Paciente buscarPacienteActivo(String id) {
        return salaEspera.get(id);
    }

    //Buscar paciente en el historiasl
    public Paciente buscarPacienteHistorico(String id) {
        return historicoPacientes.buscar(id);
    }

    //Cantidad de pacientes actualmente en sala de espera(hash)
    public int pacientesEnEspera() {
        return salaEspera.size();
    }

}
//===============================================================================================
public class Main {
    public static void main(String[] args) {
        //Encadenamiento como tabla de pacientes
        Map<String, Paciente> tablaHash = new HashPacientesEncadenado(101);
        Hospital hospital = new Hospital(tablaHash);

        //Tiempo base, para tener tiempos distintos
        long ahora = System.currentTimeMillis();

        //Se crean algunos pacientes para prueba
        Paciente p1 = new Paciente("11.111.111-1", "Ana", 3, ahora + 1000, new Stack<>());
        Paciente p2 = new Paciente("22.222.222-2", "Benjamin", 1, ahora + 2000, new Stack<>());
        Paciente p3 = new Paciente("33.333.333-3", "Camila", 2, ahora + 3000, new Stack<>());

        //Registrar a los pacientes en el hospital
        hospital.registrarPaciente(p1);
        hospital.registrarPaciente(p2);
        hospital.registrarPaciente(p3);

        System.out.println("Se registraron 3 pacientes en la sala de espera.");

        //Buscar un paciente activo por RUT
        Paciente buscado = hospital.buscarPacienteActivo("22.222.222-2");
        if (buscado != null) {
            System.out.println("Paciente activo encontrado: " + buscado.getNombre());
        } else {
            System.out.println("Paciente no encontrado en sala de espera.");
        }

        //Se atienden a los pacientes según prioridad
        System.out.println("\nAtendiendo pacientes:");
        Paciente atendido;

        while ((atendido = hospital.atenderSiguiente()) != null) {
            System.out.println(" - Paciente atendido: " + atendido.getNombre() +
                    " (Categoria = " + atendido.getCategoria() + ")");
        }

        //Buscar en histórico, pacientes ya atendidos
        Paciente historico = hospital.buscarPacienteHistorico("22.222.222-2");
        if (historico != null) {
            System.out.println("\nPaciente encontrado en histórico: " + historico.getNombre());
        }

        else {
            System.out.println("\nPaciente no está en el histórico.");
        }
        experimento1();
        experimento2();
        experimento3();
        experimento4();
    }
    //===============================================================================================
    //===============================================================================================
    //===============================================================================================
    //Crear un paciente Ramdom
    public static Paciente generarPacienteRandom(int idNum, Random rand) {
        String id = "P" + idNum;// id simple
        String nombre = "Paciente" + idNum;// nombre cualquiera
        int categoria = 1 + rand.nextInt(5);// 1, 2, 3, 4, 5
        long tiempoLlegada = idNum;// puede ser el índice como “tiempo”

        return new Paciente(id, nombre, categoria, tiempoLlegada, new Stack<>());
    }
    //===============================================================================================
    //===============================================================================================
    //===============================================================================================
    //EXPERIMENTO 1
    public static void experimento1() {
        //N pedidas por la guia
        int[] ns = {10000, 50000, 100000, 250000, 500000, 750000, 1000000};
        Random rand = new Random();
        System.out.println();
        System.out.println("EXPERIMENTO 1");
        System.out.println("N => TiempoInsercion_ms => TiempoExtraccion_ms");

        for (int N : ns) {
            ColaPrioridadPacientes cola = new ColaPrioridadPacientes();

            //Medir al insertar
            long inicioInsert = System.nanoTime();
            for (int i = 0; i < N; i++) {
                Paciente p = generarPacienteRandom(i, rand);
                cola.insertar(p);
            }
            long finInsert = System.nanoTime();
            double tiempoInsertMs = (finInsert - inicioInsert) / 1000000.0;

            //Medir al extraer
            long inicioExtraer = System.nanoTime();
            while (!cola.estaVacia()) {
                cola.extraerMin();
            }
            long finExtraer = System.nanoTime();
            double tiempoExtraerMs = (finExtraer - inicioExtraer) / 1_000_000.0;

            //Imprimir
            System.out.println(N + " => Tiempo de insertar: " + tiempoInsertMs + "=> Tiempo de Extraer: " + tiempoExtraerMs);
        }
    }
    //===============================================================================================
    //===============================================================================================
    //===============================================================================================
    //EXPERIMENTO 2
    public static void experimento2() {
        int[] ns = {1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000};
        Random rand = new Random();
        System.out.println();
        System.out.println("EXPERIMENTO 2");
        System.out.println("N => TipoHash => TiempoInsercion_ms => TiempoBusqueda_ms");

        for (int N : ns) {
            //Generar a los pacientes
            List<Paciente> pacientes = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                pacientes.add(generarPacienteRandom(i, rand));
            }

            //1)HASH ENCADENADO
            Map<String, Paciente> hashEnc = new HashPacientesEncadenado(2 * N + 1);

            //Insertar
            long inicioInsEnc = System.nanoTime();
            for (Paciente p : pacientes) {
                hashEnc.put(p.getId(), p);
            }
            long finInsEnc = System.nanoTime();
            double tInsEncMs = (finInsEnc - inicioInsEnc) / 1000000.0;

            //Buscar
            long inicioBusEnc = System.nanoTime();
            for (Paciente p : pacientes) {
                hashEnc.get(p.getId());
            }
            long finBusEnc = System.nanoTime();
            double tBusEncMs = (finBusEnc - inicioBusEnc) / 1000000.0;

            System.out.println(N + " => Encadenado =>" + tInsEncMs + " --- Tiempo de Búsqueda =>" + tBusEncMs);

            //2) HASH SONDEO LINEAL
            Map<String, Paciente> hashLin = new HashPacientesSondeoLineal(2 * N + 1);

            //Insertar
            long inicioInsLin = System.nanoTime();
            for (Paciente p : pacientes) {
                hashLin.put(p.getId(), p);
            }
            long finInsLin = System.nanoTime();
            double tInsLinMs = (finInsLin - inicioInsLin) / 1000000.0;

            //Busqueda
            long inicioBusLin = System.nanoTime();
            for (Paciente p : pacientes) {
                hashLin.get(p.getId());
            }
            long finBusLin = System.nanoTime();
            double tBusLinMs = (finBusLin - inicioBusLin) / 1000000.0;

            System.out.println(N + " => SondeoLineal => " + tInsLinMs + " --- Tiempor de Búsqueda =>" + tBusLinMs);
            System.out.println();
        }
    }
    //===============================================================================================
    //===============================================================================================
    //===============================================================================================
    //EXPERIMENTO 3
    public static void experimento3() {
        int[] ns = {10000, 50000, 100000, 500000, 1000000};
        Random rand = new Random();
        System.out.println();
        System.out.println("EXPERIMENTO 3");
        System.out.println("N => TiempoInsercion_ms => TiempoBusqueda_ms");

        for (int N : ns) {

            //1)Generar pacientes random
            List<Paciente> pacientes = new ArrayList<>();
            for (int i = 0; i < N; i++) {
                pacientes.add(generarPacienteRandom(i, rand));
            }

            //2)Creacion del arbol
            ArbolPacientes arbol = new ArbolPacientes();

            //Medicion de insertar
            long inicioIns = System.nanoTime();
            for (Paciente p : pacientes) {
                arbol.insertar(p);
            }
            long finIns = System.nanoTime();
            double tInsMs = (finIns - inicioIns) / 1000000.0;

            //Medicion de busqueda de claves que ya existian
            long inicioBus = System.nanoTime();
            for (Paciente p : pacientes) {
                arbol.buscar(p.getId());
            }
            long finBus = System.nanoTime();
            double tBusMs = (finBus - inicioBus) / 1000000.0;

            System.out.println(N + " => " + tInsMs + " => " + tBusMs);
        }
    }
    //===============================================================================================
    //===============================================================================================
    //===============================================================================================
    //EXPERIMENTO 4
    public static void experimento4() {
        //Parametros para la simulacion
        int pasos = 100000;//Cantidad de instantes simulados
        double probLlegada = 0.6;//Probabilidad de que llegue un paciente en un paso
        double probAtencion = 0.4;//Probabilidad de atender a alguien en un paso

        Random rand = new Random();
        System.out.println();
        System.out.println("EXPERIMENTO 4");

        //Se usa hash encadenado como sala de espera
        Map<String, Paciente> tablaHash = new HashPacientesEncadenado(200003);
        Hospital hospital = new Hospital(tablaHash);

        int idCounter = 0;
        int totalRegistrados = 0;
        int totalAtendidos = 0;
        int maxEnEspera = 0;

        long inicioSim = System.nanoTime();

        for (int t = 0; t < pasos; t++) {

            //1)Llegada de paciente nuevo, con una cierta probabilidad
            if (rand.nextDouble() < probLlegada) {
                String id = "P" + idCounter;
                String nombre = "Paciente" + idCounter;
                int categoria = 1 + rand.nextInt(5); // 1..5
                long tiempoLlegada = t;

                Paciente p = new Paciente(id, nombre, categoria, tiempoLlegada, new Stack<>());
                hospital.registrarPaciente(p);
                totalRegistrados++;
                idCounter++;
            }

            //2)Atencion de un paciente, con cierta probabilidad
            if (rand.nextDouble() < probAtencion) {
                Paciente atendido = hospital.atenderSiguiente();
                if (atendido != null) {
                    totalAtendidos++;
                }
            }

            //3)Actualizar el numero maximo de pacientes en espera
            int enEspera = hospital.pacientesEnEspera();
            if (enEspera > maxEnEspera) {
                maxEnEspera = enEspera;
            }
        }

        long finSim = System.nanoTime();
        double tiempoTotalMs = (finSim - inicioSim) / 1000000.0;

        System.out.println();
        System.out.println("Simulación Hospital");
        System.out.println("Pasos simulados: " + pasos);
        System.out.println("Total pacientes llegados: " + totalRegistrados);
        System.out.println("Total pacientes atendidos: " + totalAtendidos);
        System.out.println("Pacientes aún en espera: " + hospital.pacientesEnEspera());
        System.out.println("Máximo de pacientes en espera: " + maxEnEspera);
        System.out.println("Tiempo total simulación (ms): " + tiempoTotalMs);
    }
}