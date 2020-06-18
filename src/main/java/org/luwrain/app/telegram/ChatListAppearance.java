//
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
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

final class ChatsListAppearance implements ListArea.Appearance
{
    private final App app;

    ChatsListAppearance(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
    }

    @Override public void announceItem(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	if (!(item instanceof Chat))
	{
	    app.getLuwrain().setEventResponse(DefaultEventResponse.listItem(item.toString(), Suggestions.LIST_ITEM));
	    return;
	}
	final Chat chat = (Chat)item;
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

    @Override public String getScreenAppearance(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	if (item instanceof Chat)
	{
	    final Chat chat = (Chat)item;
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
	return item.toString();
    }

    @Override public int getObservableLeftBound(Object item)
    {
	return 0;
    }

    @Override public int getObservableRightBound(Object item)
    {
	return getScreenAppearance(item, EnumSet.noneOf(Flags.class)).length();
    }
}
