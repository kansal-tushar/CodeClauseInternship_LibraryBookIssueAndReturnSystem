import java.sql.*;
import java.util.Properties;

public class LibrarySystem {

    private static Connection connection;
    private static final String PROPERTIES_PATH = "config.properties";

    public static void main(String[] args) {
        try {
            connection = initializeDatabase();
            menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection initializeDatabase() throws Exception {
        Properties prop = new Properties();
        prop.load(LibrarySystem.class.getClassLoader().getResourceAsStream(PROPERTIES_PATH));

        String dbURL = prop.getProperty("DB_URL");
        String dbUser = prop.getProperty("DB_USER");
        String dbPassword = prop.getProperty("DB_PASSWORD");
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);

        return conn;
    }

    private static void menu() throws SQLException {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        while (true) {
            System.out.println("1. Login as Admin");
            System.out.println("2. Login as User");
            System.out.println("3. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline
            switch (choice) {
                case 1:
                    adminOperations(scanner);
                    break;
                case 2:
                    userOperations(scanner);
                    break;
                case 3:
                    connection.close();
                    System.exit(0);
            }
        }
    }

    private static void adminOperations(java.util.Scanner scanner) throws SQLException {
        System.out.println("Enter password: ");
        String password = scanner.nextLine();
        if (!password.equals("admin@library")) {
            System.out.println("Wrong password!");
            return;
        }

        System.out.println("1. Add Book");
        System.out.println("2. Remove Book");
        System.out.println("3. Show All Books");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        switch (choice) {
            case 1:
                System.out.println("Enter Book Title: ");
                String title = scanner.nextLine();
                System.out.println("Enter Book Author: ");
                String author = scanner.nextLine();
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO books(title, author) VALUES(?, ?)");
                stmt.setString(1, title);
                stmt.setString(2, author);
                stmt.executeUpdate();
                System.out.println("Book added successfully!");
                break;
            case 2:
                System.out.println("Enter Book ID to Remove: ");
                int bookId = scanner.nextInt();
                stmt = connection.prepareStatement("DELETE FROM books WHERE id=?");
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
                System.out.println("Book removed successfully!");
                break;
            case 3:
                stmt = connection.prepareStatement("SELECT * FROM books");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id") + ", Title: " + rs.getString("title") + ", Author: " + rs.getString("author"));
                }
                break;
        }
    }

    private static void userOperations(java.util.Scanner scanner) throws SQLException {
        System.out.println("1. Issue Book");
        System.out.println("2. Return Book");
        System.out.println("3. Show All Books");
        int choice = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        switch (choice) {
            case 1:
                System.out.println("Enter Book ID to Issue: ");
                int bookId = scanner.nextInt();
                PreparedStatement checkStmt = connection.prepareStatement("SELECT is_issued FROM books WHERE id=?");
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();
                if(rs.next()) {
                    if(rs.getBoolean("is_issued")) {
                        System.out.println("Error: Book is already issued.");
                    } else {
                        PreparedStatement stmt = connection.prepareStatement("UPDATE books SET is_issued=true WHERE id=?");
                        stmt.setInt(1, bookId);
                        stmt.executeUpdate();
                        System.out.println("Book issued successfully!");
                    }
                } else {
                    System.out.println("Error: Book with the provided ID does not exist.");
                }
                break;
            case 2:
                System.out.println("Enter Book ID to Return: ");
                bookId = scanner.nextInt();
                PreparedStatement checkReturnStmt = connection.prepareStatement("SELECT is_issued FROM books WHERE id=?");
                checkReturnStmt.setInt(1, bookId);
                ResultSet rsReturn = checkReturnStmt.executeQuery();
                
                if (rsReturn.next()) {
                    if (rsReturn.getBoolean("is_issued")) {
                        PreparedStatement stmt = connection.prepareStatement("UPDATE books SET is_issued=false WHERE id=?");
                        stmt.setInt(1, bookId);
                        stmt.executeUpdate();
                        System.out.println("Book returned successfully!");
                    } else {
                        System.out.println("Error: Book was not issued or has already been returned.");
                    }
                } else {
                    System.out.println("Error: Book with the provided ID does not exist.");
                }
                break;
            case 3:
                PreparedStatement showStmt = connection.prepareStatement("SELECT * FROM books");
                ResultSet showRs = showStmt.executeQuery();
                while (showRs.next()) {
                    System.out.println("ID: " + showRs.getInt("id") + ", Title: " + showRs.getString("title") + ", Author: " + showRs.getString("author"));
                }
                break;

        }
    }

}
