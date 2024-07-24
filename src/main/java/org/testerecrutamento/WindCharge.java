package org.testerecrutamento;


// Importa classes do Bukkit necessárias para o funcionamento do plugin
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;


// Define a classe WindCharge que implementa Listener para escutar eventos do Bukkit
public class WindCharge implements Listener {

    private final JavaPlugin plugin; // Referência ao plugin principal
    private double explosionPower; // Força da explosão do projétil
    private boolean addParticles; // Se deve adicionar partículas ao impacto
    private double projectileSpeed; // Velocidade do projétil

    // Construtor da classe WindCharge, inicializa o plugin e configurações
    public WindCharge(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfiguration(); // Carrega as configurações do plugin
        Bukkit.getPluginManager().registerEvents(this, plugin); // Registra a classe como ouvinte de eventos
    }

    // Método para carregar as configurações do plugin
    private void loadConfiguration() {
        FileConfiguration config = plugin.getConfig();
        explosionPower = config.getDouble("windcharge.explosionPower"); // Configuração da força da explosão
        addParticles = config.getBoolean("windcharge.addParticles"); // Configuração para adicionar partículas
        projectileSpeed = config.getDouble("windcharge.projectileSpeed"); // Configuração da velocidade do projétil
    }

    // Método chamado quando um projétil é lançado
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity(); // Obtém o projétil lançado
        if (projectile.getShooter() instanceof Player) { // Verifica se o projétil foi lançado por um jogador
            projectile.setVelocity(projectile.getVelocity().multiply(projectileSpeed)); // Ajusta a velocidade do projétil
        }
    }

    // Método chamado quando um projétil atinge algo
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity(); // Obtém o projétil que atingiu algo
        if (projectile.getShooter() instanceof Player) { // Verifica se o projétil foi lançado por um jogador
            // Cria uma explosão na localização do projétil
            projectile.getWorld().createExplosion(projectile.getLocation(), (float) explosionPower, false, true);
            if (addParticles) { // Adiciona partículas se configurado
                projectile.getWorld().spawnParticle(Particle.CLOUD, projectile.getLocation(), 100);
            }
            projectile.remove(); // Remove o projétil após o impacto
        }
    }
}
