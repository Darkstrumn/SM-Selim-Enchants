package selim.selim_enchants.enchants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import selim.selim_enchants.EnchantmentSelim;
import selim.selim_enchants.ITooltipInfo;
import selim.selim_enchants.Registry;
import selim.selim_enchants.SelimEnchants;

@Mod.EventBusSubscriber(modid = SelimEnchants.MOD_ID)
public class EnchantmentEnderShift extends EnchantmentSelim implements ITooltipInfo {

	public EnchantmentEnderShift() {
		super(Rarity.VERY_RARE, EnumEnchantmentType.ALL,
				new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
		this.setRegistryName("ender_shift");
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel) {
		return 25 + (enchantmentLevel) * 10;
	}

	@Override
	protected boolean canApplyTogether(Enchantment ench) {
		return !(ench instanceof EnchantmentRecall);
	}

	@Override
	public boolean canApply(ItemStack stack) {
		if (!this.isEnabled())
			return false;
		return stack != null && (stack.getItem() instanceof ItemTool
				|| stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemBow);
	}

	@Override
	public boolean isTreasureEnchantment() {
		return true;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		if (!this.isEnabled())
			return false;
		return false;
	}

	@SubscribeEvent
	public static void onBlockBreak(HarvestDropsEvent event) {
		if (!Registry.Enchantments.ENDER_SHIFT.isEnabled())
			return;
		if (event.getHarvester() != null && handleDropStacks(event.getHarvester().getEntityWorld(),
				event.getPos(), event.getHarvester(), event.getDrops()))
			event.getDrops().clear();
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onMobDeathFirst(LivingDropsEvent event) {
		if (!Registry.Enchantments.ENDER_SHIFT.isEnabled())
			return;
		Entity killer = event.getSource().getTrueSource();
		if (!(killer instanceof EntityPlayer) || killer.world.isRemote || event.isCanceled())
			return;
		EntityPlayer livingKiller = (EntityPlayer) killer;
		int level = EnchantmentHelper.getEnchantmentLevel(Registry.Enchantments.ENDER_SHIFT,
				livingKiller.getHeldItem(EnumHand.MAIN_HAND));
		if (level <= 0)
			return;
		if (!event.isCanceled())
			event.getEntity().captureDrops(event.getDrops());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onMobDeathLast(LivingDropsEvent event) {
		if (!Registry.Enchantments.ENDER_SHIFT.isEnabled())
			return;
		Entity trueSource = event.getSource().getTrueSource();
		if (trueSource instanceof EntityPlayer) {
			event.setCanceled(
					handleDropEntities(event.getEntity().world, new BlockPos(event.getEntity()),
							(EntityPlayer) trueSource, event.getEntity().captureDrops()));
		}
	}

	private static boolean handleDropEntities(World world, BlockPos dropPos, EntityPlayer player,
			Collection<EntityItem> dropEntities) {
		if (dropEntities == null)
			return false;
		List<ItemStack> dropStacks = new ArrayList<>();
		for (EntityItem i : dropEntities)
			dropStacks.add(i.getItem());
		return handleDropStacks(world, dropPos, player, dropStacks);
	}

	private static boolean handleDropStacks(World world, BlockPos dropPos, EntityPlayer player,
			Collection<ItemStack> drops) {
		if (player == null)
			return false;
		int level = EnchantmentHelper.getEnchantmentLevel(Registry.Enchantments.ENDER_SHIFT,
				player.getHeldItem(EnumHand.MAIN_HAND));
		if (level <= 0)
			return false;
		InventoryEnderChest enderChest = player.getInventoryEnderChest();
		List<ItemStack> toDrop = new ArrayList<>();
		for (ItemStack s : drops) {
			ItemStack leftover = enderChest.addItem(s);
			if (leftover != null && !leftover.isEmpty())
				toDrop.add(leftover);
		}
		for (ItemStack s : toDrop) {
			EntityItem item = new EntityItem(world, dropPos.getX() + 0.5f, dropPos.getY(),
					dropPos.getZ() + 0.5f);
			item.setItem(s);
			world.spawnEntity(item);
		}
		return true;
	}

}
