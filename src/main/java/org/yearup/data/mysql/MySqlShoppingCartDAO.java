package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDAO extends MySqlDaoBase implements ShoppingCartDao {

    public MySqlShoppingCartDAO(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();
        String sqlQuery = "SELECT * FROM shopping_cart WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, userId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while(rs.next()) {
                    int productId = rs.getInt("product_id");
                    int quantity = rs.getInt("quantity");

                    Product product = getProductById(productId, conn);

                    if(product != null) {
                        ShoppingCartItem item = new ShoppingCartItem();
                        item.setProduct(product);
                        item.setQuantity(quantity);
                        cart.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cart;
    }

    @Override
    public void addItem(int userId, int productId) {
        String checkSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)";
        String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try(Connection conn = getConnection()) {
            try (PreparedStatement checkStatement = conn.prepareStatement(checkSql)) {
                checkStatement.setInt(1, userId);
                checkStatement.setInt(2, productId);

                try (ResultSet rs = checkStatement.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement updateStatement = conn.prepareStatement(updateSql)) {
                            updateStatement.setInt(1, userId);
                            updateStatement.setInt(2, productId);
                            updateStatement.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement insertStatement = conn.prepareStatement(insertSql)) {
                            insertStatement.setInt(1, userId);
                            insertStatement.setInt(2, productId);
                            insertStatement.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            String error = "DB Error adding product to cart";
            throw new RuntimeException(error);
        }
    }

    @Override
    public void updateQuantity(int userId, int productId, int quantity) {
        String sqlQuery = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection conn = getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3, productId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                String warning = String.format("Product %d not found in cart for user %d", productId, userId);
                System.out.println(warning);
            }
        } catch (SQLException e) {
            String error = "DB Error updating quantity";
            throw new RuntimeException(error);
        }
    }

    @Override
    public void clearCart(int userId) {
        String sqlQuery = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection conn = getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String error = "DB Error clearing cart for user: " + userId;
            throw new RuntimeException(error);
        }
    }

    private Product getProductById(int productId, Connection conn) throws SQLException {
        String sqlQuery = "SELECT * FROM products WHERE product_id = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, productId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("product_id");
                    String name = rs.getString("name");
                    BigDecimal price = rs.getBigDecimal("price");
                    int categoryId = rs.getInt("category_id");
                    String description = rs.getString("description");
                    String subCategory = rs.getString("subcategory");
                    int stock = rs.getInt("stock");
                    boolean isFeatured = rs.getBoolean("featured");
                    String imageUrl = rs.getString("image_url");

                    return new Product(id, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl);
                }
            }
        }
        return null;
    }
}
