/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import Excepciones.ErrorDatoInvalido;
import Persistencia.GestorPersistencia;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 *
 * @author admin
 */
public class Algoritmos extends AccesoBD{
    
    
    
    /**
     * Método calcular la similitud entre dos Películas utilizando el algoritmo del Coseno.
     * @param i1 Primera pelicula a comparar.
     * @param i2 Segunda película a comparar.
     * @param posIniTest Posicion (o identificador) en la base de datos donde comienza la partición test.
     * @param posFinTest Posicion (o identificador) en la base de datos donde finaliza la partición test.
     * @return Devuelve el valor de similitud.
     * @throws No se lanzan excepciones.
    */
    public double similitudCoseno(Pelicula i1, Pelicula i2){
        // Variables auxiliares:
        double norma1 = 0;
        double norma2 = 0;
        int val1;
        int val2;
        Long key;
        double numerador = 0;
        int comun = 0;
        // Constante de la MEJORA del Factor de relevancia
        int N = 20;
        
        // 1. Nos quedamos con la películas que tenga menos valoraciones.
        if (i1.getValoraciones().size() < i2.getValoraciones().size()){
            for (Entry<Long,Valoracion> e : i1.getValoraciones().entrySet()) {
                key = e.getKey();
                
                // 3. Comprobamos que la otra película haya sido valorada por el mismo usuario.
                if (i2.getValoraciones().containsKey(key)){
                    // 4. Realizamos los cálculos de similitud.
                    val1 = e.getValue().getValor();
                    val2 = i2.getValoraciones().get(key).getValor();

                    norma1 = norma1 + val1 * val1;
                    norma2 = norma2 + val2 * val2;

                    numerador = numerador + val1 * val2;
                    ++comun;
                }
                
            }
        }else{
            for (Entry<Long,Valoracion> e : i2.getValoraciones().entrySet()) {
                key = e.getKey();
                if (i1.getValoraciones().containsKey(key)){
                    val2 = e.getValue().getValor();
                    val1 = i1.getValoraciones().get(key).getValor();

                    norma1 = norma1 + val1 * val1;
                    norma2 = norma2 + val2 * val2;

                    numerador = numerador + val1 * val2;
                    ++comun;
                }
                
            }
        }
        
        if (norma1 != 0 && norma2 !=0){
            double sim = numerador / (Math.sqrt(norma1) * Math.sqrt(norma2));
            // Aplicamos la MEJORA del Factor de relevancia.
            if (comun < N){
                sim = sim * ((comun*1.0)/N);
            }
            if (sim > 1){
                return 1;
            }
            return sim;
        }else{
            return 0;
        }
        
    }
    
    
    public double similitudPearson(Pelicula i1, Pelicula i2, ArrayList<Long> test){
        // Variables auxiliares:
        double norma1 = 0;
        double norma2 = 0;
        int val1;
        int val2;
        Long key;
        double numerador = 0;
        double media1 = i1.getMedia();
        double media2 = i2.getMedia();
        int comun = 0;
        // Constante de la MEJORA del Factor de relevancia
        int N = 200;
        
        // 1. Nos quedamos con la películas que tenga menos valoraciones.
        if (i1.getValoraciones().size() < i2.getValoraciones().size()){
            for (Entry<Long,Valoracion> e : i1.getValoraciones().entrySet()) {
                key = e.getKey();
                // 2. Descartamos los usuarios de la partición test.
                if (!test.contains(key)){
                    // 3. Comprobamos que la otra película haya sido valorada por el mismo usuario.
                    if (i2.getValoraciones().containsKey(key)){
                        // 4. Realizamos los cálculos de similitud.
                        val1 = e.getValue().getValor();
                        val2 = i2.getValoraciones().get(key).getValor();

                        norma1 = norma1 + (val1 - media1)*(val1 - media1);
                        norma2 = norma2 + (val2 - media2)*(val2 - media2);

                        numerador = numerador + (val1 - media1)*(val2 - media2);
                        ++comun;
                    }
                }
            }
        }else{
            for (Entry<Long,Valoracion> e : i2.getValoraciones().entrySet()) {
                key = e.getKey();
                if (!test.contains(key)){
                    if (i1.getValoraciones().containsKey(key)){
                        val2 = e.getValue().getValor();
                        val1 = i1.getValoraciones().get(key).getValor();

                        norma1 = norma1 + (val1 - media1)*(val1 - media1);
                        norma2 = norma2 + (val2 - media2)*(val2 - media2);

                        numerador = numerador + (val1 - media1)*(val2 - media2);
                        ++comun;
                    }
                }
            }
        }
        
        if (norma1 != 0 && norma2 !=0){
            double sim = numerador / (Math.sqrt(norma1*norma2)) ;
            sim = (sim + 1)/2;
            // Aplicamos la MEJORA del Factor de relevancia.
            if (comun < N){
                sim = sim * ((comun*1.0)/N);
            }
            if (sim > 1){
                return 1;
            }
            return sim;
        }else{
            return 0;
        }
        
    }

