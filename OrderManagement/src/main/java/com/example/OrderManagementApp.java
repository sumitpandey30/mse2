
package com.example;

import java.sql.*;
import java.util.Scanner;

public class OrderManagementApp {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n===== ORDER MANAGEMENT SYSTEM =====");
            System.out.println("1. Insert Orders");
            System.out.println("2. Display Orders by Status");
            System.out.println("3. Increase Order Quantity");
            System.out.println("4. Update Order Status");
            System.out.println("5. Calculate Total Value and Delete Cancelled Order");
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    insertOrders();
                    break;

                case "2":
                    displayOrdersByStatus();
                    break;

                case "3":
                    increaseQuantity();
                    break;

                case "4":
                    updateOrderStatus();
                    break;

                case "5":
                    calculateTotalAndDelete();
                    break;

                case "0":
                    System.out.println("Exiting Order Management System.");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }

    // Q1(a): Insert three orders
    public static void insertOrders() {

        String sql = "INSERT INTO Orders VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            Object[][] orders = {
                    {101, "Rahul", "Laptop", 3, 55000.0, "Pending"},
                    {102, "Aman", "Keyboard", 5, 1500.0, "Shipped"},
                    {103, "Rohit", "Mouse", 4, 800.0, "Cancelled"}
            };

            for (Object[] o : orders) {
                try {
                    ps.setInt(1, (int) o[0]);
                    ps.setString(2, (String) o[1]);
                    ps.setString(3, (String) o[2]);
                    ps.setInt(4, (int) o[3]);
                    ps.setDouble(5, (double) o[4]);
                    ps.setString(6, (String) o[5]);

                    ps.executeUpdate();
                    System.out.println("Order " + o[0] + " inserted successfully.");

                } catch (SQLException e) {
                    if ("23000".equals(e.getSQLState())) {
                        System.out.println("Duplicate Order ID: " + o[0]);
                    } else {
                        throw e;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Q1(b): Display orders by status and quantity
    public static void displayOrdersByStatus() {

        System.out.print("Enter order status: ");
        String status = sc.nextLine();

        String sql = "SELECT * FROM Orders WHERE status = ? " +
                "AND quantity > 2 ORDER BY unit_price DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            ResultSet rs = ps.executeQuery();

            boolean found = false;

            System.out.println("\nMatching Orders:");

            while (rs.next()) {
                found = true;

                System.out.println(
                        "ID: " + rs.getInt("order_id") +
                                ", Customer: " + rs.getString("customer_name") +
                                ", Product: " + rs.getString("product_name") +
                                ", Quantity: " + rs.getInt("quantity") +
                                ", Unit Price: " + rs.getDouble("unit_price") +
                                ", Status: " + rs.getString("status")
                );
            }

            if (!found) {
                System.out.println("No matching orders found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Q1(c): Increase order quantity
    public static void increaseQuantity() {

        System.out.print("Enter Order ID: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter additional quantity: ");
        int quantity = Integer.parseInt(sc.nextLine());

        String sql = "UPDATE Orders SET quantity = quantity + ? WHERE order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Order quantity updated successfully.");
            } else {
                System.out.println("Order does not exist.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Q1(d): Update order status
    public static void updateOrderStatus() {

        System.out.print("Enter Order ID to update: ");
        int id = Integer.parseInt(sc.nextLine());

        System.out.print("Enter new status (Pending/Shipped/Delivered): ");
        String newStatus = sc.nextLine();

        if (!newStatus.equalsIgnoreCase("Pending") &&
                !newStatus.equalsIgnoreCase("Shipped") &&
                !newStatus.equalsIgnoreCase("Delivered")) {

            System.out.println("Invalid status!");
            return;
        }

        String checkSql = "SELECT * FROM Orders WHERE order_id = ?";
        String updateSql = "UPDATE Orders SET status = ? WHERE order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement checkPs = con.prepareStatement(checkSql)) {

            checkPs.setInt(1, id);

            ResultSet rs = checkPs.executeQuery();

            if (!rs.next()) {
                System.out.println("Order does not exist.");
                return;
            }

            try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                updatePs.setString(1, newStatus);
                updatePs.setInt(2, id);
                updatePs.executeUpdate();
            }

            System.out.println("\nOrder status updated successfully!");

            try (PreparedStatement displayPs = con.prepareStatement(checkSql)) {
                displayPs.setInt(1, id);

                ResultSet updatedRs = displayPs.executeQuery();

                if (updatedRs.next()) {
                    System.out.println("\nUpdated Order Details:");
                    System.out.println("Order ID: " + updatedRs.getInt("order_id"));
                    System.out.println("Customer: " + updatedRs.getString("customer_name"));
                    System.out.println("Product: " + updatedRs.getString("product_name"));
                    System.out.println("Quantity: " + updatedRs.getInt("quantity"));
                    System.out.println("Unit Price: " + updatedRs.getDouble("unit_price"));
                    System.out.println("Status: " + updatedRs.getString("status"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Q1(e): Calculate total order value and delete cancelled order
    public static void calculateTotalAndDelete() {

        String totalSql = "SELECT SUM(quantity * unit_price) AS total_value FROM Orders";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(totalSql)) {

            if (rs.next()) {
                System.out.println("\nTotal Order Value: " +
                        rs.getDouble("total_value"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.print("\nEnter Order ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());

        String deleteSql =
                "DELETE FROM Orders WHERE order_id = ? AND status = 'Cancelled'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(deleteSql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Cancelled order deleted successfully.");
            } else {
                System.out.println("Order cannot be deleted. It does not exist or is not Cancelled.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
