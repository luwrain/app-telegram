//
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.io.*;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.TdApi.Chat;
import org.drinkless.tdlib.TdApi.ChatTypeSupergroup;
import org.drinkless.tdlib.TdApi.Supergroup;
import org.drinkless.tdlib.TdApi.ChatTypeBasicGroup;
import org.drinkless.tdlib.TdApi.BasicGroup;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

final class ChatPropertiesLayout extends LayoutBase
{
    private final App app;
    private final SimpleArea propsArea;

    ChatPropertiesLayout(App app, Chat chat, Runnable closing)
    {
	NullCheck.notNull(app, "app");
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(closing, "closing");
	this.app = app;
	this.propsArea = new SimpleArea(new DefaultControlContext(app.getLuwrain()), app.getStrings().chatPropsAreaName()) {
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event, closing))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event))
			return true;
		    			return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
	    };
	fill(chat);
    }

    private void fill(Chat chat)
    {
	NullCheck.notNull(chat, "chat");
	if (chat.type != null && chat.type instanceof ChatTypeSupergroup)
	{
	    final ChatTypeSupergroup s = (ChatTypeSupergroup)chat.type;
	    app.getOperations().getSupergroup(s.supergroupId, (supergroup)->{
		    propsArea.beginLinesTrans();
		    fillSupergroup(chat, supergroup);
		    propsArea.addLine("");
		    propsArea.endLinesTrans();
		});
	    return;
	}

		if (chat.type != null && chat.type instanceof ChatTypeBasicGroup)
	{
	    final ChatTypeBasicGroup b = (ChatTypeBasicGroup)chat.type;
	    app.getOperations().getBasicGroup(b.basicGroupId, (basicGroup)->{
		    propsArea.beginLinesTrans();
		    fillBasicGroup(chat, basicGroup);
		    propsArea.addLine("");
		    propsArea.endLinesTrans();
		});
	    return;
	}

		
		propsArea.beginLinesTrans();
	fillBasic(chat);
	propsArea.addLine("");
	propsArea.endLinesTrans();
    }

    private void fillBasic(Chat chat)
    {
	NullCheck.notNull(chat, "chat");
	propsArea.addLine("Тип: " + chat.type.getClass().getName());
	propsArea.addLine(chat.chatList.getClass().getName());
	propsArea.addLine("Имя: " + chat.title);
	propsArea.addLine("Unread: " + chat.isMarkedAsUnread);
	propsArea.addLine("canBeDeletedOnlyForSelf: " + chat.canBeDeletedOnlyForSelf);
	propsArea.addLine("canBeDeletedForAllUsers: " + chat.canBeDeletedForAllUsers);
	propsArea.addLine("unreadCount: " + chat.unreadCount);
    }

    private void fillSupergroup(Chat chat, Supergroup supergroup)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(supergroup, "supergroup");
	propsArea.addLine("Name: " + supergroup.username);
	propsArea.addLine("Member count: " + supergroup.memberCount);
	propsArea.addLine("Is channel: " + supergroup.isChannel);
    }

        private void fillBasicGroup(Chat chat, BasicGroup basicGroup)
    {
	NullCheck.notNull(chat, "chat");
	NullCheck.notNull(basicGroup, "basicGroup");
	propsArea.addLine("Name: " + chat.title);
	propsArea.addLine("Member count: " + basicGroup.memberCount);
    }


    AreaLayout getLayout()
    {
	return new AreaLayout(propsArea);
    }
}