    public Parametros testIAmasA(int n, HashMap<Long, TreeSet<ItemSim>> modeloSimilitud, List<Usuario> test, GestorPersistencia instancia) {
        // Variables auxiliares:
        Iterator<Usuario> it1 = test.iterator();
        Usuario u;
        double mediaP;
        long idP;
        double valoracionEstimada;
        int valoracionReal;
        double MAE = 0;
        int numEstimacionesRealizadas = 0;
        double dif;
        TreeSet<ItemSim> vecinos;
        int numEstimacionesImposibles = 0;
        int numEstimacionesPosibles = 0;
        
        
        // Nota: cargamos todas las medias de las peliculas a memoria para acelerar la ejecución
        HashMap<Long,Double> medias = getMediasPeliculasBD_HashMap(instancia);
        
        int cont = 0;
        // 1. Recorremos cada usuario de la partición test.
        while (it1.hasNext()){
            u = it1.next();
            //System.out.println(" Usuario "+cont+" de "+test.size());
            ++cont;
            
            // 2. Recorremos cada valoración del usuario en cuestión.
            for (Entry<Long,Valoracion> e : u.getValoraciones().entrySet()) {


                idP = e.getValue().getIdPelicula();

                // 3. Calculamos la valoracion real y la estimada.
                valoracionReal = e.getValue().getValor();
                mediaP = medias.get(idP);
                vecinos = modeloSimilitud.get(idP);
                valoracionEstimada = calcularPrediccionIAmasA(n, u, mediaP,vecinos);

                // 4. Comprobamos si hemos podido hacer la predicción
                if (valoracionEstimada != -1){
                   // 5. Acumulamos el MAE
                   dif = valoracionEstimada - valoracionReal*1.0;
                   if (dif > 0){
                      MAE = MAE + dif;
                   }else{
                      MAE = MAE + dif*(-1);
                   }
                }else{
                    ++numEstimacionesImposibles;
                }
            }
            
            numEstimacionesPosibles += u.getValoraciones().size();
        }
        
        numEstimacionesRealizadas = numEstimacionesPosibles - numEstimacionesImposibles;
        if (numEstimacionesRealizadas != 0){
            MAE = MAE/(numEstimacionesRealizadas*1.0);
        }else{
            MAE = 0;
        }
        
        return new Parametros(MAE,(numEstimacionesRealizadas*1.0)/numEstimacionesPosibles);
        
    }
    
    
    
