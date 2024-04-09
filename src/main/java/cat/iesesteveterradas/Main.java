package cat.iesesteveterradas;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.basex.api.client.ClientSession;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Exercici 2

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

        public static void main(String[] args) {
            File inputDir = new File("./data/input");
            File outputDir = new File("./data/output");

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
        
            File[] queryFiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xquery"));

            // Connection to server
            try (ClientSession session = new ClientSession("127.0.0.1", 1984, "admin", "admin")) {
                logger.info("Connected to BaseX server.");
                session.execute(new Open("bioinformatics"));
                
                for (File queryFile : queryFiles) {
                    runXQuery(session, queryFile, outputDir);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        public static void runXQuery(ClientSession session, File queryFile, File outputDir) {
            String queryName = queryFile.getName().replaceFirst("[.][^.]+$", "");
            File outputFile = new File(outputDir, queryName + ".xml");

            try {
                String query = readFile(queryFile);
                String result = session.execute(new XQuery(query));
                
                try (FileWriter writer = new FileWriter(outputFile)) {
                    writer.write(result);
                    System.out.println("Query " + queryName + " saved at " + outputFile.getAbsolutePath());
                } catch (IOException e) {
                    logger.error("Error upon saving " + queryName + ": " + e.getMessage());
                }
            } catch (IOException e) {
                logger.error("Error upon executing " + queryName + ": " + e.getMessage());
            }
        }

        public static String readFile(File file) {
            StringBuilder content = new StringBuilder();
            try (Scanner sc = new Scanner(file)) {
                while (sc.hasNextLine()) {
                    content.append(sc.nextLine()).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content.toString();
        }
}
