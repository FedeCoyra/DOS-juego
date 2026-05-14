package Persistencia;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GestorPersistencia {

    private static final String CARPETA = "datos";
    private static final String ARCHIVO_RANKING = CARPETA + File.separator + "ranking.dat";

    public GestorPersistencia() {
        File carpeta = new File(CARPETA);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
    }

    public void guardarPartida(PartidaGuardada partida, String nombreArchivo) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(CARPETA + File.separator + nombreArchivo))) {
            out.writeObject(partida);
        }
    }

    public PartidaGuardada cargarPartida(String nombreArchivo) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(CARPETA + File.separator + nombreArchivo))) {
            return (PartidaGuardada) in.readObject();
        }
    }

    @SuppressWarnings("unchecked")
    public List<RegistroRanking> cargarRanking() throws IOException, ClassNotFoundException {
        File archivo = new File(ARCHIVO_RANKING);
        if (!archivo.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(archivo))) {
            return (List<RegistroRanking>) in.readObject();
        }
    }

    public void guardarRanking(List<RegistroRanking> ranking) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ARCHIVO_RANKING))) {
            out.writeObject(ranking);
        }
    }

    public void registrarVictoria(String nombreJugador) throws IOException, ClassNotFoundException {
        List<RegistroRanking> ranking = cargarRanking();

        RegistroRanking encontrado = null;
        for (RegistroRanking r : ranking) {
            if (r.getNombreJugador().equalsIgnoreCase(nombreJugador)) {
                encontrado = r;
                break;
            }
        }

        if (encontrado == null) {
            ranking.add(new RegistroRanking(nombreJugador, 1));
        } else {
            encontrado.sumarVictoria();
        }

        ranking.sort(Comparator.comparingInt(RegistroRanking::getVictorias).reversed()
                .thenComparing(RegistroRanking::getNombreJugador));

        if (ranking.size() > 5) {
            ranking = new ArrayList<>(ranking.subList(0, 5));
        }

        guardarRanking(ranking);
    }
}