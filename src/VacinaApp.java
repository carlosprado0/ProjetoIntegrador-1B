import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class VacinaApp extends JFrame {

    private final Connection conn;

    public VacinaApp(Connection conn) {
        this.conn = conn;
        setTitle("Sistema de Vacinação");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 1)); // Agora com 7 opções

        JButton btnPaciente = new JButton("Cadastrar Paciente");
        JButton btnVacina = new JButton("Cadastrar Vacina");
        JButton btnRegistro = new JButton("Registrar Vacinação");
        JButton btnConsultaVacinas = new JButton("Vacinas por Região");
        JButton btnVacinados = new JButton("Vacinados por Região");
        JButton btnPesquisarPaciente = new JButton("Pesquisar Paciente por Nome");
        JButton btnSair = new JButton("Sair");

        add(btnPaciente);
        add(btnVacina);
        add(btnRegistro);
        add(btnConsultaVacinas);
        add(btnVacinados);
        add(btnPesquisarPaciente);  // novo botão
        add(btnSair);



        btnPaciente.addActionListener(e -> cadastrarPaciente());
        btnVacina.addActionListener(e -> cadastrarVacina());
        btnRegistro.addActionListener(e -> registrarVacinacao());
        btnConsultaVacinas.addActionListener(e -> consultarVacinas());
        btnVacinados.addActionListener(e -> consultarVacinados());
        btnSair.addActionListener(e -> System.exit(0));
        btnPesquisarPaciente.addActionListener(e -> pesquisarPacientePorNome());

    }
    private void mostrarResultado(String titulo, String conteudo) {
        JTextArea areaTexto = new JTextArea(conteudo);
        areaTexto.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaTexto);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, titulo, JOptionPane.INFORMATION_MESSAGE);
    }


    private void cadastrarPaciente() {
        try {
            String nome = JOptionPane.showInputDialog("Nome:");
            if (nome == null || nome.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastro cancelado.");
                return;
            }

            String dataNasc = JOptionPane.showInputDialog("Data nascimento (YYYY-MM-DD):");
            if (dataNasc == null || dataNasc.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastro cancelado.");
                return;
            }

            String endereco = JOptionPane.showInputDialog("Endereço:");
            if (endereco == null || endereco.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastro cancelado.");
                return;
            }

            String telefone = JOptionPane.showInputDialog("Telefone:");
            if (telefone == null || telefone.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastro cancelado.");
                return;
            }

            String idRegiao = JOptionPane.showInputDialog("ID da região:");
            if (idRegiao == null || idRegiao.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cadastro cancelado.");
                return;
            }

            // Execução SQL
            String sql = "INSERT INTO pacientes (nome_completo, data_nascimento, endereco_completo, telefone, id_regiao_moradia) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nome);
            stmt.setDate(2, Date.valueOf(dataNasc));
            stmt.setString(3, endereco);
            stmt.setString(4, telefone);
            stmt.setInt(5, Integer.parseInt(idRegiao));

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Paciente cadastrado com sucesso!");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }


    private void cadastrarVacina() {
        try {
            String nome = JOptionPane.showInputDialog("Nome da vacina:");
            String doses = JOptionPane.showInputDialog("Número de doses:");

            String sql = "INSERT INTO vacinas (nome_vacina, numero_doses_esquema) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nome);
            stmt.setInt(2, Integer.parseInt(doses));

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Vacina cadastrada com sucesso!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    private void registrarVacinacao() {
        try {
            String idPaciente = JOptionPane.showInputDialog("ID do paciente:");
            String idVacina = JOptionPane.showInputDialog("ID da vacina:");
            String data = JOptionPane.showInputDialog("Data da aplicação (YYYY-MM-DD):");
            String dose = JOptionPane.showInputDialog("Dose aplicada:");
            String local = JOptionPane.showInputDialog("Local da aplicação:");

            String sql = "INSERT INTO registrosvacinacao (id_paciente, id_vacina, data_aplicacao, dose_aplicada, local_aplicacao) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(idPaciente));
            stmt.setInt(2, Integer.parseInt(idVacina));
            stmt.setDate(3, Date.valueOf(data));
            stmt.setString(4, dose);
            stmt.setString(5, local);

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Vacinação registrada com sucesso!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    private void consultarVacinas() {
        try {
            String id = JOptionPane.showInputDialog("ID da região:");
            String sql = """
            SELECT DISTINCT v.nome_vacina
            FROM vacinas v
            JOIN registrosvacinacao rv ON v.id_vacina = rv.id_vacina
            JOIN pacientes p ON rv.id_paciente = p.id_paciente
            WHERE p.id_regiao_moradia = ?
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(id));
            ResultSet rs = stmt.executeQuery();

            java.util.List<String> vacinas = new java.util.ArrayList<>();
            while (rs.next()) {
                vacinas.add(rs.getString("nome_vacina"));
            }

            if (vacinas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhuma vacina encontrada.");
            } else {
                String[] colunas = {"Vacinas Encontradas"};
                String[][] dados = new String[vacinas.size()][1];
                for (int i = 0; i < vacinas.size(); i++) {
                    dados[i][0] = vacinas.get(i);
                }

                JTable tabela = new JTable(dados, colunas);
                JScrollPane scrollPane = new JScrollPane(tabela);
                scrollPane.setPreferredSize(new Dimension(400, 200));

                JOptionPane.showMessageDialog(this, scrollPane, "Vacinas por Região", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }


    private void consultarVacinados() {
        try {
            String id = JOptionPane.showInputDialog("ID da região:");
            String sql = """
            SELECT DISTINCT p.nome_completo, v.nome_vacina
            FROM registrosvacinacao rv
            JOIN vacinas v ON rv.id_vacina = v.id_vacina
            JOIN pacientes p ON rv.id_paciente = p.id_paciente
            WHERE p.id_regiao_moradia = ?
            ORDER BY p.nome_completo
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(id));
            ResultSet rs = stmt.executeQuery();

            java.util.List<String[]> dados = new java.util.ArrayList<>();
            while (rs.next()) {
                dados.add(new String[]{
                        rs.getString("nome_completo"),
                        rs.getString("nome_vacina")
                });
            }

            if (dados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum vacinado encontrado.");
            } else {
                String[] colunas = {"Nome do Paciente", "Vacina Aplicada"};
                String[][] tabelaDados = dados.toArray(new String[0][]);

                JTable tabela = new JTable(tabelaDados, colunas);
                JScrollPane scrollPane = new JScrollPane(tabela);
                scrollPane.setPreferredSize(new Dimension(450, 250));

                JOptionPane.showMessageDialog(this, scrollPane, "Vacinados por Região", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }


    public static void main(String[] args) {
        try {
            String url = "jdbc:mysql://localhost:3306/vacinasdb";
            String user = "root";
            String senha = "263012";
            Connection conn = DriverManager.getConnection(url, user, senha);

            SwingUtilities.invokeLater(() -> {
                VacinaApp app = new VacinaApp(conn);
                app.setVisible(true);
            });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro de conexão com o banco de dados: " + e.getMessage());
        }
    }
    private void pesquisarPacientePorNome() {
        try {
            String nomeBusca = JOptionPane.showInputDialog("Digite o nome do paciente:");
            if (nomeBusca == null || nomeBusca.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pesquisa cancelada.");
                return;
            }

            String sql = """
            SELECT id_paciente AS ID, nome_completo AS Nome, 
                   data_nascimento AS Nascimento, 
                   endereco_completo AS Endereço, 
                   telefone AS Telefone, 
                   id_regiao_moradia AS Região
            FROM pacientes
            WHERE nome_completo LIKE ?
            ORDER BY nome_completo
        """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + nomeBusca + "%"); // busca parcial
            ResultSet rs = stmt.executeQuery();

            mostrarTabela(rs, "Resultado da Busca por Nome");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    private void mostrarTabela(ResultSet rs, String titulo) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int colunas = metaData.getColumnCount();

        String[] nomesColunas = new String[colunas];
        for (int i = 1; i <= colunas; i++) {
            nomesColunas[i - 1] = metaData.getColumnName(i);
        }

        java.util.List<String[]> linhas = new java.util.ArrayList<>();
        while (rs.next()) {
            String[] linha = new String[colunas];
            for (int i = 1; i <= colunas; i++) {
                linha[i - 1] = rs.getString(i);
            }
            linhas.add(linha);
        }

        if (linhas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum dado encontrado.");
            return;
        }

        String[][] dados = linhas.toArray(new String[0][]);
        JTable tabela = new JTable(dados, nomesColunas);
        JScrollPane scrollPane = new JScrollPane(tabela);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(this, scrollPane, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}
