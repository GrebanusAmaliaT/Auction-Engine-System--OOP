package src.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditService {
    private static AuditService instance;
    private final String FILE_PATH = "audit_log.csv";

    private AuditService() {}

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public void logAction(String actionName) {
        try {
            File file = new File(FILE_PATH);
            boolean fileIsNew = !file.exists();

            try (FileWriter fw = new FileWriter(FILE_PATH, true)) {
                if (fileIsNew) {
                    fw.write("nume_actiune,timestamp\n");
                }

                String timestamp = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                fw.write(actionName + "," + timestamp + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}