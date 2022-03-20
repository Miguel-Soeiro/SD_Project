import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Classe que implementa o server rmi, contém todos os metodos remotos
 *
 */
public class RMIServer extends UnicastRemoteObject implements RMI_S_I {

    /**
     * ArrayList com as pessoas registadas no server
     */
    public static  ArrayList<User> listaPessoas;
    /**
     *  ArrayList com as eleicoes registadas no server
     */
    public static ArrayList<Eleicao> listaEleicoes;
    /**
     *  ArrayList com os departamentos registados no server
     */
    public static ArrayList<Departamento> listaDepartamentos;
    /**
     *  ArrayList com os clients ligados aoserver
     */
    public static ArrayList<RMI_C_I> client= new ArrayList<>();

    /**
     * Construtor da classe RMIServer
     * @throws RemoteException
     */
    protected RMIServer() throws RemoteException {
        super();
    }

    //****************************************** MÉTODOS CHAMADOS PELA CONSOLA DE ADMINISTRAÇÃO *******************************************


    /**
     * Metodo que adiciona os clientes que se liga ao rmi server a lista de clientes. Tambem faz
     * a invocacao do metodo escreveFicheiroClients para a escrita dos mesmos
     * @param c Cliente que se liga
     * @return String com a mensagem "Registado no servidor"
     */
    public String teste (RMI_C_I c){
        client.add(c);
        System.out.println("Cliente Registado");
        escreveFicheiroClients();
        return "Registado no Servidor";
    }

    /**
     * Metodo que retorna a lista de departamentos
     * @return lista de departamentos
     */
    public ArrayList<Departamento> getListaDepartamentos() {
        return listaDepartamentos;
    }

    /**
     * Metodo que regista uma Pessoa no sistema
     * @param nome nome da pessoa a registar
     * @param tipo tipo de pessoa, Estudante, Funcionario ou Docente
     * @param password password da pessoa a registar
     * @param departamento departamento a que a pessoa a registar pertence
     * @param CC cartao de cidadao da pessoa a registar
     * @param CC_val validade do cartao de cidadao
     * @param telemovel numero de telemovel da pessoa a registar
     * @param morada morada da pessoa a registar
     * @return retorna true se a pessoa foi registada com sucesso
     */
    public boolean registarPessoa(String nome, String tipo, String password, Departamento departamento, int CC, GregorianCalendar CC_val, int telemovel, String morada){
        User p = new User(nome, tipo, password, departamento, CC, CC_val, telemovel, morada);
        System.out.println(p);
        listaPessoas.add(p);
        escreveFicheiroPessoas();
        return true;
    }

    /**
     * Metodo que cria uma eleicao no sistema
     * @param data_inicio data de começo da eleicao
     * @param data_final data de fim da eleicao
     * @param titulo nome da eleicao
     * @param descricao descricao da eleicao
     * @param dept departamento a que a eleicao pertence
     * @param tipo_Pessoa tipo de pessoa que pode votar na eleicao, Estudante, Funcionario, Docente
     * @return retorna uma string caso a eleicao seja criada com sucesso
     */
    public String criarEleicao(GregorianCalendar data_inicio, GregorianCalendar data_final, String titulo, String descricao, Departamento dept, String tipo_Pessoa){

        Eleicao e = new Eleicao(data_inicio,data_final,titulo,descricao,dept,tipo_Pessoa);
        System.out.println(e);

        //talvez seja necessario fazer verificaçao se a eleiçao ja existe
        listaEleicoes.add(e);
        escreveFicheiroEleicoes();
        return "\nELEIÇÃO CRIADA COM SUCESSO\n";

    }

