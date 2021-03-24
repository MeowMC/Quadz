package dev.lazurite.quadz.common.item.container;

import dev.lazurite.quadz.common.util.Frequency;
import dev.lazurite.quadz.common.util.type.VideoDevice;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

/**
 * A dumping ground for goggles information. Mainly for storing
 * frequency and whether or not the goggles are powered on.
 * @see VideoDevice
 */
public class GogglesContainer extends ItemComponent implements VideoDevice {
    private final ItemStack stack;

    public GogglesContainer(ItemStack stack) {
        super(stack);
        this.stack = stack;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public void setFrequency(Frequency frequency) {
        putInt("channel", frequency.getChannel());
        putInt("band", frequency.getBand());
    }

    @Override
    public Frequency getFrequency() {
        return new Frequency((char) getInt("band"), getInt("channel"));
    }

    public void setEnabled(boolean enabled) {
        putBoolean("enabled", enabled);
    }

    public boolean isEnabled() {
        return getBoolean("enabled");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GogglesContainer) {
            return ((GogglesContainer) obj).getFrequency().equals(getFrequency());
        }

        return false;
    }
}
