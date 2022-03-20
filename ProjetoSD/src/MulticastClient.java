import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

/**
 * The MulticastClient class joins a multicast group and loops receiving
 * messages from that group. The client also runs a MulticastUser thread that
 * loops reading a string from the keyboard and multicasting it to the group.
 * <p>
 * The example IPv4 address chosen may require you to use a VM option to
 * prefer IPv4 (if your operating system uses IPv6 sockets by default).
 * <p>
 * Usage: java -D java.net.preferIPv4Stack=true MulticastClient
 *
 * @author Raul Barbosa
 * @version 1.0
 */
public class MulticastClient extends Thread {
    private final String MULTICAST_ADDRESS;
    private final int PORT = 4321;
    private final Data data;

    public MulticastClient(long number, String name, Data data, String ipMesa) {

        super("TERMINAL " + name+ " " + number);
        this.data = data;
        this.MULTICAST_ADDRESS = ipMesa;
    }

    public static void main(String[] args) {

        if(args.length < 1){
            System.out.println("(!) INSIRA O NOME DO DEPARTAMENTO E NUMERO DO TERMINAL COMO ARGUMENTOS");
            return;
        }

        //READING PROPERTIES FILE
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("config.properties");
        } catch (FileNotFoundException e) {
            System.out.println("Erro a ler ficheiro de propriedades");
        }
        Properties props = new Properties();
        try {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Erro a dar carregar ficheiro de propriedades");
        }

        String value = (String)props.get(args[0]);
        String[] ips = value.split(" ");

