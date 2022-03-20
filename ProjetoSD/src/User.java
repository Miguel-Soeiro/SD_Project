import java.io.Serializable;
import java.util.GregorianCalendar;

public class User implements Serializable{

    private final String nome;
    private String tipo;
    private String password;
    private Departamento departamento;
    private final int numero_cc;
    private GregorianCalendar validade_CC;
    private int contacto;
    private String morada;


    public User(String nome, String tipo, String password, Departamento departamento, int numero_cc, GregorianCalendar numero_cc_val,int contacto, String morada) {
        this.nome = nome;
        this.tipo = tipo;
        this.password = password;
        this.departamento = departamento;
        this.numero_cc = numero_cc;
        this.validade_CC =numero_cc_val;
        this.contacto = contacto;
        this.morada = morada;
    }

    public User(String nome, int numero_cc) {
        this.nome = nome;
        this.numero_cc= numero_cc;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public String getPassword() {
        return password;
    }

    public int getnumero_cc() {
        return numero_cc;
    }

    @Override
    public String toString() {
        return "Nome: " + this.nome + "; Tipo: " + this.tipo + ";numero_cc: " + this.numero_cc + "; Departamento: " + this.departamento;
    }
}