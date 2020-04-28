//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi.Chat;

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
	if (item instanceof Chat)
	{
	    final Chat chat = (Chat)item;
	    app.getLuwrain().setEventResponse(DefaultEventResponse.listItem(chat.title, Suggestions.LIST_ITEM));
		    return;
	}
	app.getLuwrain().setEventResponse(DefaultEventResponse.listItem(item.toString(), Suggestions.LIST_ITEM));
	    return;
    }
    @Override public String getScreenAppearance(Object item, Set<Flags> flags)
    {
	NullCheck.notNull(item, "item");
	NullCheck.notNull(flags, "flags");
	if (item instanceof Chat)
	{
	    final Chat chat = (Chat)item;
	    return chat.title;
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
