package pl.asie.computronics.util;

import li.cil.oc.api.driver.DeviceInfo.DeviceAttribute;
import li.cil.oc.api.internal.Colored;
import li.cil.oc.client.KeyBindings;
import li.cil.oc.util.ItemCosts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.lib.util.ColorUtils;
import pl.asie.lib.util.internal.IColorable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.asie.lib.reference.Capabilities.COLORABLE_CAPABILITY;

/**
 * @author Vexatos
 */
public class OCUtils {

	public static NBTTagCompound dataTag(final ItemStack stack) {
		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		final NBTTagCompound nbt = stack.getTagCompound();
		// This is the suggested key under which to store item component data.
		// You are free to change this as you please.
		if(!nbt.hasKey("oc:data")) {
			nbt.setTag("oc:data", new NBTTagCompound());
		}
		return nbt.getCompoundTag("oc:data");
	}

	public static class Device {

		private final String Class;
		private final String Description;
		private final String Vendor;
		private final String Product;
		private final String[] other;

		public Device(String Class, String Description, String Vendor, String Product, String... other) {
			this.Class = Class;
			this.Description = Description;
			this.Vendor = Vendor;
			this.Product = Product;
			this.other = other;
		}

		public Map<String, String> deviceInfo() {
			Map<String, String> deviceInfo = new HashMap<String, String>();
			deviceInfo.put(DeviceAttribute.Class, Class);
			deviceInfo.put(DeviceAttribute.Description, Description);
			deviceInfo.put(DeviceAttribute.Vendor, Vendor);
			deviceInfo.put(DeviceAttribute.Product, Product);
			for(int i = 0; i + 1 < other.length; i += 2) {
				deviceInfo.put(other[i], other[i + 1]);
			}
			return deviceInfo;
		}
	}

	public static final class Vendors {

		public static final String
			ACME = "ACME Co.",
			BuildCraft = "BuildCraft, Inc.",
			Hosencorp = "Hosencorp AG",
			HuggingCreeper = "Hugging Creeper Industries",
			Lumiose = "Lumiose Lighting",
			NSA = "National Security Agency",
			Railcraft = "Railcraft, Inc.",
			Siekierka = "Siekierka Innovations",
			Soluna = "Soluna Technologies",
			Trumbour = "Trumbour Technology",
			Yanaki = "Yanaki Sound Systems";

		private Vendors() {
		}
	}

	private static final int maxWidth = 220;

	//Mostly stolen from Sangar
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("unchecked")
	public static void addTooltip(ItemStack stack, List tooltip) {
		{
			FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
			final String key = stack.getUnlocalizedName() + ".tip";
			String tip = StringUtil.localize(key);
			if(!tip.equals(key)) {
				String[] lines = tip.split("\n");
				boolean shouldShorten = (font.getStringWidth(tip) > maxWidth) && !KeyBindings.showExtendedTooltips();
				if(shouldShorten) {
					tooltip.add(StringUtil.localizeAndFormat("oc:tooltip.TooLong",
						KeyBindings.getKeyBindingName(KeyBindings.extendedTooltip())));
				} else {
					for(String line : lines) {
						List list = font.listFormattedStringToWidth(line, maxWidth);
						tooltip.addAll(list);
					}
				}
			}
		}
		if(ItemCosts.hasCosts(stack)) {
			if(KeyBindings.showMaterialCosts()) {
				ItemCosts.addTooltip(stack, tooltip);
			} else {
				tooltip.add(StringUtil.localizeAndFormat(
					"oc:tooltip.MaterialCosts",
					KeyBindings.getKeyBindingName(KeyBindings.materialCosts())));
			}
		}
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("oc:data")) {
			NBTTagCompound data = stack.getTagCompound().getCompoundTag("oc:data");
			if(data.hasKey("node") && data.getCompoundTag("node").hasKey("address")) {
				tooltip.add(TextFormatting.DARK_GRAY
					+ data.getCompoundTag("node").getString("address").substring(0, 13) + "..."
					+ TextFormatting.GRAY);
			}
		}
	}

	@CapabilityInject(Colored.class)
	public static Capability<Colored> COLORED_CAPABILITY;

	@Nullable
	public static IColorable getColorable(@Nullable ICapabilityProvider provider, EnumFacing side) {
		if(provider != null && provider.hasCapability(COLORABLE_CAPABILITY, side)) {
			return provider.getCapability(COLORABLE_CAPABILITY, side);
		}
		if(provider != null && provider.hasCapability(COLORED_CAPABILITY, side)) {
			return new ConvertedColorable(provider.getCapability(COLORED_CAPABILITY, side));
		}
		return null;
	}

	/**
	 * @author Vexatos
	 */
	public static class ConvertedColorable implements IColorable {

		private final Colored colored;

		public ConvertedColorable(Colored colored) {
			this.colored = colored;
		}

		@Override
		public boolean canBeColored() {
			return colored.controlsConnectivity();
		}

		@Override
		public int getColor() {
			return colored.getColor();
		}

		@Override
		public int getDefaultColor() {
			return ColorUtils.Color.LightGray.color;
		}

		@Override
		public void setColor(int color) {
			colored.setColor(color);
		}
	}
}