    /**
     * Metodo que adiciona uma lista candidata a uma determinada eleicao
     * @param e eleicao a qual uma lista candidata vai ser adicionada
     * @param nome nome da lista candidata a ser adicionada
     */
    public void AddListaCandidata(Eleicao e, String nome){
        int i=0;
        Eleicao aux=null;
        for (Eleicao e2: listaEleicoes) {
            if(e2.getTitulo().equals(e.getTitulo())){
                System.out.println("Entrei aqui");
                e2.addListaCandidata(nome);
                System.out.println(e2.getListaCandidata());
                e2.printLista();
            }
            i++;
        }
        escreveFicheiroEleicoes();


    }

    /**
     * Metodo que remove uma lista candidata a uma determinada eleicao
     * @param e eleicao a qual uma lista candidata vai ser removida
     * @param nome nome da lista candidata a ser removida
     */
    public void RemoveListaCandidata(Eleicao e, String nome){
        for (Eleicao el: listaEleicoes ) {
            if(el.getTitulo().equals(e.getTitulo())){
                System.out.println(el.getListaCandidata());
                el.removeListaCandidata(nome);
                escreveFicheiroEleicoes();
                return;
            }
        }
    }

    /**
     * Metodo que adiciona uma Mesa de Voto a uma determinada eleicao
     * @param e Eleicao a qual vai ser adicionada a mesa de voto
     * @param d Departamento a que a mesa de voto pertence
     */
    public void  AddMesaVoto(Eleicao e, Departamento d) {

       for (Eleicao el: listaEleicoes) {
            if( el.getTitulo().equals(e.getTitulo()) && el.getDescricao().equals(e.getDescricao())){
                el.getDept().add(d);
                System.out.println("Mesa adicionada com sucesso");
                escreveFicheiroEleicoes();
                return;
            }
        }
    }

    /**
     * Metodo que remove uma Mesa de Voto a uma determinada eleicao
     * @param e Eleicao a qual vai ser removida a mesa de voto
     * @param d Departamento a que a mesa de voto pertence
     */
    public void  RemoverMesaVoto(Eleicao e, Departamento d) {

        for (Eleicao el: listaEleicoes) {
            if( el.getTitulo().equals(e.getTitulo()) && el.getDescricao().equals(e.getDescricao())){
                el.removeDepartamento(d);
                System.out.println("Mesa removida com sucesso");
                escreveFicheiroEleicoes();
                return;
            }
        }
    }

    /**
     * Metodo que altera as propriedades de uma determinada eleicao
     * @param eleicao Nome da eleicao que vai sofrer as alteracoes
     * @param data_inicio Data de inicio da eleicao
     * @param data_fim Data de fim da eleicao
     * @param titulo Titulo da eleicao
     * @param descricao Descricao da eleicao
     * @return retorna a string "Propriedades Alteradas com sucesso"
     */

    //TODO VER ISTO, NORMALMENTE JA NAO É PRECISO COMPARAR DATAS
    public String AlteraEleicao(String eleicao, GregorianCalendar data_inicio ,GregorianCalendar data_fim,String titulo, String descricao){

        GregorianCalendar date = (GregorianCalendar) Calendar.getInstance();
        for(Eleicao e : listaEleicoes){
            if (e.getTitulo().equals(eleicao)){
                if(e.getData_inicio().compareTo(date) < 0){
                    return "NAO E POSSIVEL";
                }
                e.setData_inicio(data_inicio);
                e.setData_final(data_fim);
                e.setTitulo(titulo);
                e.setDescricao(descricao);
            }
        }
        escreveFicheiroEleicoes();
        return "Propriedades Alteradas com sucesso";
    }

    /**
     * Metodo que adiciona um Departamento a lista de Departamentos do programa
     * @param d Departamento que vai ser adicionado
     * @return retorna true caso seja possivel adicionar o departamento e false caso nao seja
     */
    public boolean AddDepartamento(Departamento d){
        if(checkDepartamentExist(d)){
            listaDepartamentos.add(d);
            escreveFicheiroDepartamentos();

            System.out.println("Novo departamento adicionado ao sistema: " + listaDepartamentos);
            return true;
        }
        return false;
    }

