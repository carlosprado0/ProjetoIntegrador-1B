import java.sql.*;
import java.util.Scanner;

public class Main {

    // --- CONFIGURAÇÃO DA CONEXÃO COM O BANCO DE DADOS ---
    private static final String URL = "jdbc:mysql://localhost:3306/vacinasdb";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection conn = null;

        try {
            // ESTABELECER A CONEXÃO
            // O DriverManager tenta encontrar um driver JDBC adequado para a URL fornecida
            // e usa as credenciais para estabelecer uma sessão com o banco de dados.
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão realizada com sucesso!");

            // --- LOOP PRINCIPAL DA APLICAÇÃO ---
            // O while serve para exibir um menu em execução até que o usuário escolha sair.
            while (true) {
                System.out.println("\n--- MENU ---");
                System.out.println("1. Cadastrar paciente");
                System.out.println("2. Cadastrar vacina");
                System.out.println("3. Registrar vacinação");
                System.out.println("4. Verificar vacinas liberadas em minha região");
                System.out.println("5. Vacinados por região");
                System.out.println("6. Sair");
                System.out.print("Escolha: ");
                int opcao = scanner.nextInt();
                scanner.nextLine();

                switch (opcao) {
                    case 1 -> cadastrarPaciente(conn, scanner);
                    case 2 -> cadastrarVacina(conn, scanner);
                    case 3 -> registrarVacinacao(conn, scanner);
                    case 4 -> vacinasLiberadasRegiao(conn, scanner);
                    case 5 -> listaDeVacinados(conn, scanner);
                    case 6 -> {
                        System.out.println("Encerrando...");
                        conn.close();
                        return;
                    }
                    default -> System.out.println("Opção inválida.");
                }
            }

        } catch (SQLException e) {
            // Captura qualquer exceção relacionada a operações SQL,
            // como falha na conexão, erro de sintaxe na query, etc.
            System.out.println("Erro de SQL ou de conexão: " + e.getMessage());
        }
    }

    /**
     * Cadastra um novo paciente no banco de dados.
     */
    private static void cadastrarPaciente(Connection conn, Scanner scanner) throws SQLException {
        //  (coleta de dados do usuário)
        System.out.println("Nome completo:");
        String nome = scanner.nextLine();
        System.out.println("Data de nascimento (YYYY-MM-DD):");
        String dataNascimento = scanner.nextLine();
        System.out.println("Endereço completo:");
        String endereco = scanner.nextLine();
        System.out.println("Telefone:");
        String telefone = scanner.nextLine();
        System.out.println("ID da região de moradia (Ex: 11 para SP, 21 para RJ):");
        int idRegiao = scanner.nextInt();
        scanner.nextLine();

        String sql = "INSERT INTO pacientes (nome_completo, data_nascimento, endereco_completo, telefone, id_regiao_moradia) VALUES (?, ?, ?, ?, ?)";

        // Ele trata os dados de entrada como literais,
        // e não como parte do comando SQL
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, nome);
        stmt.setDate(2, Date.valueOf(dataNascimento));
        stmt.setString(3, endereco);
        stmt.setString(4, telefone);
        stmt.setInt(5, idRegiao);

        // O metodo executeUpdate() é usado para comandos que modificam dados (INSERT, UPDATE, DELETE).
        // Ele retorna o número de linhas afetadas pela operação.
        int rows = stmt.executeUpdate();
        System.out.println(rows + " paciente(s) cadastrado(s).");
    }

    /**
     * Cadastra um novo tipo de vacina no sistema.
     */
    private static void cadastrarVacina(Connection conn, Scanner scanner) throws SQLException {
        // (coleta de dados e inserção similar ao cadastrarPaciente)
        System.out.println("Nome da vacina:");
        String nome = scanner.nextLine();
        System.out.println("Número de doses no esquema:");
        int doses = scanner.nextInt();
        scanner.nextLine();

        String sql = "INSERT INTO vacinas (nome_vacina, numero_doses_esquema) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, nome);
        stmt.setInt(2, doses);

        int rows = stmt.executeUpdate();
        System.out.println(rows + " vacina(s) cadastrada(s).");
    }

    /**
     * Registra a aplicação de uma vacina em um paciente.
     */
    private static void registrarVacinacao(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("ID do paciente:");
        int idPaciente = scanner.nextInt();
        System.out.println("ID da vacina:");
        int idVacina = scanner.nextInt();
        scanner.nextLine();
        System.out.println("Data de aplicação (YYYY-MM-DD):");
        String data = scanner.nextLine();
        System.out.println("Dose aplicada (Ex: 1ª Dose, Reforço):");
        String dose = scanner.nextLine();
        System.out.println("Local de aplicação:");
        String local = scanner.nextLine();

        String sql = "INSERT INTO registrosvacinacao (id_paciente, id_vacina, data_aplicacao, dose_aplicada, local_aplicacao) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idPaciente);
        stmt.setInt(2, idVacina);
        stmt.setDate(3, Date.valueOf(data));
        stmt.setString(4, dose);
        stmt.setString(5, local);

        int rows = stmt.executeUpdate();
        System.out.println(rows + " registro(s) de vacinação inserido(s).");
    }

    /**
     * Consulta as vacinas disponíveis em uma determinada região.
     */
    private static void vacinasLiberadasRegiao(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Digite o ID da sua região: ");
        int idRegiao = scanner.nextInt();
        scanner.nextLine();

        // Esta requisição une as 3 tabelas para encontrar os nomes das vacinas (sem repetição)
        // que foram aplicadas em pacientes de uma determinada região.
        String sql = """
            SELECT v.nome_vacina
            FROM Vacinas v
            JOIN VacinasLiberadasRegiao vlr ON v.id_vacina = vlr.id_vacina
            WHERE vlr.id_regiao = ?
            """;

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idRegiao);

        ResultSet rs = stmt.executeQuery();

        boolean encontrou = false;
        System.out.println("\n--- Vacinas disponíveis na região " + idRegiao + " ---");
        while (rs.next()) {
            encontrou = true;
            System.out.println("- " + rs.getString("nome_vacina"));
        }

        if (!encontrou) {
            System.out.println("Nenhum registro de vacina encontrado para esta região.");
        }
    }

    /**
     * Lista todas as pessoas vacinadas em uma região específica.
     */
    private static void listaDeVacinados(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Digite o ID da região a ser consultada: ");
        int regiao = scanner.nextInt();
        scanner.nextLine();

        // LÓGICA DA CONSULTA:
        //  Começa pela tabela de RegistrosVacinacao.
        //  Usa JOIN para buscar o nome da vacina na tabela `Vacinas`.
        //  Usa JOIN para buscar os dados do paciente na tabela `Pacientes`.
        //  Filtra os resultados para pacientes que pertencem à região desejada.
        //  O DISTINCT evita que a mesma combinação de pessoa/vacina apareça múltiplas vezes
        //    caso ela tenha tomado mais de uma dose da mesma vacina.
        String sql = """
        SELECT DISTINCT p.nome_completo, v.nome_vacina
        FROM RegistrosVacinacao rv
        JOIN Vacinas v ON rv.id_vacina = v.id_vacina
        JOIN Pacientes p ON rv.id_paciente = p.id_paciente
        WHERE p.id_regiao_moradia = ?
        ORDER BY p.nome_completo
        """;

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, regiao);

        ResultSet rs = stmt.executeQuery();

        boolean encontrou = false;
        System.out.println("\n--- Pessoas vacinadas na região " + regiao + " ---");
        while (rs.next()) {
            encontrou = true;
            System.out.println("- " + rs.getString("nome_completo") + " tomou " + rs.getString("nome_vacina"));
        }

        if (!encontrou) {
            System.out.println("Nenhum registro de vacinação encontrado para esta região.");
        }
    }
}