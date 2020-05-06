//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

final class OrderedChat implements Comparable<OrderedChat>
{
    final long order;
    final long chatId;

    OrderedChat(long order, long chatId)
    {
	this.order = order;
	this.chatId = chatId;
    }

    @Override public int compareTo(OrderedChat o)
    {
	if (this.chatId == o.chatId)
	    return 0;
	return -1 * new Long(this.order).compareTo(new Long(o.order));
    }

    @Override public boolean equals(Object obj)
    {
	final OrderedChat o = (OrderedChat) obj;
	return this.chatId == o.chatId;
    }
}
