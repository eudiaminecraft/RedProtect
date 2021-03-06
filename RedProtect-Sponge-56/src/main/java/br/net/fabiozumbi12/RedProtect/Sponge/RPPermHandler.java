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

package br.net.fabiozumbi12.RedProtect.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RPPermHandler {

    public boolean hasPermOrBypass(Player p, String perm) {
        return p.hasPermission(perm) || p.hasPermission(perm + ".bypass");
    }

    public boolean hasPerm(CommandSource p, String perm) {
        return p != null && (p.hasPermission(perm) || p.hasPermission("redprotect.admin"));
    }

    public boolean hasPerm(Player p, String perm) {
        return p != null && (p.hasPermission(perm) || p.hasPermission("redprotect.admin"));
    }

    public boolean hasPerm(User p, String perm) {
        return p != null && (p.hasPermission(perm) || p.hasPermission("redprotect.admin"));
    }

    public boolean hasRegionPermMember(Player p, String s, Region poly) {
        return regionPermMember(p, s, poly);
    }

    public boolean hasRegionPermAdmin(Player p, String s, Region poly) {
        return regionPermAdmin(p, s, poly);
    }

    public boolean hasRegionPermAdmin(CommandSource sender, String s, Region poly) {
        return !(sender instanceof Player) || regionPermAdmin((Player) sender, s, poly);
    }

    public boolean hasRegionPermLeader(Player p, String s, Region poly) {
        return regionPermLeader(p, s, poly);
    }

    public boolean hasRegionPermLeader(CommandSource sender, String s, Region poly) {
        return !(sender instanceof Player) || regionPermLeader((Player) sender, s, poly);
    }

    public boolean hasGenPerm(Player p, String s) {
        return GeneralPermHandler(p, s);
    }

    public int getPlayerBlockLimit(User p) {
        return LimitHandler(p);
    }

    public int getPlayerClaimLimit(User p) {
        return ClaimLimitHandler(p);
    }

    private int LimitHandler(User p) {
        int limit = RedProtect.get().cfgs.root().region_settings.limit_amount;
        List<Integer> limits = new ArrayList<>();
        if (limit > 0) {
            if (!p.hasPermission("redprotect.limit.blocks.unlimited")) {
                for (String perm : RedProtect.get().cfgs.root().permissions_limits.blocks) {
                    RedProtect.get().logger.debug(LogLevel.DEFAULT, "Perm: " + perm);
                    if (p.hasPermission(perm)) {
                        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Has block perm: " + perm);
                        String pStr = perm.replaceAll("[^-?0-9]+", "");
                        if (!pStr.isEmpty()) {
                            limits.add(Integer.parseInt(pStr));
                        }
                    }
                }
            } else {
                return -1;
            }
        }
        if (limits.size() > 0) {
            limit = Collections.max(limits);
        }
        return limit;
    }

    private int ClaimLimitHandler(User p) {
        int limit = RedProtect.get().cfgs.root().region_settings.claim.amount_per_player;
        List<Integer> limits = new ArrayList<>();
        if (limit > 0) {
            if (!p.hasPermission("redprotect.limit.claim.unlimited")) {
                for (String perm : RedProtect.get().cfgs.root().permissions_limits.claims) {
                    RedProtect.get().logger.debug(LogLevel.DEFAULT, "Perm: " + perm);
                    if (p.hasPermission(perm)) {
                        RedProtect.get().logger.debug(LogLevel.DEFAULT, "Has claim perm: " + perm);
                        String pStr = perm.replaceAll("[^-?0-9]+", "");
                        if (!pStr.isEmpty()) {
                            limits.add(Integer.parseInt(pStr));
                        }
                    }
                }
            } else {
                return -1;
            }
        }
        if (limits.size() > 0) {
            limit = Collections.max(limits);
        }
        return limit;
    }

    private boolean regionPermLeader(Player p, String s, Region poly) {
        String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || ((this.hasPerm(p, "redprotect.user") || this.hasPerm(p, userperm)) && poly.isLeader(p));
    }

    private boolean regionPermAdmin(Player p, String s, Region poly) {
        String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || ((this.hasPerm(p, "redprotect.user") || this.hasPerm(p, userperm)) && (poly.isLeader(p) || poly.isAdmin(p)));
    }

    private boolean regionPermMember(Player p, String s, Region poly) {
        String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        if (poly == null) {
            return this.hasPerm(p, adminperm) || this.hasPerm(p, userperm);
        }
        return this.hasPerm(p, adminperm) || ((this.hasPerm(p, "redprotect.user") || this.hasPerm(p, userperm)) && (poly.isLeader(p) || poly.isAdmin(p) || poly.isMember(p)));
    }

    private boolean GeneralPermHandler(Player p, String s) {
        String adminperm = "redprotect.admin." + s;
        String userperm = "redprotect.own." + s;
        String normalperm = "redprotect." + s;
        return this.hasPerm(p, s) || this.hasPerm(p, adminperm) || this.hasPerm(p, userperm) || this.hasPerm(p, normalperm);
    }

    public boolean hasUserPerm(Player player, String string) {
        return this.hasPerm(player, "redprotect.user") || this.GeneralPermHandler(player, string);
    }

    public boolean hasFlagPerm(Player player, String string) {
        return this.hasPerm(player, "redprotect.flag.all") || this.hasUserPerm(player, string);
    }

    public boolean hasAdminFlagPerm(Player player, String string) {
        return this.hasPerm(player, "redprotect.flag.special") || this.GeneralPermHandler(player, string);
    }
}
