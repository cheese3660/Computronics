package pl.asie.computronics.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import pl.asie.lib.network.MessageHandlerBase;
import pl.asie.lib.network.Packet;

import java.io.IOException;

public class NetworkHandlerServer extends MessageHandlerBase {
	@Override
	public void onMessage(Packet packet, INetHandler handler, EntityPlayer player, int command)
		throws IOException {
		switch(command) {
			/*case Packets.PACKET_TICKET_SYNC: {
				if(Mods.isLoaded(Mods.Railcraft)) {
					Computronics.railcraft.onMessageRailcraft(packet, player, true); TODO Railcraft
				}
			}
			break;
			case Packets.PACKET_TICKET_PRINT: {
				if(Mods.isLoaded(Mods.Railcraft)) {
					Computronics.railcraft.printTicket(packet, player, true);
				}
			}
			break;*/
		}
	}
}
