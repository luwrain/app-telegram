//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram.layouts;

import java.util.*;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.controls.ListArea.*;
import org.luwrain.controls.ListUtils.*;
import org.luwrain.app.base.*;
import org.luwrain.app.telegram.*;

import static org.luwrain.core.DefaultEventResponse.*;

public final class ChatMembersLayout extends LayoutBase
{
    static private final String
	LOG_COMPONENT = Core.LOG_COMPONENT;

    interface Result
    {
	void onResult(List<User> users);
    }

    private final App app;
    final ListArea<User> listArea;
    private final Chat chat;
    private final List<User> items = new ArrayList<>();

    public ChatMembersLayout(App app, Chat chat, ActionHandler closing)
    {
	super(app);
	this.app = app;
	this.chat = chat;
	this.listArea = new ListArea<>(listParams((params)->{
		    params.name = "Поиск  групп и каналов";
		    params.model = new ListModel<>(items);
		}));
	setAreaLayout(listArea, null);
	setCloseHandler(closing);
	    }

    public void showFirst()
    {
	update((res)->{
		items.clear();
		items.addAll(res);
		setActiveArea(listArea);
	    });
    }

    private void update(Result result)
    {
	final List<User> resItems = new ArrayList<>();
		if (chat.type instanceof ChatTypeSupergroup)
	{
	    final long supergroupId = ((ChatTypeSupergroup)chat.type).supergroupId;
	    app.getOperations().callFunc(new GetSupergroupMembers(supergroupId, null, 0, 200), ChatMembers.CONSTRUCTOR, (res)->{
		    final ChatMembers members = (ChatMembers)res;
		    for(ChatMember m: members.members)
		    {
			if (m.memberId instanceof  MessageSenderUser)
			{
			    final MessageSenderUser sender = (MessageSenderUser)m.memberId;
			    final User user = app.getObjects().users.get(sender.userId);
						if (user == null)
			    continue;
						resItems.add(user);
			}
		    }
		    app.getLuwrain().runUiSafely(()->result.onResult(resItems));
		});
	    return;
	}
    }
}
