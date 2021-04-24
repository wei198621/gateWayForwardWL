package com.tiza.leo.gateway.handler;

import com.tiza.leo.gateway.message.MessageContext;
import com.tiza.leo.gateway.protocol.ProtocolResponder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.portable.ResponseHandler;

import javax.xml.ws.Response;
import java.util.Optional;

/**
 * Author: tz_wl
 * Date: 2021/4/18 10:42
 * Content:
 */
@Slf4j
@ChannelHandler.Sharable
public class ResponderHandler extends ChannelInboundHandlerAdapter {
    ProtocolResponder responder;
    public ResponderHandler(ProtocolResponder responder){
        this.responder = responder;
    }

    /**
     *
     * @param ctx
     * @param msg
     * @throws Exception
    // step 01  将01 指令 ，沿着当前管道 向上 发回给发送者
    // step 02  往下面管道发送
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageContext message= (MessageContext) msg;
        //Thread.sleep(1);  //增加1毫秒延迟应答，防止下行应答覆盖上行数据
        Optional<MessageContext> outBoundMessage=responder.ack(message);
        //若返回null 不作应答
        if(outBoundMessage ==null){
            return ;
        }
        // 将 01 指令回给 终端
        if(outBoundMessage.isPresent()){
            ctx.pipeline().writeAndFlush(outBoundMessage.get());
        }

        // 继续往下发 指令
        ctx.fireChannelRead(msg);
    }
}
