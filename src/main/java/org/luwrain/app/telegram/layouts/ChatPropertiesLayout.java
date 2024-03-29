//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram.layouts;

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
import org.luwrain.app.telegram.*;

public final class ChatPropertiesLayout extends LayoutBase
{
    private final App app;
    private final SimpleArea propsArea;

    public ChatPropertiesLayout(App app, Chat chat, Runnable closing)
    {
	//FIXME:super
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
	if (chat.type != null && chat.type instanceof ChatTypeSupergroup)
	{
	    final ChatTypeSupergroup s = (ChatTypeSupergroup)chat.type;
	    app.getOperations().getSupergroup(s.supergroupId, (supergroup)->{
		    propsArea.update((lines)->{
		    fillSupergroup(chat, supergroup);
		    lines.addLine("");
			});
		});
	    return;
	}

		if (chat.type != null && chat.type instanceof ChatTypeBasicGroup)
	{
	    final ChatTypeBasicGroup b = (ChatTypeBasicGroup)chat.type;
	    app.getOperations().getBasicGroup(b.basicGroupId, (basicGroup)->{
		    propsArea.update((lines)->{
		    fillBasicGroup(chat, basicGroup);
		    lines.addLine("");
			});
		});
	    return;
	}

		propsArea.update((lines)->{
	fillBasic(chat);
	lines.addLine("");
		    });
    }

    private void fillBasic(Chat chat)
    {
	propsArea.addLine("Тип: " + chat.type.getClass().getName());
	//	propsArea.addLine(chat.chatList.getClass().getName());
	propsArea.addLine("Имя: " + chat.title);
	propsArea.addLine("canBeDeletedOnlyForSelf: " + chat.canBeDeletedOnlyForSelf);
	propsArea.addLine("canBeDeletedForAllUsers: " + chat.canBeDeletedForAllUsers);
	propsArea.addLine("unreadCount: " + chat.unreadCount);
    }

    private void fillSupergroup(Chat chat, Supergroup supergroup)
    {
	propsArea.addLine("Имя: " + supergroup.username);
	propsArea.addLine("Участников: " + supergroup.memberCount);
	propsArea.addLine("Канал: " + supergroup.isChannel);
    }

        private void fillBasicGroup(Chat chat, BasicGroup basicGroup)
    {
	propsArea.addLine("Name: " + chat.title);
	propsArea.addLine("Member count: " + basicGroup.memberCount);
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(propsArea);
    }
}