    public Parametros testWA(HashMap<Long, TreeSet<ItemSim>> modeloSimilitud, List<Usuario> test, GestorPersistencia instancia) {
        // Variables auxiliares:
        Iterator<Usuario> it1 = test.iterator();
        Usuario u;
        long idP;
        double valoracionEstimada;
        int valoracionReal;
        double MAE = 0;
        int numEstimacionesRealizadas = 0;
        double dif;
        TreeSet<ItemSim> vecinos;
        int numEstimacionesImposibles = 0;
        int numEstimacionesPosibles = 0;
        
        // Nota: cargamos todas las medias de las peliculas a memoria para acelerar la ejecución
        HashMap<Long,Double> medias = getMediasPeliculasBD_HashMap(instancia);
        
        // 1. Recorremos cada usuario de la partición test.
        int cont = 0;
        while (it1.hasNext()){
            u = it1.next();
            //System.out.println(" Usuario "+cont+" de "+test.size());
            ++cont;
            // 2. Recorremos cada valoración del usuario en cuestión.
             for (Entry<Long,Valoracion> e : u.getValoraciones().entrySet()) {
                 idP = e.getValue().getIdPelicula();
                 
                 // 3. Calculamos la valoracion real y la estimada.
                 valoracionReal = e.getValue().getValor();
                 vecinos = modeloSimilitud.get(idP);
                 valoracionEstimada = calcularPrediccionWA(u,vecinos, medias);
                 
                 // 4. Comprobamos si hemos podido hacer la predicción
                 if (valoracionEstimada != -1){
                    // 5. Acumulamos el MAE
                    dif = valoracionEstimada - valoracionReal*1.0;
                    if (dif > 0){
                       MAE = MAE + dif;
                    }else{
                       MAE = MAE + dif*(-1);
                    }
                 }else{
                     ++numEstimacionesImposibles;
                 }
             }
             numEstimacionesPosibles += u.getValoraciones().size();
        }
        
        numEstimacionesRealizadas = numEstimacionesPosibles - numEstimacionesImposibles;
        if (numEstimacionesRealizadas != 0){
            MAE = MAE/(numEstimacionesRealizadas*1.0);
        }else{
            MAE = 0;
        }
        
        return new Parametros(MAE,(numEstimacionesRealizadas*1.0)/numEstimacionesPosibles);
        
    }

    
    
    /**
     * Método calcular el modelo de similitud, es decir, la clasificación de películas, utilizando el algoritmo del Coseno.
     * @param k Número de vecinos mas cercanos.
     * @param numPeliculas Número de películas dadas de alta en el sistema.
     * @param posIniTest Posicion (o identificador) en la base de datos donde comienza la partición test.
     * @param posFinTest Posicion (o identificador) en la base de datos donde finaliza la partición test.
     * @return Devuelve el modelo de similitud entre películas.
     * @throws No se lanzan excepciones.
    */
    
    public HashMap<Long, TreeSet<ItemSim>> getModeloSimilitud_byCoseno(int k, GestorPersistencia instancia) {
        // Estructura que representa el modelo de similitud (clave: id de pelicula; valor: lista de idPelicula-Similitud).
        HashMap<Long, TreeSet<ItemSim>> modelo_similitud = new HashMap();
        // Variables auxiliares:
        TreeSet<ItemSim> fila1;
        TreeSet<ItemSim> fila2;
        long id1;
        long id2;
        double similitud;
        long numPeliculas = getNumPeliculasBD(instancia);
        List<Pelicula> peliculas = getPeliculasBD(instancia);
        
        
        for (long i=0; i<numPeliculas; ++i){
            //System.out.println(" pelicula "+i+" de "+numPeliculas);
            //###// 1.1: Sacar la película numero i. Nota: estudiar si se pueden sacar todas de golpe.
            //Pelicula it1 = getPeliculaBD_byPos(instancia, i);
            Pelicula it1 = peliculas.get((int)i);
            id1 = it1.getIdPelicula();
            
            for (long j=i+1; j<numPeliculas; ++j){
                //###// 1.2: Sacar la película numero j.
                //Pelicula it2 = getPeliculaBD_byPos(instancia, j);
                Pelicula it2 = peliculas.get((int)j);
                id2 = it2.getIdPelicula();
                
                // 1.2: Calculo de la similitud entre it1 e it2.
                similitud = similitudCoseno(it1, it2);
                
                // 1.3: Guardar la similitud en una estructura.
                    //### 1.3: En el modelo definitivo, la similitud se guardará en la base de datos.
                    //###//Similitud s1 = new Similitud(it1.id,it2.id,similitud);
                //     NOTA: Hay que guardar, a la vez, tanto la similitud sim(id1,id2) como sim (id2,id1)
                if (modelo_similitud.containsKey(id1)){
                    fila1 =  modelo_similitud.get(id1);
                    fila1.add(new ItemSim(id2,similitud));
                    if (fila1.size() > k){
                        fila1.remove(fila1.last());
                    }
                    
                    if (modelo_similitud.containsKey(id2)){
                        fila2 =  modelo_similitud.get(id2);
                        fila2.add(new ItemSim(id1,similitud));
                        if (fila2.size() > k){
                            fila2.remove(fila2.last());
                        }
                    }else{
                        modelo_similitud.put(id2, new TreeSet<ItemSim>());
                        modelo_similitud.get(id2).add(new ItemSim(id1,similitud));
                    }
                }else{
                    modelo_similitud.put(id1, new TreeSet<ItemSim>());
                    modelo_similitud.get(id1).add(new ItemSim(id2,similitud));
                    
                    if (modelo_similitud.containsKey(id2)){
                        fila2 =  modelo_similitud.get(id2);
                        fila2.add(new ItemSim(id1,similitud));
                        if (fila2.size() > k){
                            fila2.remove(fila2.last());
                        }
                    }else{
                        modelo_similitud.put(id2, new TreeSet<ItemSim>());
                        modelo_similitud.get(id2).add(new ItemSim(id1,similitud));
                    }
                }
            }
        }
        
        return modelo_similitud;
    }


    
    
