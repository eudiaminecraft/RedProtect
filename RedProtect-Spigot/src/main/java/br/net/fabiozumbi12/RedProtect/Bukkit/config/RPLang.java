/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.RedProtect.Bukkit.config;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

public class RPLang {

    public static final Properties Lang = new Properties();
    static final Properties BaseLang = new Properties();
    private static final HashMap<Player, String> DelayedMessage = new HashMap<>();
    //static List<String> langString = new ArrayList<String>();
    private static String pathLang;
    private static String resLang;

    public static SortedSet<String> helpStrings() {
        SortedSet<String> values = new TreeSet<>();
        for (Object help : Lang.keySet()) {
            if (help.toString().startsWith("cmdmanager.help.")) {
                values.add(help.toString().replace("cmdmanager.help.", ""));
            }
        }
        return values;
    }

    public static void init() {
        resLang = "lang" + RPConfig.getString("language") + ".properties";
        pathLang = RedProtect.get().getDataFolder() + File.separator + resLang;

        File lang = new File(pathLang);
        if (!lang.exists()) {
            if (RedProtect.get().getResource("assets/redprotect/" + resLang) == null) {
                resLang = "langEN-US.properties";
                pathLang = RedProtect.get().getDataFolder() + File.separator + resLang;
            }
            RPUtil.saveResource("/assets/redprotect/" + resLang, null, new File(RedProtect.get().getDataFolder(), resLang));
            RedProtect.get().logger.info("Created language file: " + pathLang);
        }

        loadLang();
        loadBaseLang();
        RedProtect.get().logger.info("Language file loaded - Using: " + RPConfig.getString("language"));
    }

    static void loadBaseLang() {
        BaseLang.clear();
        try {
            InputStream fileInput = RedProtect.get().getResource("assets/redprotect/langEN-US.properties");
            Reader reader = new InputStreamReader(fileInput, "UTF-8");
            BaseLang.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateLang();
    }

    static void loadLang() {
        Lang.clear();
        try {
            FileInputStream fileInput = new FileInputStream(pathLang);
            Reader reader = new InputStreamReader(fileInput, "UTF-8");
            Lang.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Lang.get("_lang.version") != null) {
            int langv = Integer.parseInt(Lang.get("_lang.version").toString().replace(".", ""));
            int rpv = Integer.parseInt(RedProtect.get().pdf.getVersion().replace(".", ""));
            if (RedProtect.get().pdf.getVersion().length() > Lang.get("_lang.version").toString().length()) {
                langv = Integer.parseInt(Lang.get("_lang.version").toString().replace(".", "") + 0);
            }
            if (langv < rpv || langv == 0) {
                RedProtect.get().logger.warning("Your lang file is outdated. Probally need strings updates!");
                RedProtect.get().logger.warning("Lang file version: " + Lang.get("_lang.version"));
                Lang.put("_lang.version", RedProtect.get().pdf.getVersion());
            }
        }
    }

    static void updateLang() {
        for (Entry<Object, Object> linha : BaseLang.entrySet()) {
            if (!Lang.containsKey(linha.getKey())) {
                Lang.put(linha.getKey(), linha.getValue());
            }
        }
        if (!Lang.containsKey("_lang.version")) {
            Lang.put("_lang.version", RedProtect.get().pdf.getVersion());
        }
        try {
            Lang.store(new OutputStreamWriter(new FileOutputStream(pathLang), "UTF-8"), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        String FMsg;

        if (Lang.get(key) == null) {
            FMsg = "&c&oMissing language string for " + ChatColor.GOLD + key;
        } else {
            FMsg = Lang.get(key).toString();
        }

        FMsg = ChatColor.translateAlternateColorCodes('&', FMsg);

        return FMsg;
    }

    public static void sendMessage(final Player p, String key) {
        if (DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)) {
            return;
        }

        if (Lang.get(key) == null) {
            p.sendMessage(get("_redprotect.prefix") + " " + ChatColor.translateAlternateColorCodes('&', key));
        } else if (get(key).equalsIgnoreCase("")) {
            return;
        } else {
            p.sendMessage(get("_redprotect.prefix") + " " + get(key));
        }

        DelayedMessage.put(p, key);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
            if (DelayedMessage.containsKey(p)) {
                DelayedMessage.remove(p);
            }
        }, 20);
    }

    public static void sendMessage(CommandSender sender, String key) {
        if (sender instanceof Player && DelayedMessage.containsKey(sender) && DelayedMessage.get(sender).equals(key)) {
            return;
        }

        if (Lang.get(key) == null) {
            sender.sendMessage(get("_redprotect.prefix") + " " + ChatColor.translateAlternateColorCodes('&', key));
        } else if (get(key).equalsIgnoreCase("")) {
            return;
        } else {
            sender.sendMessage(get("_redprotect.prefix") + " " + get(key));
        }

        if (sender instanceof Player) {
            final Player p = (Player) sender;
            DelayedMessage.put(p, key);
            Bukkit.getScheduler().scheduleSyncDelayedTask(RedProtect.get(), () -> {
                if (DelayedMessage.containsKey(p)) {
                    DelayedMessage.remove(p);
                }
            }, 20);
        }

    }

    public static String translBool(String bool) {
        return get("region." + bool);
    }

    public static String translBool(Boolean bool) {
        return get("region." + bool.toString());
    }

    public static boolean containsValue(String value) {
        return Lang.containsValue(value);
    }
}
