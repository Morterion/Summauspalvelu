package Summauspalvelu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//Summauspalvelin
public class Orja extends Thread{
    private static final int LOPETA = 0;
    private static int kokonaisMaara = 0;
    private static Map<Integer, Integer> arvokartta = Collections.synchronizedMap(new HashMap<Integer, Integer>());
    private int Portti;
    private ServerSocket PalvelinSoketti;
    private Socket AsiakasSoketti;
    private boolean Kaynnissa;

    public Orja(int portti) throws IOException{
        super();
        this.Portti = portti;
        PalvelinSoketti = new ServerSocket(Portti);
        Kaynnissa = false;
    }

    public void run() {
        try {
            Kaynnissa = true;
            AsiakasSoketti = PalvelinSoketti.accept();
            ObjectInputStream ObjektiSisaantulo = new ObjectInputStream(AsiakasSoketti.getInputStream());
            while (Kaynnissa) {
                try {
                    int Luku = ObjektiSisaantulo.readInt();
                    if (Luku == LOPETA) {
                        YhteysSulku();
                        break;
                    } else {
                        lisaaArvo(Portti, Luku);
                    }
                } catch (Exception e) {
                    Kaynnissa = false;
                    break;
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void YhteysSulku(){
        System.out.println("Suljetaan yhteydet..");
        try{
            AsiakasSoketti.close();
            PalvelinSoketti.close();
        }catch(IOException e){
            System.out.println("Virhe sokettien sulkemisessa.");
            e.printStackTrace();
        }
        Kaynnissa = false;
    }


    // Staattiset metodit
    public static void lisaaArvo(int portti, int arvo){
        System.out.println(portti+" "+arvo);
        int summa = arvokartta.get(portti) != null?arvokartta.get(portti): 0;
        summa += arvo;
        arvokartta.put(portti,summa);
        kokonaisMaara++;
    }

    public static int annaSuurin(){
        int suurin = 0;
        for(int portti: arvokartta.keySet()){
            if(suurin == 0){
                suurin = portti; continue;
            }
            if(arvokartta.get(portti)>arvokartta.get(suurin)){ suurin = portti;}
        }
        return suurin;
    }

    public static int haeKokonaisMaara(){return kokonaisMaara;}

    public static int haeSumma(){
        int summa = 0;
        for(int x: arvokartta.keySet()){
            summa += arvokartta.get(x);
        }
        return summa;
    }
}