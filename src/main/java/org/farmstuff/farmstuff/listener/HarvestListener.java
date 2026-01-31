package org.farmstuff.farmstuff.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Random;
import org.bukkit.event.Listener;


public class HarvestListener implements Listener {

    public enum HoeType {
        WOOD(Material.WOODEN_HOE, 1),
        STONE(Material.STONE_HOE, 1),
        IRON(Material.IRON_HOE, 2),
        GOLD(Material.GOLDEN_HOE, 2),
        DIAMOND(Material.DIAMOND_HOE, 3),
        NETHERITE(Material.NETHERITE_HOE, 3);

        private final Material material;
        private final int dropMultiplier;

        HoeType(Material material, int dropMultiplier){
            this.material = material;
            this.dropMultiplier = dropMultiplier;
        }

        public static HoeType fromMaterial(Material mat){
            for (HoeType type : values()) {
                if (type.material == mat) return type;
            }
            return null;
        }

        public int getDropMultiplier(){
            return dropMultiplier;
        }

    }

    public enum CropType {
        WHEAT(Material.WHEAT, Material.WHEAT_SEEDS, Material.WHEAT),
        POTATOES(Material.POTATOES, Material.POTATO, Material.POTATO),
        CARROTS(Material.CARROTS, Material.CARROT, Material.CARROT),
        BEETROOTS(Material.BEETROOTS, Material.BEETROOT_SEEDS, Material.BEETROOT),
        NETHER_WART(Material.NETHER_WART, Material.NETHER_WART, Material.NETHER_WART),
        COCOA(Material.COCOA, Material.COCOA_BEANS, Material.COCOA_BEANS);

        private final Material blockMaterial;
        private final Material seedItem;
        private final Material cropItem;

        CropType(Material blockMaterial, Material seedItem, Material cropItem) {
            this.blockMaterial = blockMaterial;
            this.seedItem = seedItem;
            this.cropItem = cropItem;
        }

        public static CropType fromCrop(Material crop){
            for(CropType type : values()) {
                if (type.blockMaterial == crop) return type;
            }
            return null;
        }

        public Material getCropItem(){
            return cropItem;
        }

        public Material getSeedItem(){
            return seedItem;
        }

        public boolean hasSeparateCropItem(){
            return cropItem != seedItem;
        }
    }

    public int calculateFortuneMultiplier(int fortuneLevel){

        Random random = new Random();
        int ranNum = random.nextInt(100);

        switch (fortuneLevel) {
            case 1:
                if (ranNum < 66) return 1; //66% for 1x
                else return 2; // 33% for 2x

            case 2:
                if (ranNum < 50) return 1; //50% for 1x
                else if (ranNum < 75) return 2; // 25% for 2x
                else return 3; // 25% for 3x

            case 3:
                if (ranNum < 40) return 1; // 40% 1x
                else if (ranNum < 60) return 2; // 20%
                else if (ranNum < 80) return 3; // 20%
                else return 4; // 20%

            default:
                return 1;
        }
    }

    private void triggerRareEvent(Block block, Player player) {
        Random random = new Random();
        int eventChance = random.nextInt(1000); // 0.1% base chance

        if (eventChance < 1) {
            // 0.1% chance for explosion :3
            block.getWorld().createExplosion(block.getLocation(), 2.0f, false, false);
            block.getWorld().playSound(block.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }
    }


    @EventHandler
    public void onRightClickCrop(PlayerInteractEvent e){
        // check if it's right click
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        BlockData data = block.getBlockData();
        if (!(data instanceof Ageable)) return;
        Ageable crop = (Ageable) data;

        // skip because not fully grown
        if (crop.getAge() < crop.getMaximumAge()) return;

        // checks for player item in hand
        ItemStack inHand = e.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = inHand.getItemMeta();

        // applies fortune enchant effect
        int fortuneLevel = 0;
        int fortuneMultiplier = 1;
        if (meta != null && meta.hasEnchant(Enchantment.FORTUNE)){
            fortuneLevel = meta.getEnchantLevel(Enchantment.FORTUNE);
            fortuneMultiplier = calculateFortuneMultiplier(fortuneLevel);
        }

        // applies durability
        if (inHand.getType().getMaxDurability() > 0) {
            org.bukkit.inventory.meta.Damageable damage = (org.bukkit.inventory.meta.Damageable) inHand.getItemMeta();
            if (damage != null) {
                int unbreakingLevel = 0;
                if (meta != null && meta.hasEnchant(Enchantment.UNBREAKING)){
                    unbreakingLevel = meta.getEnchantLevel(Enchantment.UNBREAKING);
                }

                // calculate chance to not consume durability
                Random random = new Random();

                // unbreaking logic 100/level+1
                int chance = random.nextInt(100 + unbreakingLevel * 100);

                if (chance < 100) {
                    int currentDamage = damage.getDamage();
                    damage.setDamage(currentDamage + 1);

                    // check if the hoe should break
                    if (damage.getDamage() >= inHand.getType().getMaxDurability()) {
                        e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        block.getWorld().playSound(block.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    } else {
                        inHand.setItemMeta(damage);
                    }
                }
            }
        }

        HoeType hoeType = HoeType.fromMaterial(inHand.getType());
        if (hoeType == null) return;

        CropType cropType = CropType.fromCrop(block.getType());
        crop.setAge(0);
        block.setBlockData(crop);

        int baseAmount = hoeType.getDropMultiplier();
        int finalAmount = baseAmount * fortuneMultiplier;

        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(cropType.getCropItem(), finalAmount));
        if(cropType.hasSeparateCropItem()) {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(cropType.getSeedItem()));
        }

        triggerRareEvent(block, e.getPlayer());
        e.setCancelled(true);


    }
}
