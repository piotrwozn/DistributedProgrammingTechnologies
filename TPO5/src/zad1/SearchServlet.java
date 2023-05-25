package zad1;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("q");

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Books WHERE title LIKE ? OR author LIKE ?")) {

            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");

            ResultSet rs = stmt.executeQuery();

            List<Book> books = new ArrayList<>();
            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("publication_year"),
                        rs.getInt("pages"),
                        rs.getString("publisher")
                );
                books.add(book);
            }

            req.setAttribute("books", books);
            req.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