    /**
     * Metodo que verifica se o departamento ja existe na lista de departamentos do programa
     * @param d Departamento que vai ser testado
     * @return retorna false se o departamento ja existe no programa e true se nao existe
     */
    public boolean checkDepartamentExist(Departamento d){
        for(Departamento dept : listaDepartamentos){
            if(dept.getNome().equals(d.getNome())){
                return false;
            }
        }
        return true;

    }

    /**
     * Metodo que vai buscar as Eleicoes cuja a data ja passou
     * @return ArrayList com as eleicoes cuja data ja passou
     */
    public ArrayList<Eleicao> getEleicoesPassadas(){
        ArrayList<Eleicao> listaEleicoesPassadas = new ArrayList<>();
        GregorianCalendar date = (GregorianCalendar) Calendar.getInstance();
        for (Eleicao e : listaEleicoes){
            if(e.getData_final().compareTo(date) < 0){
                listaEleicoesPassadas.add(e);
            }
        }
        return listaEleicoesPassadas;
    }

    /**
     * Metodo que vai buscar as Eleiçoes que ainda nao comecaram
     * @return ArrayList com as eleicoes que ainda nao comecaram
     */
    public ArrayList<Eleicao> getEleicoesElegiveis(){
        ArrayList<Eleicao> elegieis = new ArrayList<>();
        GregorianCalendar date = (GregorianCalendar) Calendar.getInstance();
        for(Eleicao e : listaEleicoes){
           if(e.getData_inicio().compareTo(date) > 0){
               elegieis.add(e);
           }
        }
        return elegieis;
    }

    /**
     * Metodo que vai retornar os departamentos que sao possivel adicionar a uma determinada eleicao
     * @param e Eleicao que se pretende saber quais os departamentos que ainda e possivel adicionar
     * @return ArrayList com os departamentos elegiveis
     */
    public ArrayList<Departamento> getDepartamentosElegiveis(Eleicao e){
        ArrayList<Departamento> deptsElegiveis = new ArrayList<>();
        boolean encontrou = false;
        for (Departamento d: listaDepartamentos) {
            for(Departamento d2 : e.getDept()){
                if(d2.getNome().equals(d.getNome())){
                    encontrou= true;
                }
            }
            if(encontrou == false){
                deptsElegiveis.add(d);
            }
            else{
                encontrou= false;
            }
        }
        return deptsElegiveis;
    }

    /**
     * Metodo que vai retornar um ArrayList com todos os locais em que uma determinada pessoa votou
     * @param pessoa Pessoa que se quer saber onde voto
     * @return ArrayList com os locais de voto
     */
    public ArrayList<String> LocalVoto(String pessoa){
        ArrayList<String> locais = new ArrayList<>();
        for (Eleicao e: listaEleicoes) {
            for(User p: e.getVotantes()){
                if(p.getNome().equals(pessoa)){
                    String s = "Eleiçao: " + e.getTitulo() + "  " + "Local: " + p.getLocalVoto();
                    locais.add(s);
                }
            }
        }
        return locais;
    }


    //******************************************** METODOS CHAMADOS PELO SERVIDOR MULTICAST **************************************************

    /**
     * Metodo que vai percorrer a lista de consolas de administracao
     * e efetuar callback de modo a enviar uma notificacao.
     * @param message - Conteudo da notificacao
     * @param priority - Prioridade da notificacao 0 ou 1
     */
    public void sendNotification(String message, int priority){
        //ENVIAR MENSAGEM AOS CLIENTES
        ArrayList<Integer> indices = new ArrayList<>();
        int i = 0;
        for(RMI_C_I c : client){
            try {
                c.notification(message, priority);
            } catch (RemoteException e) {
                indices.add(i);
                System.out.println("Notificaçao nao enviada!");

            }
            i++;
        }
        for (int j = indices.size()-1 ; j >= 0 ; j--){
            client.remove(client.get(indices.get(j)));
        }
    }

