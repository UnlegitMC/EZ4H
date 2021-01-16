package me.liuli.ez4h.translators.bedrockTranslators.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityHeadLookPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityTeleportPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;
import me.liuli.ez4h.minecraft.bedrock.Client;
import me.liuli.ez4h.translators.BedrockTranslator;
import me.liuli.ez4h.translators.cache.EntityInfo;
import me.liuli.ez4h.utils.BedrockUtils;

public class MovePlayerPacketTranslator implements BedrockTranslator {
    @Override
    public void translate(BedrockPacket inPacket, Client client) {
        MovePlayerPacket packet=(MovePlayerPacket)inPacket;
        Vector3f position=packet.getPosition(),rotation=packet.getRotation();
        if(packet.getRuntimeEntityId()==client.clientStat.entityId){
            switch (packet.getMode()){
                case RESPAWN:
                case NORMAL:{
                    if(!(BedrockUtils.calcDistance(client.clientStat.x,client.clientStat.y,client.clientStat.z,position.getX(),position.getY(),position.getZ())>8)){
                        break;
                    }
                }
                case HEAD_ROTATION:
                case TELEPORT:{
                    ServerPlayerPositionRotationPacket serverPlayerPositionRotationPacket = new ServerPlayerPositionRotationPacket(position.getX(), position.getY() - 1.62, position.getZ(),rotation.getY(),rotation.getX(), 1);
                    client.sendPacket(serverPlayerPositionRotationPacket);
                    client.clientStat.x=position.getX();
                    client.clientStat.y=position.getY();
                    client.clientStat.z=position.getZ();
                    break;
                }
            }
        }else{
            EntityInfo entityInfo=client.clientStat.entityInfoMap.get((int)packet.getRuntimeEntityId());
            double moveX=position.getX()-entityInfo.x,moveY=(position.getY()-1.62)-entityInfo.y,moveZ=position.getZ()-entityInfo.z;
            if(!packet.getMode().equals(MovePlayerPacket.Mode.TELEPORT)){
                client.sendPacket(new ServerEntityPositionRotationPacket((int) packet.getRuntimeEntityId(), moveX,moveY,moveZ,rotation.getY(),rotation.getX(), packet.isOnGround()));
            }else{
                client.sendPacket(new ServerEntityTeleportPacket((int) packet.getRuntimeEntityId(), position.getX(),position.getY()-1.62, position.getZ(),rotation.getY(),rotation.getX(), packet.isOnGround()));
            }
            entityInfo.x=position.getX();
            entityInfo.y= (float) (position.getY()-1.62);
            entityInfo.z=position.getZ();
            client.sendPacket(new ServerEntityHeadLookPacket((int)packet.getRuntimeEntityId(),rotation.getZ()));
        }
    }

    @Override
    public Class<? extends BedrockPacket> getPacketClass() {
        return MovePlayerPacket.class;
    }
}