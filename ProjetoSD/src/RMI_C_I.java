import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI_C_I extends Remote {

    void notification(String message, int priority) throws RemoteException;
}
