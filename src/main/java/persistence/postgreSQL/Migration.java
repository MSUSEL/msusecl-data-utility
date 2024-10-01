package persistence.postgreSQL;

import businessObjects.cve.NvdMirrorMetaData;
import exceptions.DataAccessException;
import persistence.IDataSource;
import service.CredentialService;
import service.INvdMirrorService;
import service.MirrorService;
import service.NvdMirrorManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;

import static common.Constants.*;

public class Migration {
    private final Connection conn;
    private final NvdMirrorManager manager;
    private final INvdMirrorService mirrorService;


    public Migration(IDataSource<Connection> dataSource, NvdMirrorManager manager, INvdMirrorService mirrorService) {
        this.conn = dataSource.getConnection();
        this.manager = manager;
        this.mirrorService = mirrorService;
    }

    public void migrate() {
        executeScript(MIGRATION_SCRIPT_PATH, "sql");
        executeScript(PG_STORED_PROCEDURES_PATH, "plpgsql");

        // Update Data
        hydrate();
    }

    private void executeScript(String filepath, String scriptType) {
        String line;
        StringBuilder query = new StringBuilder();

        String lineEnd = determineLineEnd(scriptType);

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            while((line = reader.readLine()) != null) {
                query.append(line).append("\n");
                if (line.endsWith(lineEnd)) {
                    executeQuery(query.toString());
                    query.setLength(0);
                }
            }
        } catch (IOException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String determineLineEnd(String scriptType) {
        String lineEnd;
        if (scriptType.equals("sql")) {
            lineEnd = ";";
        } else if (scriptType.equals("plpgsql")) {
            lineEnd = "$$;";
        } else {
            throw new DataAccessException("Incorrect database script type");
        }
        return lineEnd;
    }

    private void executeQuery(String query) throws DataAccessException {
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            int rowsAffected = statement.executeUpdate();
            System.out.printf("Query: %s\n", query);
            System.out.printf("Rows Affected: %s\n\n", rowsAffected);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void hydrate() {
        NvdMirrorMetaData metadata = mirrorService.handleGetCurrentMetaData();
        if (metadata.getTimestamp() == null) {
            manager.handleBuildMirror();
        } else {
            manager.handleUpdateNvdMirror(metadata.getTimestamp(), Instant.now().toString());
        }
    }
}
