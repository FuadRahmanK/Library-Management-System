import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;

class Book implements Serializable {
    String name;
    String author;
    double price;
    int copies;
    String publication;

    public Book(String name, String author, double price, int copies, String publication) {
        this.name = name;
        this.author = author;
        this.price = price;
        this.copies = copies;
        this.publication = publication;
    }

    public void displayDetails() {
        System.out.println("Book: " + name);
        System.out.println("Author: " + author);
        System.out.println("Price: $" + price);
        System.out.println("Copies available: " + copies);
        System.out.println("Publication: " + publication);
    }

    @Override
    public String toString() {
        return name + "," + author + "," + price + "," + copies + "," + publication;
    }
}

class Bookstore {
    static Scanner input = new Scanner(System.in);
    ArrayList<Book> books = new ArrayList<>();
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/PMS";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root@123";

    public Bookstore() {
        establishConnection();
        createTableIfNotExists();
        loadFromDatabase();
    }

    private void establishConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            System.out.println("Connected to the database");
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement()) {
            String createTableQuery = "CREATE TABLE IF NOT EXISTS books (" +
                    "name VARCHAR(255) NOT NULL," +
                    "author VARCHAR(255) NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "copies INT NOT NULL," +
                    "publication VARCHAR(255) NOT NULL)";
            statement.executeUpdate(createTableQuery);
            System.out.println("Table created or already exists.");
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    public void saveToDatabase() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO books (name, author, price, copies, publication) VALUES (?, ?, ?, ?, ?)")) {
            for (Book book : books) {
                statement.setString(1, book.name);
                statement.setString(2, book.author);
                statement.setDouble(3, book.price);
                statement.setInt(4, book.copies);
                statement.setString(5, book.publication);
                statement.executeUpdate();
            }
            System.out.println("Data saved to database.");
        } catch (SQLException e) {
            System.out.println("Error saving data to database: " + e.getMessage());
        }
    }

    public void loadFromDatabase() {
    	books.clear();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM books")) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String author = resultSet.getString("author");
                double price = resultSet.getDouble("price");
                int copies = resultSet.getInt("copies");
                String publication = resultSet.getString("publication");
                Book book = new Book(name, author, price, copies, publication);
                books.add(book);
            }
            System.out.println("Data loaded from database.");
        } catch (SQLException e) {
            System.out.println("Error loading data from database: " + e.getMessage());
        }
    }

    public void addBook(String name, String author, double price, int copies, String publication) {
        Book newBook = new Book(name, author, price, copies, publication);
        books.add(newBook);
        System.out.println("Book added successfully!");

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO books (name, author, price, copies, publication) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, newBook.name);
            statement.setString(2, newBook.author);
            statement.setDouble(3, newBook.price);
            statement.setInt(4, newBook.copies);
            statement.setString(5, newBook.publication);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding book to database: " + e.getMessage());
        }
    }

    public void updateBookCopies(int updCopies , String updName) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            PreparedStatement statement = connection.prepareStatement("UPDATE books SET copies = ? WHERE name = ?")) {
              statement.setInt(1, updCopies);
              statement.setString(2, updName);
              int rowsAffected = statement.executeUpdate();
              if (rowsAffected > 0) {
                  System.out.println("Book updated successfully!");
                  loadFromDatabase();
              } else {
                  System.out.println("Book not found.");
              }
        } catch (SQLException e)  {
            System.out.println("Error updating book to database: "+ e.getMessage());
        }
    }
    
    public void deleteBookByName(String bookName) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM books WHERE name = ?")) {
            statement.setString(1, bookName);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Book deleted successfully!");
                loadFromDatabase();
            } else {
                System.out.println("Book not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting book from database: " + e.getMessage());
        }
    }

    public String getAllBooksDetails() {
        StringBuilder details = new StringBuilder();
        if (books.isEmpty()) {
            details.append("No books available in the store.");
        } else {
            for (Book book : books) {
                details.append("--------------\n");
                details.append("Book: ").append(book.name).append("\n");
                details.append("Author: ").append(book.author).append("\n");
                details.append("Price: $").append(book.price).append("\n");
                details.append("Copies available: ").append(book.copies).append("\n");
                details.append("Publication: ").append(book.publication).append("\n");
            }
        }
        return details.toString();
    }
}

public class BookstoreManagementGUI {
    public static void main(String[] args) {
        Bookstore bookstore = new Bookstore();
        JFrame frame = new JFrame("Bookstore Management");
        frame.setSize(700, 500);
        frame.setLayout(new FlowLayout());

        JButton addButton = new JButton("Add Book");
        JButton deleteButton = new JButton("Delete Book");
        JButton updateButton = new JButton("Update Book");
        JButton displayButton = new JButton("Display All Books");
        JTextArea outputArea = new JTextArea(25, 55);
        outputArea.setEditable(false);

        addButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Enter book name:");
            String author = JOptionPane.showInputDialog("Enter author name:");
            double price = Double.parseDouble(JOptionPane.showInputDialog("Enter price:"));
            int copies = Integer.parseInt(JOptionPane.showInputDialog("Enter number of copies:"));
            String publication = JOptionPane.showInputDialog("Enter publication:");
            bookstore.addBook(name, author, price, copies, publication);
            outputArea.setText(name+" added successfully!");
        });

        deleteButton.addActionListener(e -> {
            String bookName = JOptionPane.showInputDialog("Enter book name to delete:");
            bookstore.deleteBookByName(bookName);
            outputArea.setText(bookName+" deleted successfully!");
        });
        
        updateButton.addActionListener(e -> {
            String updName = JOptionPane.showInputDialog("Enter book name to update:");
            int updCopies = Integer.parseInt(JOptionPane.showInputDialog("Enter remaining no. of copies:"));
            bookstore.updateBookCopies(updCopies, updName);
            outputArea.setText(updName+" updated successfully!");
        });

	      displayButton.addActionListener(e -> {
	          bookstore.loadFromDatabase();
    	      outputArea.setText(bookstore.getAllBooksDetails());
	      });

        frame.add(addButton);
        frame.add(deleteButton);
        frame.add(updateButton);
        frame.add(displayButton);
        frame.add(new JScrollPane(outputArea));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
