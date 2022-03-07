//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi.Chat;
import org.drinkless.tdlib.TdApi.Message;
import org.drinkless.tdlib.TdApi.MessageText;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class ChatsListAppearance extends ListUtils.DefaultAppearance<Chat>
{
    private final App app;

    ChatsListAppearance(App app, ControlContext context)
    {
	super(context);
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    @Override public void announceItem(Chat chat, Set<Flags> flags)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(flags, "flags");
	final StringBuilder b = new StringBuilder();
	b.append(chat.title);
	if (chat.lastMessage != null)
	{
	    final String text = MessageAppearance.getMessageText(chat.lastMessage);
	    if (!text.trim().isEmpty())
		b.append(" ").append(text.trim());
	}
	app.getLuwrain().setEventResponse(DefaultEventResponse.listItem(chat.unreadCount > 0?Sounds.ATTENTION:Sounds.LIST_ITEM, new String(b), Suggestions.LIST_ITEM));
    }

    @Override public String getScreenAppearance(Chat chat, Set<Flags> flags)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(flags, "flags");
	    final StringBuilder b = new StringBuilder();
	    b.append(chat.title);
	    if (chat.lastMessage != null)
	    {
		final String text = MessageAppearance.getMessageText(chat.lastMessage);
		if (!text.trim().isEmpty())
		    b.append(": ").append(text);
	    }
	    return new String(b);
    }
}