    /**
     * @param CC Numero de cartao de cidadao inserido na mesa
     * @param department nome da mesa onde o eleitor inseriu as credenciais
     * @return true se o eleitor estiver registado e false caso contrario
     */
    public boolean isRegistered(int CC, String department){

        for(User pessoa : listaPessoas){
            if(pessoa.getCC() == CC){
                String message = "Eleitor com o nome " + pessoa.getNome() + " e CC " + CC + ", chegou à mesa " + department + ".";
                sendNotification(message,0);
                return true;
            }
        }
        return false;
    }

    /**
     * Indica as consolas de administracao que a mesa se encontra ativa
     * enviando uma notificacao atraves de um callbak
     * @param dept Nome da mesa que foi iniciada
     */
    public void ping(String dept){
        for (Departamento d: listaDepartamentos) {
            if(d.getNome().equals(dept)){
                d.setMesaOn(true);
                break;
            }
        }
        String message = "A mesa " + dept + " encontra-se em funcionamento!";
        sendNotification(message,0);
    }

    /**
     * Indica as consolas de administracao que a mesa se encontra desativadas
     * ou sofreram um crash enviando uma notificacao atraves de um callbak
     * @param department Nome da mesa que foi abaixo
     */
    public void crash(String department){
        for (Departamento d: listaDepartamentos) {
            if(d.getNome().equals(department)){
                d.setMesaOn(false);
                break;
            }
        }
        String message = "(!) A mesa " + department + " foi abaixo!";
        sendNotification(message,1);
    }

