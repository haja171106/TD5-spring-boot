package restaurant;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private Connection connection;

    public DBConnection(String url, String user, String password)  {
        try {
            connection = DriverManager.getConnection(url, user, password);
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}
