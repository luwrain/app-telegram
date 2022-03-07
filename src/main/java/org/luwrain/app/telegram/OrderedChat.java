//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import org.drinkless.tdlib.TdApi.ChatPosition;

final class OrderedChat implements Comparable<OrderedChat>
{
    final long chatId;
    final ChatPosition position;

    OrderedChat(long chatId, ChatPosition position)
    {
	this.chatId = chatId;
	this.position = position;
    }

    @Override public int compareTo(OrderedChat o)
    {
	if (this.position.order != o.position.order) 
	    return o.position.order < this.position.order ? -1 : 1;
	if (this.chatId != o.chatId) 
	    return o.chatId < this.chatId ? -1 : 1;
	return 0;
    }

    @Override public boolean equals(Object obj)
    {
	OrderedChat o = (OrderedChat) obj;
	return this.chatId == o.chatId && this.position.order == o.position.order;
    }
}