    /**
     * Funcao para verificar se as credencias de autenticacao foram bem colocada
     * pelo eleitor no terminal de voto
     * @param userCC - Numero do cartao de cidadaos
     * @param name - Nome do eleitor
     * @param password - Password
     * @return True - Se os dados estao corretos. False - Se os dados estao errados
     */
    public boolean acceptLogin(int userCC, String name, String password){
        for(User pessoa : listaPessoas){
            if(pessoa.getCC() == userCC && pessoa.getNome().equals(name) && pessoa.getPassword().equals(password)){
                String message = "Eleitor com o nome " + name + " e CC " + userCC + ", efetuou login num terminal.";
                sendNotification(message,0);
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo para obter uma lista com o nome das eleicoes que sao comuns ao eleitor e
     * a mesa. Retorna todas as eleicoes que estejam a decorrer na mesa e que sejam compativeis
     * com o eleitor.
     * @param userCC - Numero do cartao de cidadao
     * @param departamento - Nome do departamento onde se encontra a mesa
     * @return ArrayList com o nome das eleicoes
     */
    public ArrayList<String> getElections(int userCC, String departamento){
        User eleitor = null;
        ArrayList<String> eleicoes = new ArrayList<>();

        //ENCONTRAR ELEITOR
        for(User p : listaPessoas){
            if(p.getCC() == userCC){
                eleitor = p;
            }
        }
        GregorianCalendar date = (GregorianCalendar) Calendar.getInstance();
        //ENCONTRAR ELEICOES COMUNS HA MESA E ELEITORES
        for(Eleicao e : listaEleicoes){
            boolean voted = false;
            if(e.getTipo_Pessoa().equals(eleitor.getTipo()) && e.getData_inicio().compareTo(date) < 0 && e.getData_final().compareTo(date) > 0){
                for(User p : e.getVotantes()){
                    if(p.getCC() == userCC){
                        voted = true;
                        break;
                    }
                }
                for(Departamento d : e.getDept()){
                    if(d.getNome().equals(departamento) && voted == false){
                        eleicoes.add(e.getTitulo());
                        break;
                    }
                }
            }
        }
        return eleicoes;
    }

    /**
     * Metodo para obeter as listas pertencentes a uma determinada eleicao
     * @param election - Nome da eleicao
     * @return ArrayList com as listas de uma dada eleicao
     */
    public ArrayList<String> getCandidates(String election){

        ArrayList<String> listas = new ArrayList<String>();

        //ENCONTRAR A ELEIÇÃO
        for(Eleicao e : listaEleicoes){
            if(e.getTitulo().equals(election)){
                //RETIRA AS LISTAS CANDIDATAS
                for(ListaCandidata lista : e.getListaCandidata()){
                    listas.add(lista.getNome());
                }
                return listas;
            }
        }
        return listas;
    }

    /**
     * Regista o voto
     * @param election - Nome da eleicao
     * @param option - Opcao em que pretende votar
     */
    public void vote(String election, String option){
        int vote;
        try{
            vote = Integer.parseInt(option);
        }catch(Exception e){
            vote = -1;
        }
        for(Eleicao e : listaEleicoes){
            if(e.getTitulo().equals(election)){

                //SE FOR UMA OPCAOO INVALIDA CONSIDERA VOTO NULO
                if( vote > e.getListaCandidata().size() || vote < 0 ){
                    e.addVotoNulo();
                    System.out.println("Voto Nulo");

                } // SE A OPÇÃO FOR IGUAL AO TAMANHO DO ARRAY, ESCOLHEU A OPÇÃO DE VOTO EM BRANCO
                else if( vote == e.getListaCandidata().size() ){
                    e.addVotoBranco();
                    System.out.println("Voto em branco");
                    e.addTotalVotos();
                }

                else{
                    e.getListaCandidata().get(vote).addVote();
                    System.out.println(e.getListaCandidata().get(vote));
                    e.addTotalVotos();
                }

                escreveFicheiroEleicoes();
                return;
            }
        }
    }

    /**
     * Adiciona o eleitor a lista de votantes de uma determinada eleicao
     * @param election - Nome da eleicao
     * @param userCC - Numero de cartao de cidadao
     * @param department - Nome da mesa em que votou
     */
    public void addElector(String election, int userCC, String department){
        User eleitor = null;
        User pessoa = null;
        //PROCURAR PESSOA
        for(User p : listaPessoas){
            if(userCC == p.getCC()){
                pessoa = p;
                break;
            }
        }
        //FAZER COPIA DOS DADOS
        if(pessoa != null){
            eleitor = new User(pessoa.getNome(), userCC, department);
        }
        //ADICIONAR VOTANTE
        for(Eleicao e : listaEleicoes){
            if(e.getTitulo().equals(election)){
                e.getVotantes().add(eleitor);
                break;
            }
        }
        String message = "Eleitor com o nome " + pessoa.getNome() + " e CC " + userCC + ", efetuou o voto na mesa " + department;
        sendNotification(message,0);
    }


    //******************************************** METODOS DE LEITURA E ESCRITA DE OBJETOS **************************************************

    /**
     * Metodo que escreve para um ficheiro objeto as pessoas existentes no programa
     */
    public  void escreveFicheiroPessoas(){

        File f = new File("Pessoas.obj");

        try{
            FileOutputStream os = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(listaPessoas);

            oos.close();

        }catch(FileNotFoundException e){
            System.out.println("Erro a criar ficheiro");
        }
        catch(IOException e){
            System.out.println("Erro a escrever para ficheiro");
        }
    }

    /**
     * Metodo que escreve para um ficheiro objeto as eleicoes existentes no programa
     */
    public void escreveFicheiroEleicoes(){

        File f = new File("Eleicoes.obj");

        try{
            FileOutputStream os = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(listaEleicoes);

            oos.close();

        }catch(FileNotFoundException e){
            System.out.println("Erro a criar ficheiro");
        }
        catch(IOException e){
            System.out.println("Erro a escrever para ficheiro");
        }
    }

    /**
     * Metodo que escreve para um ficheiro objeto os departamentos existentes no programa
     */
    public void escreveFicheiroDepartamentos(){

        File f = new File("Departamentos.obj");

        try{
            FileOutputStream os = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(listaDepartamentos);

            oos.close();

        }catch(FileNotFoundException e){
            System.out.println("Erro a criar ficheiro");
        }
        catch(IOException e){
            System.out.println("Erro a escrever para ficheiro");
        }
    }

    /**
     * Metodo que escreve para um ficheiro objeto os clientes existentes no programa
     */
    public void escreveFicheiroClients(){

        File f = new File("Clients.obj");

        try{
            FileOutputStream os = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(os);

            oos.writeObject(client);

            oos.close();

        }catch(FileNotFoundException e){
            System.out.println("Erro a criar ficheiro");
        }
        catch(IOException e){
            System.out.println("Erro a escrever para ficheiro");
        }
    }

    /**
     * Metodo que le de um ficheiro objeto os clientes existentes no programa
     */
    public static void lerFicheiroClients(){
        File f = new File("Clients.obj");
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);

            client = (ArrayList<RMI_C_I>) ois.readObject();
            ois.close();

        }catch (FileNotFoundException e){
            System.out.println("Ficheiro Clients ainda nao existe");
            listaPessoas = new ArrayList<>();
        } catch(IOException e){
            System.out.println("Erro a escrever para ficheiro");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Metodo que le de um ficheiro objeto as pessoas existentes no programa
     */
    public  static void leFicheiroPessoas() {

        File f = new File("Pessoas.obj");
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);

            listaPessoas = (ArrayList<User>) ois.readObject();
            ois.close();
        }catch (FileNotFoundException e){
            System.out.println("Ficheiro Pessoas ainda nao existe");
            listaPessoas = new ArrayList<>();
        } catch(IOException e){
            System.out.println("Erro a escrever para ficheiro");
        }catch (ClassNotFoundException e) {
        e.printStackTrace();
    }

    }

    /**
     * Metodo que le de um ficheiro objeto as eleicoes existentes no programa
     */
    public  static void leFicheiroEleicoes(){

        File f = new File("Eleicoes.obj");
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);

            listaEleicoes= (ArrayList<Eleicao>) ois.readObject();
            ois.close();
        }catch (FileNotFoundException e){
            System.out.println("Ficheiro Eleicoes ainda nao existe");
            listaEleicoes = new ArrayList<>();
        } catch(IOException e){
            System.out.println("Erro a escrever para ficheiro");
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Metodo que le de um ficheiro objeto os departamentos existentes no programa
     */
    public  static void leFicheiroDepartamentos(){

        File f = new File("Departamentos.obj");
        try {
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);

            listaDepartamentos = (ArrayList<Departamento>) ois.readObject();
            ois.close();
        }catch (FileNotFoundException e){
            System.out.println("Ficheiro Departamentos ainda nao existe");
            listaDepartamentos = new ArrayList<>();
        } catch(IOException e){
            System.out.println("Erro a escrever para ficheiro");
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String [] args ) throws IOException, ClassNotFoundException {

        boolean failed = true;
        System.getProperties().put("java.security.policy","policy.all");
        System.setSecurityManager(new SecurityManager());
        RMI_S_I server = new RMIServer();
        System.getProperties().put("java.security.policy","policy.all");
        System.setSecurityManager(new SecurityManager());

        leFicheiroPessoas();
        leFicheiroDepartamentos();
        leFicheiroEleicoes();
        lerFicheiroClients();

        try {
            LocateRegistry.createRegistry(1099).rebind("Server",server);
            System.out.println("RMI Server ready...!");
        } catch (RemoteException exception){
            System.out.println("O server principal já se encontra ligado");

            while(failed){
                try {
                    Thread.sleep(5000);
                    System.out.println("A tentar de novo");
                    LocateRegistry.createRegistry(1099).rebind("Server",server);
                    System.out.println("Server Secundario is Ready");
                    failed = false;
                    leFicheiroPessoas();
                    leFicheiroEleicoes();
                    leFicheiroDepartamentos();
                    lerFicheiroClients();

                } catch (InterruptedException | RemoteException exception2) {
                    System.out.println("Server principal em funcionamento");
                }
            }
        }
    }

}