    public HashMap<Long, TreeSet<ItemSim>> getModeloSimilitud_byPearson(int k, ArrayList<Long> test, GestorPersistencia instancia) {
        // Estructura que representa el modelo de similitud (clave: id de pelicula; valor: lista de idPelicula-Similitud).
        HashMap<Long, TreeSet<ItemSim>> modelo_similitud = new HashMap();
        // Variables auxiliares:
        TreeSet<ItemSim> fila1;
        TreeSet<ItemSim> fila2;
        long id1;
        long id2;
        double similitud;
        long numPeliculas = getNumPeliculasBD(instancia);
        List<Pelicula> peliculas = getPeliculasBD(instancia);
        
        for (long i=0; i<numPeliculas; ++i){
            //System.out.println(" pelicula "+i+" de "+numPeliculas);
            //###// 1.1: Sacar la película numero i. Nota: estudiar si se pueden sacar todas de golpe.
            //Pelicula it1 = getPeliculaBD_byPos(instancia, i);
            Pelicula it1 = peliculas.get((int)i);
            id1 = it1.getIdPelicula();
            
            for (long j=i+1; j<numPeliculas; ++j){
                //###// 1.2: Sacar la película numero j.
                //Pelicula it2 = getPeliculaBD_byPos(instancia, j);
                Pelicula it2 = peliculas.get((int)j);
                id2 = it2.getIdPelicula();
                
                // 1.2: Calculo de la similitud entre it1 e it2.
                similitud = similitudPearson(it1, it2, test);
                
                // 1.3: Guardar la similitud en una estructura.
                    //### 1.3: En el modelo definitivo, la similitud se guardará en la base de datos.
                    //###//Similitud s1 = new Similitud(it1.id,it2.id,similitud);
                //     NOTA: Hay que guardar, a la vez, tanto la similitud sim(id1,id2) como sim (id2,id1)
                if (modelo_similitud.containsKey(id1)){
                    fila1 =  modelo_similitud.get(id1);
                    fila1.add(new ItemSim(id2,similitud));
                    if (fila1.size() > k){
                        fila1.remove(fila1.last());
                    }
                    
                    if (modelo_similitud.containsKey(id2)){
                        fila2 =  modelo_similitud.get(id2);
                        fila2.add(new ItemSim(id1,similitud));
                        if (fila2.size() > k){
                            fila2.remove(fila2.last());
                        }
                    }else{
                        modelo_similitud.put(id2, new TreeSet<ItemSim>());
                        modelo_similitud.get(id2).add(new ItemSim(id1,similitud));
                    }
                }else{
                    modelo_similitud.put(id1, new TreeSet<ItemSim>());
                    modelo_similitud.get(id1).add(new ItemSim(id2,similitud));
                    
                    if (modelo_similitud.containsKey(id2)){
                        fila2 =  modelo_similitud.get(id2);
                        fila2.add(new ItemSim(id1,similitud));
                        if (fila2.size() > k){
                            fila2.remove(fila2.last());
                        }
                    }else{
                        modelo_similitud.put(id2, new TreeSet<ItemSim>());
                        modelo_similitud.get(id2).add(new ItemSim(id1,similitud));
                    }
                }
            }
        }
        
        return modelo_similitud;
    }

    
    
