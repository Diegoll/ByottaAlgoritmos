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
public class Usuario {
    int id;
    String nombre;
    ArrayList<valoracion> valoraciones;

    Usuario(int indiceusu) {
        id=indiceusu; 
        valoraciones=new ArrayList<>();
    }
    
    
}
