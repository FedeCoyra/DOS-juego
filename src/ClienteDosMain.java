import Controlador.ControladorJuego;
import Vista.VistaConsola;
import Vista.VistaJuego;
import Vista.VistaSwing;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.Util;
import ar.edu.unlu.rmimvc.cliente.Cliente;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ClienteDosMain {

    public static void main(String[] args) {

        ArrayList<String> ips = Util.getIpDisponibles();

        String ipCliente = (String) JOptionPane.showInputDialog(
                null,
                "Seleccione la IP en la que escuchará peticiones el cliente",
                "IP del cliente",
                JOptionPane.QUESTION_MESSAGE,
                null,
                ips.toArray(),
                null
        );
        if (ipCliente == null) return;

        String portClienteStr = (String) JOptionPane.showInputDialog(
                null,
                "Seleccione el puerto en el que escuchará peticiones el cliente",
                "Puerto del cliente",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                5001
        );
        if (portClienteStr == null) return;

        String ipServidor = (String) JOptionPane.showInputDialog(
                null,
                "Ingrese la IP en la que corre el servidor",
                "IP del servidor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                "127.0.0.1"
        );
        if (ipServidor == null) return;

        String portServidorStr = (String) JOptionPane.showInputDialog(
                null,
                "Ingrese el puerto en el que corre el servidor",
                "Puerto del servidor",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                5000
        );
        if (portServidorStr == null) return;


        String[] opcionesVista = {"Gráfica", "Consola"};
        int vistaResp = JOptionPane.showOptionDialog(
                null,
                "Seleccione el tipo de vista para este cliente",
                "Tipo de vista",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcionesVista,
                opcionesVista[0]
        );
        if (vistaResp == JOptionPane.CLOSED_OPTION) return;
        boolean usarSwing = (vistaResp == 0);

        int puertoCliente  = Integer.parseInt(portClienteStr.trim());
        int puertoServidor = Integer.parseInt(portServidorStr.trim());

        VistaJuego vista;
        if (usarSwing) {
            VistaSwing vistaSwing = new VistaSwing();
            SwingUtilities.invokeLater(() -> vistaSwing.setVisible(true));
            vista = vistaSwing;
        } else {
            vista = new VistaConsola();
        }

        ControladorJuego controlador = new ControladorJuego(vista);
        controlador.setIndiceJugadorLocal(-1);

        Cliente cliente = new Cliente(ipCliente, puertoCliente, ipServidor, puertoServidor);

        try {
            cliente.iniciar(controlador);
            controlador.iniciar();
        } catch (RemoteException | RMIMVCException e) {
            e.printStackTrace();
            vista.mostrarMensaje("No se pudo conectar con el servidor: " + e.getMessage());
        }
    }
}