        try {
            long number = Integer.parseInt(args[1]);
            Data data = new Data();
            MulticastClient client = new MulticastClient(number,args[0], data, ips[0]);
            client.start();
            MulticastUser user = new MulticastUser(number,args[0], data, ips[0], ips[1]);
            user.start();
        }
        catch (Exception e){
            System.out.println("PROPERTIES FILE COM ERROS");
        }
    }

    public void run() {
        MulticastSocket socket = null;
        try {
            socket = new MulticastSocket(PORT);  // create socket and bind it
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            send(socket, "type | terminalCrash; id | " + this.getName());
            while (true) {
                String message = readMessage(socket);
                readComands(socket,message);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    private String readMessage(MulticastSocket socket) throws IOException {

        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }

    private void getCandidatesList(HashMap<String, String> map){
        int size = Integer.parseInt(map.get("list_item"));
        System.out.println("LISTAS CANDIDATAS");
        for(int i=0; i<size; i++){
            String key = "item_" + i;
            System.out.println(i + ". " + map.get(key));
        }
        //OPÇÃO DE VOTO EM BRANCO
        System.out.println(size + ". Voto Branco");
        System.out.print("> ");
    }

    private void readComands(MulticastSocket socket, String message) throws IOException, InterruptedException {
        HashMap<String,String> map = new HashMap();
        String[] pares =  message.split("; ");

        for(String comandos : pares) {
            String[] a = comandos.split(" \\| ");
            map.put(a[0], a[1]);
        }

        //MESSAGE TO ALL TERMINALS
        if(map.get("type").equals("free")){
            if(data.isFree()){
                send(socket, "type | freeTerminal; id | " + this.getName() + "; request | " + map.get("request") + "; userCC | " + map.get("userCC") + "; election | " + map.get("election"));
            }
        }
        //MESSAGES TO SPECIFIC TERMINAL
        else if(map.get("id").equals(this.getName())){
            //IF THIS WAS THE CHOSEN TERMINAL
            if(map.get("type").equals("chosen")){
                    int userCC = Integer.parseInt(map.get("userCC"));
                    data.setFree(false);
                    data.setUserCC(userCC);
                    data.setBlocked(false);
                    data.setElectionName(map.get("election"));
            }
            //VERIFY IF LOGIN CREDENTIALS ARE CORRECT
            else if(map.get("type").equals("status")){
                if(map.get("logged").equals("on")){
                    System.out.println("Welcome to eVoting");
                    send(socket, "type | candidates; election | " + data.getElectionName() + "; id | " + this.getName());
                    data.setLoggedIn(true);
                }
                else{
                    data.setLoggedIn(false);
                    System.out.println("\n(!) CREDENCIAIS ERRADAS\n");
                }
                data.go();
            }
            else if(map.get("type").equals("candidatesList")){
                getCandidatesList(map);
            }
        }

    }

    private void send(MulticastSocket socket, String message) throws IOException {
        byte[] buffer = message.getBytes();
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
    }
}

class MulticastUser extends Thread {
    private final String VOTE_ADDRESS;
    private final String MULTICAST_ADDRESS;
    private final int PORT = 4321;
    private final Data data;

    public MulticastUser(long number,String name, Data data, String ipMesa, String ipVoto) {
        super("TERMINAL " + name+ " " + number);
        this.data = data;
        this.VOTE_ADDRESS = ipVoto;
        this.MULTICAST_ADDRESS = ipMesa;
    }

    private void send(MulticastSocket socket, String message) throws IOException {
        byte[] buffer = message.getBytes();
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
    }

    private void sendVote(MulticastSocket socket, String message) throws IOException {
        byte[] buffer = message.getBytes();
        InetAddress group = InetAddress.getByName(VOTE_ADDRESS);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
        socket.send(packet);
    }
    
    private String getInput(Scanner keyboardScanner) throws IOException {

        long sTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - sTime < 60000)
        {
            if (System.in.available() > 0)
            {
                data.setBlocked(false);
                return keyboardScanner.nextLine();
            }
        }
        data.setBlocked(true);
        data.setFree(true);
        data.setLoggedIn(false);
        return null;
    }
    
    public void run() {
        MulticastSocket socket = null;
        MulticastSocket voteSocket = null;
        try {
            socket = new MulticastSocket();  // create socket without binding it (only for sending)
            voteSocket = new MulticastSocket(); // create socket to send vote
            Scanner keyboardScanner = new Scanner(System.in);

            while (true) {
                System.out.println("----------< " + this.getName() + " >----------");
                System.out.println("Prima Enter para continuar...");
                keyboardScanner.nextLine();
                if(data.getBlocked()){
                    System.out.println("\n(!) A MAQUINA ENCONTRA-SE BLOQUEADA, DIRIJA-SE À MESA DE VOTO\n");
                }
                else{
                    String username;
                    String password = null;
                    while(true){
                        System.out.print("Username: ");
                        username = getInput(keyboardScanner);
                        if(username == null || username.length() > 0){
                            break;
                        }
                        else{
                            System.out.println("(!) USERNAME INVALIDO");
                        }
                    }

                    if(!data.getBlocked()){
                        while(true){
                            System.out.print("Password: ");
                            password = getInput(keyboardScanner);
                            if(password == null || password.length() > 0){
                                break;
                            }
                            else{
                                System.out.println("(!) PASSWORD INVALIDA");
                            }
                        }
                    }
                    if(!data.getBlocked()){
                        send(socket, "type | login; id | " + this.getName() + "; userCC | " + data.getUserCC() + "; username | " + username + "; password | " + password);
                        data.stop();
                    }
                    else{
                        System.out.println("(!) MAQUINA BLOQUEADA, PASSARAM 60 SEGUNDOS");
                    }
                    if(data.isLoggedIn()){
                        //INPUT COM OPÇÃO DE VOTO
                        String vote = getInput(keyboardScanner);
                        if(!data.getBlocked()){
                            sendVote(voteSocket, "type | vote; election | " + data.getElectionName() + "; option | " + vote + "; id | " + this.getName());
                            sendVote(voteSocket, "type | elector; election | " + data.getElectionName() + "; userCC | " + data.getUserCC());
                            data.setBlocked(true);
                            data.setFree(true);
                            data.setLoggedIn(false);
                        }
                        else{
                            System.out.println("(!) MAQUINA BLOQUEADA, PASSARAM 60 SEGUNDOS");
                        }
                    }
                    Thread.sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            assert socket != null;
            socket.close();
            assert voteSocket != null;
            voteSocket.close();
        }
    }
}

class Data{
    private int userCC;
    private boolean free;
    private boolean blocked;
    private boolean loggedIn;
    private String electionName;

    public Data(){
        this.userCC = 0;
        this.free = true;
        this.blocked = true;
        this.loggedIn = false;
        this.electionName = "";
    }

    synchronized void stop() throws InterruptedException {
        wait(30000);
    }

    synchronized void go() throws InterruptedException {
        notify();
    }
    public void setUserCC(int userCC){
        this.userCC = userCC;
    }

    public int getUserCC() {
        return userCC;
    }

    public void clearUser(){
        this.userCC = 0;
    }

    public void setBlocked(boolean blocked){
        this.blocked = blocked;
    }

    public boolean getBlocked(){
        return blocked;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public void setElectionName(String electionName) {
        this.electionName = electionName;
    }

    public String getElectionName() {
        return electionName;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }
}