    /**
     * Método para predecir la valoracion de un usuario sobre una película, teniendo en cuenta solo los vecinos más cercanos, utilizando el algoritmo de predicción IA+A.
     * @param u Usuario
     * @param idP identificador de la película a predecir.
     * @param vecinos Conjunto de vecinos más cercanos a la película a precedir.
     * @return Devuelve la valoracion estimada. Devuelve -1 si no se ha podido predecir
     * @throws No se lanzan excepciones.
    */
    protected double calcularPrediccionIAmasA(int n, Usuario u, double mediaP, TreeSet<ItemSim> vecinos) {
        // Estructura con solamente las valoraciones que un usuario ha realizado sobre los k vecinos mas cercanos a idP
        ArrayList<Valoracion> valoracionesCercanas = new ArrayList();
        
        // PASO 1: Quedarnos con las valoraciones a las películas más cercanas.
        // 1.1. Se recorren los vecinos mas cercanos a idP
        //mostrarVecinos(vecinos);
        for(ItemSim i : vecinos){
            // 1.2. Se comprueba si el usuario a valorado a dicho vecino
            if (u.getValoraciones().containsKey(i.getId())){
                // 1.3. Si es así se almacena en la estructura valoracionesCercanas.
                valoracionesCercanas.add(u.getValoraciones().get(i.getId()));
            }
        }
        
        if (!valoracionesCercanas.isEmpty()){
            // PASO 2: Conseguir las medias.
            // 2.1. Media de la pelicula idP
            

            // 2.2. Media del usuario en cuentión.
            double mediaU = u.getMedia();


            // PASO 3: Cálculo de la predicción.
            double numerador = 0;
            double denominador = 0;
            long idPAux;
            ItemSim itemSim;
            Valoracion v;
            
            
            // ENFOQUE DADOS-N. Seleccionamos n valoraciones cercanas.
            if ( n > 0 && n < valoracionesCercanas.size()){
                int rand;
                int cont = 0;
                ArrayList<Valoracion> array = new ArrayList();
                
                while (cont < n){
                    rand = (int) (Math.random() * valoracionesCercanas.size());
                    v = valoracionesCercanas.get(rand);
                    if (!array.contains(v)){
                        array.add(v);
                        ++cont;
                    }
                }
                
                valoracionesCercanas = array;
            }
            // FIN ENFOQUE DADOS-N
            
            
            Iterator<Valoracion> it1 = valoracionesCercanas.iterator();
            
            while(it1.hasNext()){
                v = it1.next();
                idPAux = v.getIdPelicula();

                itemSim = buscarVecino(idPAux, vecinos);

                numerador = numerador + itemSim.getSim()*(v.getValor()-mediaU) ;
                //denominador = denominador + itemSim.getSim();
                denominador = denominador + itemSim.getSim() ;

            }

            if (denominador != 0){
                double ajuste = numerador/denominador;
                
                return mediaP + ajuste;
            }else{
                return 0;
            }
        }else{
            return -1;
        }
        
    }

    
     /**
     * Método para predecir la valoracion de un usuario sobre una película, teniendo en cuenta solo los vecinos más cercanoS, utilizando el algoritmo de predicción IA+A.
     * @param u Usuario
     * @param idP identificador de la película a predecir.
     * @param vecinos Conjunto de vecinos más cercanos a la película a precedir.
     * @return Devuelve la valoracion estimada. Devuelve -1 si no se ha podido predecir
     * @throws No se lanzan excepciones.
    */
    private double calcularPrediccionWA(Usuario u, TreeSet<ItemSim> vecinos, HashMap<Long,Double> medias) {
        // Estructura con solamente las valoraciones que un usuario ha realizado sobre los k vecinos mas cercanos a idP
        ArrayList<Valoracion> valoracionesCercanas = new ArrayList();
        
        // PASO 1: Quedarnos con las valoraciones a las películas más cercanas.
        // 1.1. Se recorren los vecinos mas cercanos a idP
        //mostrarVecinos(vecinos);
        for(ItemSim i : vecinos){
            // 1.2. Se comprueba si el usuario a valorado a dicho vecino
            if (u.getValoraciones().containsKey(i.getId())){
                // 1.3. Si es así se almacena en la estructura valoracionesCercanas.
                valoracionesCercanas.add(u.getValoraciones().get(i.getId()));
            }
        }
        
        if (!valoracionesCercanas.isEmpty()){
            // PASO 2: Conseguir las medias.

            // 2.1. Media del usuario en cuentión.
            double mediaU = u.getMedia();
            // NOTA: Necesitamos la media de cada pelicula vecina. Se irá pidiendo con forme haga falta.

            // PASO 3: Cálculo de la predicción.
            double mediaK;
            double numerador = 0;
            double denominador = 0;
            long idPAux;
            ItemSim itemSim;
            Iterator<Valoracion> it1 = valoracionesCercanas.iterator();
            Valoracion v;

            while(it1.hasNext()){
                v = it1.next();
                idPAux = v.getIdPelicula();
                mediaK = medias.get(idPAux);
                itemSim = buscarVecino(idPAux, vecinos);

                numerador = numerador + itemSim.getSim()*(v.getValor()-mediaK) ;
                denominador = denominador + itemSim.getSim();

            }

            if (denominador != 0){
                return mediaU + numerador/denominador;
            }else{
                return 0;
            }
        }else{
            return -1;
        }
        
    }
    
    
    /**
     * Método para buscar la similitud de una película
     * @param idP identificador de la película a predecir.
     * @param vecinos Conjunto de vecinos más cercanos a la película a precedir.
     * @return Devuelve la pareja película-similitud. Si existe, devuelve una pareja con sus campos a cero.
     * @throws No se lanzan excepciones.
    */
    private ItemSim buscarVecino(long idP, TreeSet<ItemSim> vecinos) {
        Iterator<ItemSim> it = vecinos.iterator();
        ItemSim i;
        
        while (it.hasNext()){
            i = it.next();
            
            if (i.getId() == idP){
                return i;
            }
        }
        
        return null;
    }

