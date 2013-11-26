/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Modelo;

import java.util.ArrayList;

/**
 *
 * @author Pc
 */
public class pelicula {
    int id;
    String nombre;
    int anno;
    ArrayList<valoracion> valoraciones;
    float notamedia;
    
    pelicula(){
        valoraciones=new ArrayList<>();
    };
    pelicula(int aid, String anombre, int aanno){
        valoraciones=new ArrayList<>();
        id=aid;
        nombre=anombre;
        anno=aanno;
    };
    void addvaloracion(valoracion e){valoraciones.add(e);};
}
