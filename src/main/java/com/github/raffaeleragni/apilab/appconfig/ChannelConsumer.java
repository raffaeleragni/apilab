package com.github.raffaeleragni.apilab.appconfig;

import com.rabbitmq.client.Channel;
import java.io.IOException;

@FunctionalInterface
public interface ChannelConsumer {

  void channelAccept(Channel t) throws IOException;
  
}