package Summauspalvelu;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;


// Pääluokka
public class Main{
    private static final int PORTTI = 2000;

    private static final int SULJE= 0;
    private static final int ANNASUMMA= 1;
    private static final int ANNASUURIN= 2;
    private static final int ANNALUKUMAARA= 3;

    private ArrayList<Orja> orjat = new ArrayList<>();

    private ObjectInputStream DataInput;
    private ObjectOutputStream DataOutput;

    private ServerSocket PalvelinSoketti;
    private Socket AsiakasSoketti;
    private DatagramSocket DGramSoketti;

    public static void main(String[] args){
        Main main = new Main("127.0.0.1", 3126+"", PORTTI+"");
    }

    public Main(String osoite, String kohdePortti, String viesti){
        UDPSend(osoite, Integer.parseInt(kohdePortti), Integer.toString(PORTTI));
        TCPForm();
        int tarvittavaMaara = 0;
        try {
            tarvittavaMaara = DataInput.readInt();
        }catch(Exception e){
            e.printStackTrace();
        }
        PalvelinKaynnistus(tarvittavaMaara);
        try {
            while(kasitteleSyote(DataInput.readInt())!= -1){};
        }catch(Exception e){
            e.printStackTrace();
        }
        for (int i = 0; i < orjat.size(); i++) {
            orjat.get(i).YhteysSulku();
        }
    }


    private void UDPSend(String osoite, int kohdePortti, String viesti){

        try {
            InetAddress kohdeOsoite = InetAddress.getByName(osoite);
            DGramSoketti = new DatagramSocket();
            byte[] lahetettavaData = Integer.toString(PORTTI).getBytes();
            DatagramPacket paketti = new DatagramPacket(lahetettavaData, lahetettavaData.length, kohdeOsoite, kohdePortti);
            DGramSoketti.send(paketti);
            DGramSoketti.close();
        } catch (IOException e) {
            System.err.println("UDP-paketin lähettäminen ei onnistunut.");
            e.printStackTrace();
            System.exit(1000);
        }
    }


    private boolean TCPForm(){
        try{
            PalvelinSoketti = new ServerSocket(PORTTI);
            AsiakasSoketti = PalvelinSoketti.accept();
            DataInput = new ObjectInputStream(AsiakasSoketti.getInputStream());//Sisään
            DataOutput = new ObjectOutputStream(AsiakasSoketti.getOutputStream());//Ulos
        }catch(SocketTimeoutException e){ //Soketti timeout
            try {
                if(DataOutput != null){
                    DataOutput.flush();
                    DataInput.close();
                    DataOutput.close();
                }
                PalvelinSoketti.close(); //Suljetaan soketti
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    private void PalvelinKaynnistus(int maara){
        for(int i=1;i <= maara;i++){
            System.out.println("Portti "+ (PORTTI+i));
            try {
                orjat.add(new Orja((PORTTI+i)));
                orjat.get(i-1).start();
                DataOutput.writeInt(PORTTI+i);
                DataOutput.flush();
            } catch (IOException e) {
                System.out.println("Portin lähettäminen epäonnistui");
                e.printStackTrace();
            }
        }
    }

    public int kasitteleSyote(int luku) throws Exception{
        switch(luku) {
            case SULJE:
                orjat.forEach(orja -> orja.YhteysSulku());
                return -1;
            case ANNASUMMA:
                int tulos1 = Orja.haeSumma();
                DataOutput.writeInt(tulos1);
                DataOutput.flush();
                return ANNASUMMA;
            case ANNASUURIN:
                int tulos2 = Orja.annaSuurin();
                DataOutput.writeInt(tulos2-PORTTI);
                DataOutput.flush();
                return ANNASUURIN;
            case ANNALUKUMAARA:
                int tulos3 = Orja.haeKokonaisMaara();
                DataOutput.writeInt(tulos3);
                DataOutput.flush();
                return ANNALUKUMAARA;
            default:
                System.out.println("Boop.");
                return -1;
        }
    }


}