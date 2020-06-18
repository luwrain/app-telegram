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
import org.drinkless.tdlib.TdApi.ChatTypePrivate;
import org.drinkless.tdlib.TdApi.User;
import org.drinkless.tdlib.TdApi.Message;
import org.drinkless.tdlib.TdApi.MessageText;
import org.drinkless.tdlib.TdApi.MessageAudio;
import org.drinkless.tdlib.TdApi.Messages;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.template.*;

final class ContactsLayout extends LayoutBase implements ListArea.ClickHandler, Objects.UsersListener
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;

    private final App app;
    private final ListArea contactsArea;

    ContactsLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.contactsArea = new ListArea(createContactsParams()){
		private final Actions actions = actions(
							);
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event, actions))
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
		@Override public Action[] getAreaActions()
		{
		    return actions.getAreaActions();
		}
	    };
	synchronized(app.getObjects()) {
	app.getObjects().usersListeners.add(this);
	}
    }

    @Override public boolean onListClick(ListArea listArea, int index, Object obj)
    {
	if (obj == null)
	    return false	;
		return false;
    }

    @Override public void onUsersUpdate(User user)
    {
    }

    private ListArea.Params createContactsParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new ContactsModel();
	params.appearance = new ChatsListAppearance(app);
	params.clickHandler = this;
	params.name = app.getStrings().contactsAreaName();
	return params;
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(contactsArea);
    }

    private final class ContactsModel implements ListArea.Model
    {
	@Override public int getItemCount()
	{
	    return 0;
	}
	@Override public Object getItem(int index)
	{
	    return "";
	}
	@Override public void refresh()
	{
	}
    }
    }
