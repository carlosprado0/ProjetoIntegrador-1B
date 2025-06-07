import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    public static void main(String[] args) {

        // Define a URL do banco de dados (ajuste conforme seu host, porta e nome do banco)
        String url = "jdbc:mysql://localhost:3306/seu_banco";

        // Nome de usuário do banco de dados
        String usuario = "root";

        // Senha do banco de dados
        String senha = "";

        try {
            // Carrega o driver JDBC do MySQL (necessário em versões mais antigas do Java)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Estabelece a conexão com o banco de dados usando a URL, usuário e senha
            Connection conn = DriverManager.getConnection(url, usuario, senha);

            // Verifica se a conexão foi bem-sucedida
            if (conn != null) {

                // Imprime no console se a conexão estiver OK
                System.out.println("Conexão bem-sucedida!");

                // Encerra a conexão (boa prática para liberar recursos)
                conn.close();
            }

            // Captura erro se o driver JDBC não for encontrado
        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC não encontrado!");
            e.printStackTrace();

            // Captura erro de SQL (por exemplo: usuário errado, banco não encontrado etc)
        } catch (SQLException e) {
            System.out.println("Erro de conexão com o banco de dados.");
            e.printStackTrace();
        }
    }
}

