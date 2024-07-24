package org.testerecrutamento;



// Importações necessárias para o funcionamento do plugin
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Define a classe Home que implementa Listener e CommandExecutor para lidar com eventos e comandos
public class Home implements Listener, CommandExecutor {

    private final JavaPlugin plugin; // Referência ao plugin principal
    private long cooldown; // Tempo de espera entre usos do comando /home
    private boolean showParticles; // Se deve mostrar partículas ao teletransportar
    private Connection connection; // Conexão com o banco de dados

    // Construtor da classe Home, inicializa o plugin e configurações
    public Home(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfiguration(); // Carrega as configurações do plugin
        setupDatabase(); // Configura o banco de dados

        // Registra os comandos sethome e home
        if (plugin.getCommand("sethome") != null) {
            plugin.getCommand("sethome").setExecutor(this);
        }
        if (plugin.getCommand("home") != null) {
            plugin.getCommand("home").setExecutor(this);
        }

        // Registra a classe como ouvinte de eventos do Bukkit
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // Método para carregar as configurações do plugin
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();
        cooldown = config.getLong("home.cooldown"); // Configuração de cooldown
        showParticles = config.getBoolean("home.showParticles"); // Configuração para mostrar partículas
    }

    // Método para configurar o banco de dados SQLite
    private void setupDatabase() {
        try {
            // Conecta ao banco de dados SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:homes.db");
            // Cria a tabela homes se não existir
            PreparedStatement statement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS homes (" +
                            "player_uuid TEXT PRIMARY KEY, " +
                            "world TEXT, " +
                            "x DOUBLE, " +
                            "y DOUBLE, " +
                            "z DOUBLE, " +
                            "yaw FLOAT, " +
                            "pitch FLOAT)"
            );
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Exibe o erro no console
        }
    }

    // Método para definir a localização "home" do jogador
    private void setHome(Player player) {
        Location loc = player.getLocation(); // Obtém a localização atual do jogador
        try {
            // Insere ou atualiza a localização "home" do jogador no banco de dados
            PreparedStatement statement = connection.prepareStatement(
                    "REPLACE INTO homes (player_uuid, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
            statement.setString(1, player.getUniqueId().toString()); // UUID do jogador
            statement.setString(2, loc.getWorld().getName()); // Nome do mundo
            statement.setDouble(3, loc.getX()); // Coordenada X
            statement.setDouble(4, loc.getY()); // Coordenada Y
            statement.setDouble(5, loc.getZ()); // Coordenada Z
            statement.setFloat(6, loc.getYaw()); // Yaw (rotação horizontal)
            statement.setFloat(7, loc.getPitch()); // Pitch (rotação vertical)
            statement.executeUpdate();
            player.sendMessage("Home set!"); // Informa ao jogador que a localização foi definida
        } catch (SQLException e) {
            e.printStackTrace(); // Exibe o erro no console
        }
    }

    // Método para teletransportar o jogador para a sua localização "home"
    private void teleportHome(Player player) {
        try {
            // Consulta a localização "home" do jogador no banco de dados
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM homes WHERE player_uuid = ?"
            );
            statement.setString(1, player.getUniqueId().toString()); // UUID do jogador
            ResultSet resultSet = statement.executeQuery();

            // Se uma localização "home" foi encontrada, teletransporta o jogador
            if (resultSet.next()) {
                Location loc = new Location(
                        Bukkit.getWorld(resultSet.getString("world")), // Nome do mundo
                        resultSet.getDouble("x"), // Coordenada X
                        resultSet.getDouble("y"), // Coordenada Y
                        resultSet.getDouble("z"), // Coordenada Z
                        resultSet.getFloat("yaw"), // Yaw (rotação horizontal)
                        resultSet.getFloat("pitch") // Pitch (rotação vertical)
                );

                // Executa a tarefa de teleporte com atraso definido pelo cooldown
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(loc); // Teletransporta o jogador
                        if (showParticles) {
                            loc.getWorld().spawnParticle(Particle.PORTAL, loc, 10); // Mostra partículas se configurado
                        }
                        player.sendMessage("Teleported to home!"); // Informa ao jogador que foi teletransportado
                    }
                }.runTaskLater(plugin, cooldown * 2);
            } else {
                player.sendMessage("No home set!"); // Informa ao jogador que não há uma localização "home" definida
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Exibe o erro no console
        }
    }

    // Método chamado quando um comando é executado
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verifica se o comando foi executado por um jogador
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!"); // Informa que somente jogadores podem usar o comando
            return true;
        }

        Player player = (Player) sender;

        // Se o comando for "sethome", define a localização "home" do jogador
        if (command.getName().equalsIgnoreCase("sethome")) {
            setHome(player);
            return true;
        }

        // Se o comando for "home", teletransporta o jogador para sua localização "home"
        if (command.getName().equalsIgnoreCase("home")) {
            teleportHome(player);
            return true;
        }

        return false; // Retorna false se o comando não for reconhecido
    }
}

