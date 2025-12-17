package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories()
    {
        List<Category> categoriesList = new ArrayList<>();
        String sqlQuery = "SELECT * FROM categories";

        try (Connection conn = getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery);
            ResultSet rs = preparedStatement.executeQuery()) {

            while(rs.next()) {
                Category current = mapRow(rs);
                categoriesList.add(current);
            }
        } catch (SQLException e) {
            String error = "DB Error retrieving categories";
            throw new RuntimeException(error);
        }

        return categoriesList;
    }

    @Override
    public Category getById(int categoryId)
    {
        String sqlQuery = "SELECT * FROM categories WHERE category_id = ?";

        Category found = null;

        try (Connection conn = getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, categoryId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if(rs.next()) {
                    found = mapRow(rs);
                }
            }
        } catch (SQLException e) {
            String error = "DB Error retrieving category id:" + categoryId;
            throw new RuntimeException(error);
        }
        return found;
    }

    @Override
    public Category create(Category category)
    {
        String sqlQuery = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());

            int rowsAffected = preparedStatement.executeUpdate();

            if(rowsAffected > 0) {
                try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    if(rs.next()) {
                        int newCategoryId = rs.getInt(1);
                        category.setCategoryId(newCategoryId);
                    }
                }
            } else {
                String error = "Category inset failed, no rows affected";
                throw new RuntimeException(error);
            }
        } catch (SQLException e) {
            String error1 = "DB Error creating category" + category.getName() + e.getMessage();
            throw new RuntimeException(error1);
        }
        return category;
    }

    @Override
    public void update(int categoryId, Category category)
    {
        String sqlQuery = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";

        try(Connection conn = getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());
            preparedStatement.setInt(3, categoryId);

            int rowsAffected = preparedStatement.executeUpdate();

            if(rowsAffected == 0) {
                String warning = String.format("No category found with ID: %d", categoryId);
                System.out.println(warning);
            }
        } catch (SQLException e) {
            String error = "DB Error while updating category ID: " + categoryId;
            throw new RuntimeException(error);
        }
    }

    @Override
    public void delete(int categoryId)
    {
        String sqlQuery = "DELETE FROM categories WHERE category_id = ?";

        try(Connection conn = getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sqlQuery)) {
            preparedStatement.setInt(1, categoryId);

            int rowsAffected = preparedStatement.executeUpdate();

            if(rowsAffected == 0) {
                String warning = String.format("No category found with ID: %d", categoryId);
                System.out.println(warning);
            }
        } catch (SQLException e) {
            String error = "DB Error while deleting category ID: " + categoryId;
            throw new RuntimeException(error);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
