import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class Cliente {
    private static final String IP  = "localhost";
    private static final int PUERTO = 5555;
    private Socket connect;
    private DataInputStream IN;
    private DataOutputStream OUT;
    private static File Directorio = new File("Directorio"); //Almacenar archivos entrantes

    private Cliente() throws IOException{
        if(!Directorio.exists()){
            Directorio.mkdir();
        }
        //Conexion
        connect = new Socket(IP, PUERTO);
        IN = new DataInputStream(connect.getInputStream());  //flujo entrada S -> C
        OUT = new DataOutputStream(connect.getOutputStream()); //flujo salida C -> S

        //Menu
        byte opcion;
        do {
            System.out.println("1 = Pedir archivo");
            System.out.println("2 = Ver archivos locales");
            System.out.println("3 = Ver archivos remotos");
            System.out.println("4 = Eliminar archivos locales");
            System.out.println("5 = Salir");
            opcion = new Scanner(System.in).nextByte();
            switch (opcion){
                case 1:
                    pedirArchivosRemotos(); break;
                case 2:
                    listarArchivosLocales(); break;
                case 3:
                    verArchivosRemotos(); break;
                case 4:
                    System.out.println("luego");
                    break;
            }
        }while (opcion != 5);
    }

    private void listarArchivosLocales(){
        String lista = Arrays.toString((Directorio.list()));
        if(lista.length()==2){
            return;
        }
        lista = " "+lista.substring(1, lista.length()-1);
        int num = 0;
        System.out.println("\t\t\t\t\t\t\tARCHIVOS LOCALES");
        for (String archi: lista.split(",") ){
            System.out.println("\t\t\t\t\t\t\t"+ (++num) +".- "+archi);
        }
    }

    private void verArchivosRemotos() throws IOException{
        OUT.writeUTF("2");
        String lista = IN.readUTF();
        String tamanos = IN.readUTF();
        if (lista.isEmpty()){
            return;
        }
        int num = 0;

        String tam [] = (tamanos).split(",");
        System.out.println("\t\t\t\t\t\t\tARCHIVOS REMOTOS");
        for(String archivo : (" "+lista).split(",")){
            System.out.println("\t\t\t\t\t\t\t"+ (++num) +archivo +"  "+ Long.parseLong(tam[num-1].trim())+ " Bytes");
        }
    }

    private void pedirArchivosRemotos() throws IOException{
        OUT.writeUTF("1");
        System.out.println("Nombre del archivo:");
        String archivo = new Scanner(System.in).nextLine();
        OUT.writeUTF(archivo);
        if(IN.readBoolean()){
            byte [] buffer = new byte[2048];
            BufferedInputStream BIS = new BufferedInputStream(connect.getInputStream());
            BufferedOutputStream BOS = new BufferedOutputStream(new FileOutputStream(Directorio.getName()+"/"+archivo));
            int in;
            long contador = 0;
            while ((in = BIS.read(buffer)) != -1){
                BOS.write(buffer,0,in);
                contador += in;
                if(in != 2048){
                    BOS.flush();
                    break;
                }
                BOS.flush();
            }
            System.out.println("Archivo recibido: "+archivo + "  tamaño: "+contador);
            BOS.close();
        }else{
            System.out.println("Archivo no existe");
        }
    }

    public static void main(String[] args)throws IOException {
        new Cliente();
    }
}
