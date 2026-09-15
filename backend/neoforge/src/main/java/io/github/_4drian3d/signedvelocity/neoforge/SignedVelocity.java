package io.github._4drian3d.signedvelocity.neoforge;

import io.github._4drian3d.signedvelocity.common.queue.SignedQueue;
import io.github._4drian3d.signedvelocity.common.queue.SignedResult;
import io.github._4drian3d.signedvelocity.neoforge.model.QueuedDataPacket;
import io.github._4drian3d.signedvelocity.shared.SignedConstants;
import io.github._4drian3d.signedvelocity.shared.types.QueueType;
import io.github._4drian3d.signedvelocity.shared.types.ResultType;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = "signedvelocity", dist = Dist.DEDICATED_SERVER)
public final class SignedVelocity {
  public static final Logger LOGGER = LoggerFactory.getLogger("SignedVelocity");
  public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(
      SignedConstants.SIGNED_NAMESPACE, SignedConstants.SIGNED_CHANNEL);
  public static final SignedQueue CHAT_QUEUE = new SignedQueue();
  public static final SignedQueue COMMAND_QUEUE = new SignedQueue();

  public SignedVelocity(IEventBus modEventBus, ModContainer modContainer) {
    NeoForge.EVENT_BUS.addListener(this::onPlayerDisconnect);
    modEventBus.addListener(this::onRegisterPayloadHandlers);

    LOGGER.info("Started SignedVelocity");
  }

  public void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
    final var uuid = event.getEntity().getUUID();
    CHAT_QUEUE.removeData(uuid);
    COMMAND_QUEUE.removeData(uuid);
  }

  public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
    event.registrar("1").optional().playToServer(QueuedDataPacket.PACKET_ID, QueuedDataPacket.PACKET_CODEC, (packet, context) -> {
      final SignedQueue queue = switch (QueueType.getOrThrow(packet.source())) {
        case QueueType.COMMAND -> SignedVelocity.COMMAND_QUEUE;
        case QueueType.CHAT -> SignedVelocity.CHAT_QUEUE;
      };
      final SignedResult resulted = switch (ResultType.getOrThrow(packet.result())) {
        case ResultType.CANCEL -> SignedResult.cancel();
        case ResultType.MODIFY -> SignedResult.modify(packet.modifiedMessage());
        case ResultType.ALLOWED -> SignedResult.allowed();
      };
      queue.dataFrom(packet.playerId()).complete(resulted);
    });
  }
}