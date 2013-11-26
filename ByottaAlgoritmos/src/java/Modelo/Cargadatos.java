/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Modelo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Pc
 */
public class Cargadatos {

    /**
     * @param args the command line arguments
     */
    public static void cargarpeliculas(String archivo, ArrayList<pelicula> pelis)
    {
        BufferedReader br = null;
        String line = "";
        int i = 0;
        pelicula temp=null;
        try {
		br = new BufferedReader(new FileReader(archivo));
                line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] peli = line.split("\t");
                            temp = new pelicula(Integer.parseInt(peli[0]),peli[2],0);
                            //System.out.println("id |" + Integer.parseInt(peli[0]) + "| Nombre: |" + peli[2] + "| anno: |0|");
                            pelis.add(temp);
                            peli[0]="0";peli[1]="0";peli[2]="";
		}
                System.out.println("peliculas cargadas");
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
        }catch(NumberFormatException e) {
            System.out.println("la cagaste");
            e.printStackTrace();
	} finally {
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    }
    
    public static void cargarvaloraciones(String archivo, ArrayList<pelicula> pelis)
    {
        BufferedReader br2 = null;
        String line2 = "";
        int i = 0;
        
	try {                
                br2 = new BufferedReader(new FileReader(archivo));
                line2 = br2.readLine(); //para quitar la linea de cabecera
                valoracion tempv;
                System.out.println("cargando valoracione...");
		while ((line2 = br2.readLine()) != null) {
                        String[] cadenavaloracion = line2.split(",");
                        int indice = Integer.parseInt(cadenavaloracion[1]);
                        tempv = new valoracion(Integer.parseInt(cadenavaloracion[2]),Integer.parseInt(cadenavaloracion[0]));
                        pelis.get(indice-1).addvaloracion(tempv);
                        i++;
                        //System.out.println(((i/3085))+ "% completo");
                }
                System.out.println("valoraciones cargadas");
 
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
        }catch(NumberFormatException e) {
            System.out.println("la cagaste");
            e.printStackTrace();
	} finally {
		if (br2 != null) {
			try {br2.close();} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    }
    public static void main(String[] args) {
        // TODO code application logic here
        ArrayList<pelicula> pelis=new ArrayList<>();
        
        String archivopelis = "C:\\Users\\Pc\\Documents\\NetBeansProjects\\algoritmos\\src\\algoritmos\\peliculas2.csv";
	String archivovaloraciones = "C:\\Users\\Pc\\Documents\\NetBeansProjects\\algoritmos\\src\\algoritmos\\ratings7.csv";
	
        cargarpeliculas(archivopelis, pelis);
        cargarvaloraciones(archivovaloraciones, pelis);
        
        //System.out.println(pelis.get(1000).valoraciones.get(0).estrellas);
 
	System.out.println("Done");
        //______________________
        
        
        
        
        
        
        
        
        
        
        
    }
    
}
