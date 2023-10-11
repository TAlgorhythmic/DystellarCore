package net.zylesh.dystellarcore.core;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public enum Suffix {
    NONE("", 10),
    L("&b【&c&lL&b】", 11),
    LOL("&b【&c&lLOL&b】", 12),
    GG("&b【&6&lGG&b】", 13),
    GF("&b【&6&lGF&b】", 14),
    LMAO("&b【&1&lLMAO&b】", 15),
    LMFAO("&b【&1&lLMFAO&b】", 16),
    HEART("&8[&c&l<3&8]", 17),
    USA("&1░&cU&fS&cA&1░", 20),
    COL("&eC&1O&cL", 21),
    VEN("&f☆&eV&1E&cN&f☆", 22),
    ARG("&e✺&bA&fR&bG&e✺", 23),
    URU("&e✺&9U&fR&9U&e✺", 24),
    ESP("&cE&eS&cP", 25),
    PA("&1☆P&cA☆", 26),
    UK("&cU&1K", 29),
    IT("&aI&fT&cA", 30),
    MEX("&2M&fE&cX", 31),
    BR("&aB&eR&1A", 32),
    PE("&cP&fE&cR", 33),
    BOL("&cB&eO&2L", 34),
    CUB("&cC&1U&fB", 35),
    CHL("&1C&fH&cL", 38),
    FR("&1F&fR&cA", 39),
    GER("&0G&cE&eR", 40),
    POL("&fP&cO&fL", 41),
    IR("&2I&fR&eE", 42),
    ROM("&1R&eO&cM", 43),
    TR("&f☾&cTR&f☆", 44);


    Suffix(String suffix, int slot) {
        this.suffix = suffix;
        this.slot = slot;
    }

    private ItemStack icon;
    private final int slot;
    private final String suffix;

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public String toString() {
        return suffix;
    }

    public static void initialize() {
        ArrayList<String> lore = new ArrayList<>();
        lore.add(" ");
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to select.");
        ItemStack NONE_ITEM = new ItemStack(Material.COAL_BLOCK);
        ItemMeta none = NONE_ITEM.getItemMeta();
        none.setDisplayName(NONE.suffix);
        none.setLore(lore);
        NONE_ITEM.setItemMeta(none);
        NONE.setIcon(NONE_ITEM);
        ItemStack L_ITEM = new ItemStack(Material.WOOL);
        ItemMeta l = L_ITEM.getItemMeta();
        l.setDisplayName(L.suffix);
        l.setLore(lore);
        L_ITEM.setItemMeta(l);
        L.setIcon(L_ITEM);
        ItemStack LOL_ITEM = new ItemStack(Material.WOOL);
        ItemMeta lol = LOL_ITEM.getItemMeta();
        lol.setDisplayName(LOL.suffix);
        lol.setLore(lore);
        LOL_ITEM.setItemMeta(lol);
        LOL.setIcon(LOL_ITEM);
        ItemStack GG_ITEM = new ItemStack(Material.WOOL);
        ItemMeta gg = GG_ITEM.getItemMeta();
        gg.setDisplayName(GG.suffix);
        gg.setLore(lore);
        GG_ITEM.setItemMeta(gg);
        GG.setIcon(GG_ITEM);
        ItemStack GF_ITEM = new ItemStack(Material.WOOL);
        ItemMeta gf = GF_ITEM.getItemMeta();
        gf.setDisplayName(GF.suffix);
        gf.setLore(lore);
        GF_ITEM.setItemMeta(gf);
        GF.setIcon(GF_ITEM);
        ItemStack LMAO_ITEM = new ItemStack(Material.WOOL);
        ItemMeta lmao = LMAO_ITEM.getItemMeta();
        lmao.setDisplayName(LMAO.suffix);
        lmao.setLore(lore);
        LMAO_ITEM.setItemMeta(lmao);
        LMAO.setIcon(LMAO_ITEM);
        ItemStack LMFAO_ITEM = new ItemStack(Material.WOOL);
        ItemMeta lmfao = LMFAO_ITEM.getItemMeta();
        lmfao.setDisplayName(LMFAO.suffix);
        lmfao.setLore(lore);
        LMFAO_ITEM.setItemMeta(lmfao);
        LMFAO.setIcon(LMFAO_ITEM);
        ItemStack HEART_ITEM = new ItemStack(Material.WOOL);
        ItemMeta heart = HEART_ITEM.getItemMeta();
        heart.setDisplayName(HEART.suffix);
        heart.setLore(lore);
        HEART_ITEM.setItemMeta(heart);
        HEART.setIcon(HEART_ITEM);
        ItemStack USA_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta usa = USA_ITEM.getItemMeta();
        usa.setDisplayName(USA.suffix);
        usa.setLore(lore);
        USA_ITEM.setItemMeta(usa);
        USA.setIcon(USA_ITEM);
        ItemStack COL_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta col = COL_ITEM.getItemMeta();
        col.setDisplayName(COL.suffix);
        col.setLore(lore);
        COL_ITEM.setItemMeta(col);
        COL.setIcon(COL_ITEM);
        ItemStack VEN_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta ven = VEN_ITEM.getItemMeta();
        ven.setDisplayName(VEN.suffix);
        ven.setLore(lore);
        VEN_ITEM.setItemMeta(ven);
        VEN.setIcon(VEN_ITEM);
        ItemStack ARG_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta arg = ARG_ITEM.getItemMeta();
        arg.setDisplayName(ARG.suffix);
        arg.setLore(lore);
        ARG_ITEM.setItemMeta(arg);
        ARG.setIcon(ARG_ITEM);
        ItemStack URU_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta uru = URU_ITEM.getItemMeta();
        uru.setDisplayName(URU.suffix);
        uru.setLore(lore);
        URU_ITEM.setItemMeta(uru);
        URU.setIcon(URU_ITEM);
        ItemStack ESP_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta esp = ESP_ITEM.getItemMeta();
        esp.setDisplayName(ESP.suffix);
        esp.setLore(lore);
        ESP_ITEM.setItemMeta(esp);
        ESP.setIcon(ESP_ITEM);
        ItemStack PA_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta pa = PA_ITEM.getItemMeta();
        pa.setDisplayName(PA.suffix);
        pa.setLore(lore);
        PA_ITEM.setItemMeta(pa);
        PA.setIcon(PA_ITEM);
        ItemStack UK_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta uk = UK_ITEM.getItemMeta();
        uk.setDisplayName(UK.suffix);
        uk.setLore(lore);
        UK_ITEM.setItemMeta(uk);
        UK.setIcon(UK_ITEM);
        ItemStack IT_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta it = IT_ITEM.getItemMeta();
        it.setDisplayName(IT.suffix);
        it.setLore(lore);
        IT_ITEM.setItemMeta(it);
        IT.setIcon(IT_ITEM);
        ItemStack MEX_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta mex = MEX_ITEM.getItemMeta();
        mex.setDisplayName(MEX.suffix);
        mex.setLore(lore);
        MEX_ITEM.setItemMeta(mex);
        MEX.setIcon(MEX_ITEM);
        ItemStack BR_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta br = BR_ITEM.getItemMeta();
        br.setDisplayName(BR.suffix);
        br.setLore(lore);
        BR_ITEM.setItemMeta(br);
        BR.setIcon(BR_ITEM);
        ItemStack PE_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta pe = PE_ITEM.getItemMeta();
        pe.setDisplayName(PE.suffix);
        pe.setLore(lore);
        PE_ITEM.setItemMeta(pe);
        PE.setIcon(PE_ITEM);
        ItemStack BOL_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta bol = BOL_ITEM.getItemMeta();
        bol.setDisplayName(BOL.suffix);
        bol.setLore(lore);
        BOL_ITEM.setItemMeta(bol);
        BOL.setIcon(BOL_ITEM);
        ItemStack CUB_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta cub = CUB_ITEM.getItemMeta();
        cub.setDisplayName(CUB.suffix);
        cub.setLore(lore);
        CUB_ITEM.setItemMeta(cub);
        CUB.setIcon(CUB_ITEM);
        ItemStack CHL_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta chl = CHL_ITEM.getItemMeta();
        chl.setDisplayName(CHL.suffix);
        chl.setLore(lore);
        CHL_ITEM.setItemMeta(chl);
        CHL.setIcon(CHL_ITEM);
        ItemStack FR_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta fr = FR_ITEM.getItemMeta();
        fr.setDisplayName(FR.suffix);
        fr.setLore(lore);
        FR_ITEM.setItemMeta(fr);
        FR.setIcon(FR_ITEM);
        ItemStack GER_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta ger = GER_ITEM.getItemMeta();
        ger.setDisplayName(GER.suffix);
        ger.setLore(lore);
        GER_ITEM.setItemMeta(ger);
        GER.setIcon(GER_ITEM);
        ItemStack POL_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta pol = POL_ITEM.getItemMeta();
        pol.setDisplayName(POL.suffix);
        pol.setLore(lore);
        POL_ITEM.setItemMeta(pol);
        POL.setIcon(POL_ITEM);
        ItemStack IR_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta ir = IR_ITEM.getItemMeta();
        ir.setDisplayName(IR.suffix);
        ir.setLore(lore);
        IR_ITEM.setItemMeta(ir);
        IR.setIcon(IR_ITEM);
        ItemStack ROM_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta rom = ROM_ITEM.getItemMeta();
        rom.setDisplayName(ROM.suffix);
        rom.setLore(lore);
        ROM_ITEM.setItemMeta(rom);
        ROM.setIcon(ROM_ITEM);
        ItemStack TR_ITEM = new ItemStack(Material.EMERALD);
        ItemMeta tr = TR_ITEM.getItemMeta();
        tr.setDisplayName(TR.suffix);
        tr.setLore(lore);
        TR_ITEM.setItemMeta(tr);
        TR.setIcon(TR_ITEM);
    }
}
