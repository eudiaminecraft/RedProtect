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

package br.net.fabiozumbi12.RedProtect.Bukkit.listeners;

import br.net.fabiozumbi12.RedProtect.Bukkit.RPUtil;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPConfig;
import br.net.fabiozumbi12.RedProtect.Bukkit.config.RPLang;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RPMine19 implements Listener {

    public RPMine19() {
        RedProtect.get().logger.debug("Loaded RPMine19...");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Block b = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();

        Location l = null;

        if (b != null) {
            l = b.getLocation();
            RedProtect.get().logger.debug("RPPlayerListener - Is PlayerInteractEvent event. The block is " + b.getType().name());
        } else {
            l = p.getLocation();
        }

        if (RedProtect.get().tpWait.contains(p.getName())) {
            RedProtect.get().tpWait.remove(p.getName());
            RPLang.sendMessage(p, "cmdmanager.region.tpcancelled");
        }

        if (itemInHand != null && (event.getAction().name().equals("RIGHT_CLICK_BLOCK") || b == null)) {
            Material hand = itemInHand.getType();
            Region r = RedProtect.get().rm.getTopRegion(l);
            if (r != null && hand.equals(Material.CHORUS_FRUIT) && !r.canTeleport(p)) {
                RPLang.sendMessage(p, "playerlistener.region.cantuse");
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) {
            return;
        }

        final Player p = e.getPlayer();
        Location lfrom = e.getFrom();
        Location lto = e.getTo();

        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT)) {
            final Region rfrom = RedProtect.get().rm.getTopRegion(lfrom);
            final Region rto = RedProtect.get().rm.getTopRegion(lto);

            if (rfrom != null && !rfrom.canTeleport(p)) {
                RPLang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
            }
            if (rto != null && !rto.canTeleport(p)) {
                RPLang.sendMessage(p, "playerlistener.region.cantuse");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onShootBow(EntityShootBowEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity();
        Entity proj = e.getProjectile();
        List<String> Pots = RPConfig.getStringList("server-protection.deny-potions");

        if (proj != null && (proj instanceof TippedArrow)) {
            TippedArrow arr = (TippedArrow) proj;
            if (Pots.contains(arr.getBasePotionData().getType().name())) {
                RPLang.sendMessage(p, "playerlistener.denypotion");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLingerPotion(LingeringPotionSplashEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity().getShooter();
        Entity ent = e.getEntity();

        RedProtect.get().logger.debug("Is LingeringPotionSplashEvent event.");

        Region r = RedProtect.get().rm.getTopRegion(ent.getLocation());
        if (r != null && !r.allowEffects(p)) {
            RPLang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
            return;
        }

        if (RPUtil.denyPotion(e.getEntity().getItem())) {
            e.setCancelled(true);
            if (e.getEntity().getShooter() instanceof Player) {
                RPLang.sendMessage((Player) e.getEntity().getShooter(), RPLang.get("playerlistener.denypotion"));
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (e.getItem() == null) {
            return;
        }

        Player p = e.getPlayer();
        //deny potion
        if (p == null) {
            return;
        }

        Region r = RedProtect.get().rm.getTopRegion(p.getLocation());
        if (r != null && e.getItem().getType().equals(Material.CHORUS_FRUIT) && !r.canTeleport(p)) {
            RPLang.sendMessage(p, "playerlistener.region.cantuse");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChangeBlock(EntityChangeBlockEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            Block b = e.getBlock();
            Region r = RedProtect.get().rm.getTopRegion(b.getLocation());
            if (r != null && !r.canBuild(p)) {
                RPLang.sendMessage(p, "blocklistener.region.cantbreak");
                e.setCancelled(true);
            }
        }
    }
}
