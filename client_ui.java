import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import java.io.*;
import java.net.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class client_ui extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        //-- INICIO SOCKET CLIENTE --//
        light_client skcte = new light_client();

        //-- Root node
        Accordion acordeon = new Accordion();

        //-- PANEL NUMERO 1
        TextField texto = new TextField();

        texto.prefWidthProperty().bind(primaryStage.widthProperty().subtract(210));
        Button btn = new Button("Accept");
        VBox root = new VBox();
        GridPane horizontal = new GridPane();

        ProgressBar pb = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        pb.prefWidthProperty().bind(primaryStage.widthProperty().subtract(210));
        pb.setVisible(false);
        root.getChildren().addAll(horizontal, pb);
        horizontal.setHgap(10); horizontal.setVgap(10);

        Label lblprog = new Label("PROGRESS:");
        lblprog.setVisible(false);
        horizontal.addRow(0, new Label("FILE NAME:"), texto, btn);
        horizontal.addRow(1, lblprog, pb);

        TitledPane panel1 = new TitledPane("Request Files", root);
        panel1.setFont(Font.font(13));
        panel1.setEffect(new Glow());
        btn.setOnAction( e ->  {
            lblprog.setVisible(false); pb.setVisible(false);
            if(texto.getLength() > 0){
                try{
                    pb.setProgress(-1);
                    pb.setVisible(true);
                    if(!skcte.pedirArchivosRemotos(texto.getText())){
                        lblprog.setText("NOT FOUND");lblprog.setVisible(true);
                        pb.setVisible(true); pb.setProgress(0);
                    }else{
                        lblprog.setText("COMPLETE");lblprog.setVisible(true);
                        pb.setProgress(1);pb.setVisible(true);
                    }
                }catch (IOException ex){ //Do something
                }
            }
        });

        //-- PANEL NUMERO 2
        TableView<Archivito> table = new TableView<>();
        table.setEditable(true);
        TableColumn num = new TableColumn("Num");
        TableColumn name = new TableColumn("File Name");
        name.prefWidthProperty().bind(primaryStage.widthProperty().subtract(210));
        TableColumn size = new TableColumn("File Size (Bytes)");
        size.setPrefWidth(size.getPrefWidth()+35);
        table.getColumns().addAll(num, name, size);

        num.setCellValueFactory(new PropertyValueFactory<Archivito, Integer>("num"));
        name.setCellValueFactory(new PropertyValueFactory<Archivito, String>("name"));
        size.setCellValueFactory(new PropertyValueFactory<Archivito, Long>("size"));

        TitledPane panel2 = new TitledPane("View Remote Files", table);
        panel2.setFont(Font.font(13));
        panel2.setEffect(new Glow());
        panel2.setOnMouseClicked(e -> {
            try{
                table.setItems(skcte.verArchivosRemotos());
            }catch (IOException EX){ //DO something
            }
        });

        //-- PANEL NUMERO 3
        TableView<Archivito> table2 = new TableView<>();
        table2.setEditable(true);

        TableColumn numero = new TableColumn("Num");
        TableColumn nombre = new TableColumn("File Name");
        nombre.prefWidthProperty().bind(primaryStage.widthProperty().subtract(220));
        TableColumn tamano = new TableColumn("File Size (Bytes)");
        tamano.setPrefWidth(size.getPrefWidth()+35);
        table2.getColumns().addAll(numero, nombre, tamano);

        numero.setCellValueFactory(new PropertyValueFactory<Archivito, Integer>("num"));
        nombre.setCellValueFactory(new PropertyValueFactory<Archivito, String>("name"));
        tamano.setCellValueFactory(new PropertyValueFactory<Archivito, Long>("size"));

        TitledPane panel4 = new TitledPane("View Local Files", table2);
        panel4.setFont(Font.font(13));
        panel4.setEffect(new Glow());
        panel4.setOnMouseClicked(e -> {
            String lista = Arrays.toString((skcte.Directorio.list()));
            File [] archis = skcte.Directorio.listFiles();
            if(lista.length()==2){
                return;
            }
            lista = " "+lista.substring(1, lista.length()-1);
            int conta = 0;

            ArrayList <Archivito> arryList = new ArrayList<>();
            for (String archi: lista.split(",") ){
                arryList.add(new Archivito(++conta, archi, archis[conta-1].length()));
            }
            table2.setItems(FXCollections.observableArrayList(arryList));
        });

        //-- PANEL NUMERO 3
        Text t1 = new Text("All Right Reserved 2017 - Powered By JavaFX");
        t1.setFont(Font.font(13));
        t1.setEffect(new DropShadow());

        Text t2 = new Text("Pilaba");
        t2.setFont(Font.font(13));
        t2.setEffect(new DropShadow());

        WebView WB = new WebView();
        WebEngine webEngine = WB.getEngine();
        webEngine.load("https://k61.kn3.net/69C67D031.gif");

        BorderPane BP = new BorderPane();
        BP.setTop(t2);
        BP.setBottom(t1);
        BP.setCenter(WB);

        TitledPane panel3 = new TitledPane("Extras",BP);
        panel3.setFont(Font.font(13));
        panel3.setEffect(new Glow());

        //-- add the panes to acordeon
        acordeon.getPanes().addAll(panel1,panel2,panel4,panel3);

        //-- Scene
        Scene scene = new Scene(acordeon,500,350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Cliente");
        primaryStage.show();
    }

    private class light_client {
        private static final String IP  = "localhost";
        private static final int PUERTO = 5555;
        private Socket connect;
        private DataInputStream IN;
        private DataOutputStream OUT;
        private File Directorio = new File("Directorio"); //Almacenar archivos entrantes

        light_client() throws IOException{
            if(!Directorio.exists()){
                Directorio.mkdir();
            }
            //Conexion
            connect = new Socket(IP, PUERTO);
            IN = new DataInputStream(connect.getInputStream());  //flujo entrada S -> C
            OUT = new DataOutputStream(connect.getOutputStream()); //flujo salida C -> S
        }

        private boolean pedirArchivosRemotos(String archivo) throws IOException{
            OUT.writeUTF("1");
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
                BOS.close();
                return true;
            }else{
                return false;
            }
        }

        private ObservableList<Archivito> verArchivosRemotos() throws IOException{
            OUT.writeUTF("2");
            String lista = IN.readUTF();
            String tamanos = IN.readUTF();
            if (lista.isEmpty()){
                return null;
            }
            ArrayList<Archivito> arrya = new ArrayList<>();

            String tam [] = (tamanos).split(",");
            int num = 0;
            for(String archivo : (" "+lista).split(",")){
                arrya.add(new Archivito(++num,archivo,Long.parseLong(tam[num-1].trim())));
            }
            return FXCollections.observableArrayList(arrya);
        }
    }

    public class Archivito {
        int num;
        String name;
        long size;

        Archivito(int num, String name, long size){
            this.num = num;
            this.name = name;
            this.size = size;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }
    }

}