    // Nota: no genera una nueva variable. No hay new.
    public Pelicula buscarPelicula(long idPelicula, List<Pelicula> lista) {
        Iterator<Pelicula> it1 = lista.iterator();
        Pelicula aux;
        
        while(it1.hasNext()){
            aux = it1.next();
            if(aux.getIdPelicula() == idPelicula){
                return aux;
            }
        }
        
        return null;
        
    }
    
    // Nota: no genera una nueva variable. No hay new.
    public Usuario buscarUsuario(long idUsuario, List<Usuario> lista) {
        Iterator<Usuario> it1 = lista.iterator();
        Usuario aux;
        
        while(it1.hasNext()){
            aux = it1.next();
            if(aux.getIdUsuario() == idUsuario){
                return aux;
            }
        }
        
        return null;
    }
    
    public void calcularMedias(ArrayList<Usuario> usuarios, ArrayList<Pelicula> peliculas) {
        
        // 1. Calculo de la media de valoraciones para cada usuario
        double numerador;
        Iterator<Usuario> it1 = usuarios.iterator();
        Usuario u;
        while(it1.hasNext()){
            numerador = 0;
            u = it1.next();
            for(Map.Entry<Long,Valoracion> v : u.getValoraciones().entrySet()){
                numerador += v.getValue().getValor();
            }
            u.setMedia(numerador/u.getValoraciones().size());
        }
        
        // 2. Calculo de la media de valoraciones para cada pelicula
        Iterator<Pelicula> it2 = peliculas.iterator();
        Pelicula p;
        while(it2.hasNext()){
            numerador = 0;
            p = it2.next();
            for(Map.Entry<Long,Valoracion> v : p.getValoraciones().entrySet()){
                numerador += v.getValue().getValor();
            }
             p.setMedia(numerador/p.getValoraciones().size());
        }
        
    }
    
    
    public void mostrarUsuario(Usuario u){
        System.out.println(" USUARIO "+u.getIdUsuario());
        
        System.out.println("    idUsuario: "+u.getIdUsuario());
        
    }
    public void mostrarPelicula(Pelicula p){
        System.out.println(" PELICULA "+p.getIdPelicula());
        
        System.out.println("    titulo: "+p.getTitulo());
        
    }
    
