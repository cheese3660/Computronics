package pl.asie.computronics.integration.charset.audio;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.charset.api.audio.AudioAPI;
import pl.asie.charset.api.audio.AudioData;
import pl.asie.charset.api.audio.AudioSink;
import pl.asie.charset.api.audio.IAudioReceiver;
import pl.asie.charset.api.audio.IAudioSource;
import pl.asie.charset.lib.audio.*;
import pl.asie.charset.lib.audio.AudioDataDFPWM;
import pl.asie.computronics.api.audio.AudioPacket;
import pl.asie.computronics.api.audio.AudioPacketDFPWM;
import pl.asie.computronics.reference.Mods;
import pl.asie.computronics.tile.TileAudioCable;
import pl.asie.computronics.tile.TileSpeaker;

/**
 * @author Vexatos
 */
public class IntegrationCharsetAudio {
	@CapabilityInject(IAudioSource.class)
	public static Capability<IAudioSource> SOURCE_CAPABILITY;
	@CapabilityInject(IAudioReceiver.class)
	public static Capability<IAudioReceiver> RECEIVER_CAPABILITY;

	private static final ResourceLocation CABLE_SINK_KEY = new ResourceLocation("computronics:cableSink");
	private static final ResourceLocation SPEAKER_SINK_KEY = new ResourceLocation("computronics:speakerSink");

	public void postInit() {
		AudioAPI.SINK_REGISTRY.register(AudioSinkSpeaker.class);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onAttach(final AttachCapabilitiesEvent.TileEntity event) {
		if (event.getTileEntity() instanceof TileSpeaker
				&& RECEIVER_CAPABILITY != null) {
			event.addCapability(SPEAKER_SINK_KEY, new ICapabilityProvider() {
				private final AudioSink sink = new AudioSinkSpeaker((TileSpeaker) event.getTileEntity());

				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return capability == RECEIVER_CAPABILITY && facing != null;
				}

				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
					return capability == RECEIVER_CAPABILITY ? (T) sink : null;
				}
			});
		} else if (event.getTileEntity() instanceof TileAudioCable
				&& RECEIVER_CAPABILITY != null) {
			event.addCapability(CABLE_SINK_KEY, new ICapabilityProvider() {
				private final TileAudioCable cable = (TileAudioCable) event.getTileEntity();
				private final AudioReceiverCable[] RECEIVERS = new AudioReceiverCable[6];

				@Override
				public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
					return capability == RECEIVER_CAPABILITY && facing != null;
				}

				@Override
				public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
					if (capability == RECEIVER_CAPABILITY && facing != null) {
						if (RECEIVERS[facing.ordinal()] == null) {
							RECEIVERS[facing.ordinal()] = new AudioReceiverCable(cable, facing);
						}

						return (T) RECEIVERS[facing.ordinal()];
					} else {
						return null;
					}
				}
			});
		}
	}

	public static int send(IBlockAccess world, BlockPos pos, AudioPacket packet, float volume, boolean ignoreComputronicsAPICheck) {
		AudioData dataNew;
		pl.asie.charset.api.audio.AudioPacket packetNew;

		if (packet instanceof AudioPacketDFPWM) {
			int time = ((AudioPacketDFPWM) packet).data.length * 8000 / ((AudioPacketDFPWM) packet).frequency;
			dataNew = new AudioDataDFPWM(((AudioPacketDFPWM) packet).data, time);
		} else {
			dataNew = new AudioDataDummy();
		}

		packetNew = new pl.asie.charset.api.audio.AudioPacket(dataNew, volume);
		for (EnumFacing facing : EnumFacing.VALUES) {
			BlockPos posO = pos.offset(facing);
			TileEntity tile = world.getTileEntity(posO);
			if (tile != null && tile.hasCapability(RECEIVER_CAPABILITY, facing.getOpposite())) {
				if (!ignoreComputronicsAPICheck && tile instanceof pl.asie.computronics.api.audio.IAudioReceiver) {
					continue;
				}
				tile.getCapability(RECEIVER_CAPABILITY, facing.getOpposite()).receive(packetNew);
			}
		}

		if (packetNew.getSinkCount() > 0) {
			for (AudioSink sink : packetNew.getSinks()) {
				packet.addReceiver(new AudioReceiverConverted(sink));
			}
			return packetNew.getSinkCount();
		} else {
			return 0;
		}
	}

	public static boolean connects(TileEntity tile, EnumFacing dir) {
		return tile != null && (tile.hasCapability(SOURCE_CAPABILITY, dir)
				|| tile.hasCapability(RECEIVER_CAPABILITY, dir));
	}
}