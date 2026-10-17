import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    // sesuaikan dengan pengaturan MySQL di komputermu (XAMPP/Laragon)
    private static final String URL = "jdbc:mysql://localhost:3306/db_people";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection connection;

    // constructor: membuka koneksi ke database
    public Database() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal terhubung ke database: " + e.getMessage(), e);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
