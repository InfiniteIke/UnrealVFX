package com.unrealvfx.debug;

import com.unrealvfx.network.PacketVFX;
import com.unrealvfx.network.VFXNetwork;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

/**
 * Bacchetta di test (VFX Wand) per la modalità creativa.
 * Permette di testare e spawnare effetti all'istante con i click del mouse.
 */
public class ItemVFXWand extends Item {
    private static final String[] PRESET_LIST = new String[]{
        "dbz:hakai_ball",
        "dbz:hakai_explosion",
        "dbz:sparking_aura_ssj",
        "dbz:sparking_explosion",
        "dbz:zanzoken_dash",
        "dbz:aura_ssj",
        "dbz:explosion_sphere"
    };

    public ItemVFXWand() {
        setUnlocalizedName("unrealvfx.wand");
        setTextureName("stick"); // Usa icona stick come base
        setCreativeTab(CreativeTabs.tabTools);
        setMaxStackSize(1);
    }

    private String getSelectedEffect(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        int index = stack.getTagCompound().getInteger("selected_effect");
        if (index < 0 || index >= PRESET_LIST.length) index = 0;
        return PRESET_LIST[index];
    }

    private void cycleSelectedEffect(ItemStack stack, EntityPlayer player) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        int index = (stack.getTagCompound().getInteger("selected_effect") + 1) % PRESET_LIST.length;
        stack.getTagCompound().setInteger("selected_effect", index);

        if (!player.worldObj.isRemote) {
            player.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[VFX Wand] Effetto selezionato: " +
                EnumChatFormatting.YELLOW + PRESET_LIST[index]
            ));
        }
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            if (player.isSneaking()) {
                cycleSelectedEffect(stack, player);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            String effectId = getSelectedEffect(stack);
            double spawnX = x + 0.5;
            double spawnY = y + 1.0;
            double spawnZ = z + 0.5;

            VFXNetwork.CHANNEL.sendToAllAround(
                PacketVFX.spawn(effectId, spawnX, spawnY, spawnZ, 1.0f),
                new NetworkRegistry.TargetPoint(world.provider.dimensionId, spawnX, spawnY, spawnZ, 128)
            );
        }
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            String effectId = getSelectedEffect(stack);
            if (player.isSneaking()) {
                // Attacca o stacca l'effetto al giocatore
                VFXNetwork.CHANNEL.sendToAllAround(
                    PacketVFX.attach(effectId, player.getEntityId(), 1.0f),
                    new NetworkRegistry.TargetPoint(world.provider.dimensionId, player.posX, player.posY, player.posZ, 128)
                );
                player.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.AQUA + "[VFX Wand] Effetto '" + effectId + "' attivato sul giocatore!"
                ));
            }
        }
        return stack;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(EnumChatFormatting.GRAY + "Effetto attivo: " + EnumChatFormatting.YELLOW + getSelectedEffect(stack));
        tooltip.add(EnumChatFormatting.DARK_AQUA + "• Click Destro su un blocco: " + EnumChatFormatting.WHITE + "Spawna effetto");
        tooltip.add(EnumChatFormatting.DARK_AQUA + "• Shift + Click Destro in aria: " + EnumChatFormatting.WHITE + "Attiva/Aggancia al player");
        tooltip.add(EnumChatFormatting.DARK_AQUA + "• Shift + Click Sinistro in aria: " + EnumChatFormatting.WHITE + "Cambia effetto");
    }
}