    public void mostrarMediasUsuarios(ArrayList<Usuario> usuarios){
        System.out.println(" MEDIAS DE USUARIOS");
        Iterator<Usuario> it = usuarios.iterator();
        Usuario u;
        while(it.hasNext()){
            u = it.next();
            System.out.println("    Usuario "+u.getIdUsuario()+" : "+u.getMedia());
        }
    }
    
    public void mostrarMediasPeliculas(ArrayList<Pelicula> peliculas){
        System.out.println(" MEDIAS DE PELICULAS");
        Iterator<Pelicula> it = peliculas.iterator();
        Pelicula u;
        while(it.hasNext()){
            u = it.next();
            System.out.println("    Usuario "+u.getIdPelicula()+" : "+u.getMedia());
            
        }
    }

    public void mostrarConjuntoTest(List<Usuario> test){
        
        System.out.println("  Conjunto Test de tamaño "+test.size());
        Iterator<Usuario> it = test.iterator();
        
        while (it.hasNext()){
            System.out.println("    Usuario : "+it.next().getIdUsuario());
        }
    }
    
    public void mostrarModeloSimilitud(HashMap<Long, TreeSet<ItemSim>> modeloSimilitud) {
        System.out.println("ESTADO: Modelo de similitud creado.");
        System.out.println("  Modelo de similitud:");
        long centinela = 0;
        for(Entry<Long, TreeSet<ItemSim>> e : modeloSimilitud.entrySet()){
//            if (e.getValue() == null){
//                System.out.println(" Existe una fila del modelo de similitud vacia - idP="+e.getKey());
//                centinela = e.getKey();
//            }
            System.out.println("    Pelicula("+e.getKey()+"):");
            for(ItemSim i : e.getValue()){
                System.out.println("      ("+i.getId()+"-"+i.getSim()+")");
            }
            System.out.println();
        }
//        System.out.println(" No existe una fila del modelo de similitud vacia - Centinela="+centinela);
    }

    public void testIndependiente() throws ErrorDatoInvalido, IOException {
        ArrayList<ArrayList<Usuario>> array = new ArrayList();
        
        ArrayList<Usuario> test1 = new ArrayList();
        ArrayList<Usuario> test2 = new ArrayList();
        ArrayList<Usuario> test3 = new ArrayList();
        ArrayList<Usuario> test4 = new ArrayList();
        ArrayList<Usuario> test5 = new ArrayList();
        
        /*test1 = ejecucionCosenoIAmasA(10, 0, 0);
        test2 = ejecucionCosenoIAmasA(10, 1, 0);
        test3 = ejecucionCosenoIAmasA(10, 2, 0);
        test4 = ejecucionCosenoIAmasA(10, 3, 0);
        test5 = ejecucionCosenoIAmasA(10, 4, 0);
        */
        array.add(test1);
        array.add(test2);
        array.add(test3);
        array.add(test4);
        array.add(test5);
        
        Iterator<Usuario> it1;
        Iterator<Usuario> it2;
        ArrayList<Usuario> a;
        Usuario u1;
        Usuario u2;
        
        for(int i=0;i<array.size();++i){
            for( int j=i+1;j<array.size();++j){
                it1 = array.get(i).iterator();
                while(it1.hasNext()){
                    u1 = it1.next();
                    it2 = array.get(j).iterator();
                    while(it2.hasNext()){
                        u2 = it2.next();
                        
                        if (u1.getIdUsuario() == u2.getIdUsuario()){
                            System.out.println(" test fallido. igualdad en test"+i+"-"+j+". u1="+u1.getIdUsuario()+" u2="+u2.getIdUsuario());
                        }
                        
                    }
                }
                    
                
            }
        }
    }

    public void mostrarVecinos(TreeSet<ItemSim> vecinos) {
         System.out.println("    Numero de vecinos: "+vecinos.size());
            for(ItemSim i : vecinos){
                System.out.println("      ("+i.getId()+"-"+i.getSim()+")");
            }
    }
}


