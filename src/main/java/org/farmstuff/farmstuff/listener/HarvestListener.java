package org.farmstuff.farmstuff.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

    public int calculateFortuneMultiplier(CropType cropType, int fortuneLevel){

        Random random = new Random();

        // base number of binomial attempts
        int attempts = 3 + fortuneLevel;

        // binomial distribution with p=0.57
        int bonusDrops = 0;
        for (int i = 0; i < attempts; i++){
            if (random.nextDouble() <0.57){
                bonusDrops++;
            }
        }

        // 2 drops minimum from carrots and potatoes
        if (cropType == CropType.CARROTS || cropType == CropType.POTATOES) {
            return 2 + bonusDrops;
        }

        if (cropType == CropType.WHEAT || cropType == CropType.BEETROOTS) {
            return 1 + bonusDrops;
        }
        return bonusDrops;
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

    private void applyHoeDurability(ItemStack hoe, ItemMeta meta, Block block, Player player) {
        if (hoe.getType().getMaxDurability() <= 0) return;

        org.bukkit.inventory.meta.Damageable damage = (org.bukkit.inventory.meta.Damageable) hoe.getItemMeta();
        if (damage == null) return;

        int unbreakingLevel = 0;
        if (meta != null && meta.hasEnchant(Enchantment.UNBREAKING)) {
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
            if (damage.getDamage() >= hoe.getType().getMaxDurability()) {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                block.getWorld().playSound(block.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            } else {
                hoe.setItemMeta(damage);
            }
        }
    }

    private int getFortuneLevel(ItemMeta meta) {
        if (meta != null && meta.hasEnchant(Enchantment.FORTUNE)) {
            return meta.getEnchantLevel(Enchantment.FORTUNE);
        }
        return 0;
    }

    private void dropCropItems(Block block, CropType cropType, HoeType hoeType, int fortuneLevel) {
        int seedDrops = calculateFortuneMultiplier(cropType, fortuneLevel);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(cropType.getSeedItem(), seedDrops));

        // drop crop item if separate. wheat item, beetroot item only affected by hoe tier
        if (cropType.hasSeparateCropItem()) {
            int cropAmount = hoeType.getDropMultiplier();
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(cropType.getCropItem(), cropAmount));
        }
    }

    private void resetCropAge(Block block, Ageable crop) {
        crop.setAge(0);
        block.setBlockData(crop);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onRightClickCrop(PlayerInteractEvent e){
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        BlockData data = block.getBlockData();
        if (!(data instanceof Ageable)) return;
        Ageable crop = (Ageable) data;

        if (crop.getAge() < crop.getMaximumAge()) return;

        Player player = e.getPlayer();
        ItemStack inHand = player.getInventory().getItemInMainHand();

        HoeType hoeType = HoeType.fromMaterial(inHand.getType());
        if (hoeType == null) return;

        CropType cropType = CropType.fromCrop(block.getType());
        if (cropType == null) return;

        ItemMeta meta = inHand.getItemMeta();

        applyHoeDurability(inHand, meta, block, player);

        resetCropAge(block, crop);

        int fortuneLevel = getFortuneLevel(meta);
        dropCropItems(block, cropType, hoeType, fortuneLevel);

        triggerRareEvent(block, player);

        e.setCancelled(true);
    }

    // circumvent breaking crop any other way other than hoe right click
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCropBreak(BlockBreakEvent e){
        Block block = e.getBlock();
        CropType cropType = CropType.fromCrop(block.getType());
        if (cropType == null) return;
        e.setCancelled(true);
    }
}
