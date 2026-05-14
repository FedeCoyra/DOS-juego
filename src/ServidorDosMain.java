import Modelo.JuegoDos;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.Util;
import ar.edu.unlu.rmimvc.servidor.Servidor;

import javax.swing.JOptionPane;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ServidorDosMain {

    public static void main(String[] args) {
        ArrayList<String> ips = Util.getIpDisponibles();

        String ip = (String) JOptionPane.showInputDialog(
                null,
                "Seleccione la IP en la que escuchará peticiones el servidor",
                "IP del servidor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                ips.toArray(),
                null
        );

        if (ip == null) return;

        String port = (String) JOptionPane.showInputDialog(
                null,
                "Seleccione el puerto en el que escuchará peticiones el servidor",
                "Puerto del servidor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                5000
        );

        if (port == null) return;

        try {
            JuegoDos modelo = new JuegoDos();
            Servidor servidor = new Servidor(ip, Integer.parseInt(port));
            servidor.iniciar(modelo);
            System.out.println("Servidor DOS levantado en " + ip + ":" + port);
        } catch (RemoteException | RMIMVCException e) {
            e.printStackTrace();
        }
    }
}