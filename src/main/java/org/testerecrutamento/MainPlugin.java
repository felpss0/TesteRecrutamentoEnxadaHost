package org.testerecrutamento;

import org.testerecrutamento.WindCharge;
import org.testerecrutamento.Home;
import org.bukkit.plugin.java.JavaPlugin;

public final class MainPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        //Carrega a configuração padrão do plugin
        saveDefaultConfig();

        // Inicializa os módulos WindCharge e Home
        new WindCharge(this);
        new Home(this);

        getLogger().info("MainPlugin Ativado");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        getLogger().info("MainPlugin Desativado");
    }
}
