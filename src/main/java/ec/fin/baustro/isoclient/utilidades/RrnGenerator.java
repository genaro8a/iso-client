package ec.fin.baustro.isoclient.utilidades;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RrnGenerator {
    private static final String STATE_FILE_PATH = "rrnGeneratorState.bin";
    private static GeneratorState state;

    static {
        loadState();
    }

    public static synchronized String generateRrn() {
        LocalDateTime now = LocalDateTime.now();

        if (now.toLocalDate().isAfter(state.getLastDateTime().toLocalDate())) {
            state.setCount(0); // Reinicia el contador si estamos en un nuevo día
            state.setLastDateTime(now);
        }

        state.setCount(state.getCount() + 1);
        saveState();

        return now.format(DateTimeFormatter.ofPattern("yyMMddHH")) + String.format("%04d", state.getCount());
    }

    private static void loadState() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STATE_FILE_PATH))) {
            state = (GeneratorState) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("State file for RRN not found. Creating a new state.");
            state = new GeneratorState(0, LocalDateTime.now()); // Valores iniciales
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void saveState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STATE_FILE_PATH))) {
            oos.writeObject(state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


