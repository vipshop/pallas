package com.vip.pallas.search.monitor;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by owen on 21/03/2018.
 */
public class GaugeMonitorHandler extends ChannelDuplexHandler {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        GaugeMonitorService.incConns();
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        GaugeMonitorService.decConns();
        super.channelInactive(ctx);
    }

}
