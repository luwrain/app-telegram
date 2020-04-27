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
import org.drinkless.tdlib.TdApi.User;
import org.drinkless.tdlib.TdApi.Message;
import org.drinkless.tdlib.TdApi.MessageText;
import org.drinkless.tdlib.TdApi.Messages;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.template.*;

final class MainLayout extends LayoutBase implements ListArea.ClickHandler, ConsoleArea.InputHandler, ConsoleArea.ClickHandler, Objects.ChatsListener, Objects.UsersListener
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;

    private final App app;
    private final ListArea chatsArea;
    private final ConsoleArea consoleArea;

    private Object[] items = new Object[0];
    private Chat activeChat = null;
    private Message[] messages = new Message[0];

    MainLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.chatsArea = new ListArea(createChatsParams()){
		private final Actions actions = actions(
							action("add-contact", "Добавить контакт", new KeyboardEvent(KeyboardEvent.Special.INSERT), MainLayout.this::actAddContact)
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
		    switch(event.getCode())
		    {
		    case REFRESH:
			app.getOperations().getContacts();
			return true;
		    }
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
	this.consoleArea = new ConsoleArea(createConsoleParams()){
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
	synchronized(app.getObjects()) {
	app.getObjects().chatsListeners.add(this);
		app.getObjects().usersListeners.add(this);
	}
    }

    @Override public boolean onListClick(ListArea listArea, int index, Object obj)
    {
	if (obj == null)
	    return false	; 
	if (obj instanceof User)
	{
	    final User user = (User)obj;
	    app.getOperations().openChat(user);
	    return true;
	}
	if (obj instanceof Chat)
	{
	    final Chat chat = (Chat)obj;
	    this.activeChat = chat;
	    app.getOperations().getChatHistory(chat, (messagesChat, messages)->{
		    this.messages = messages.messages;
		    consoleArea.refresh();
		});
	    app.getLuwrain().setActiveArea(consoleArea);
	    return true;
	}
	return false;
    }

        @Override public ConsoleArea.InputHandler.Result onConsoleInput(ConsoleArea area, String text)
    {
	NullCheck.notNull(text, "text");
	if (text.isEmpty() || activeChat == null)
	    	return ConsoleArea.InputHandler.Result.REJECTED;
	app.getOperations().sendMessage(activeChat, text);
return ConsoleArea.InputHandler.Result.OK;
    }

    @Override public boolean onConsoleClick(ConsoleArea consoleArea, int index, Object obj)
    {
	return false;
    }



    @Override public void onChatsUpdate(Chat chat)
    {
	updateChats();
    }

        @Override public void onUsersUpdate(User user)
    {
	updateChats();
    }


    private void updateChats()
    {
		final List res = new LinkedList();
	for(Map.Entry<Long, Chat> e: app.getObjects().chats.entrySet())
	    res.add(e.getValue());

		for(Map.Entry<Integer, User> e: app.getObjects().users.entrySet())
	    res.add(e.getValue());

		
	items = res.toArray(new Object[res.size()]);
	chatsArea.refresh();Log.debug(LOG_COMPONENT, "" + res.size() + " items in main layout");

    }

    private boolean actAddContact()
    {
	app.getLuwrain().playSound(Sounds.OK);
	return true;
    }

    private ListArea.Params createChatsParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new ChatsModel();
	params.appearance = new ChatsListAppearance();
	params.clickHandler = this;
	params.name = "area1";
	return params;
    }

        private ConsoleArea.Params createConsoleParams()
    {
	final ConsoleArea.Params params = new ConsoleArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new ConsoleAreaModel();
	params.appearance = new ConsoleAreaAppearance();
	params.areaName = "Беседа";
	params.inputPos = ConsoleArea.InputPos.TOP;
	params.inputPrefix = ">";
	params.clickHandler = this;
	params.inputHandler = this;
	return params;
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(AreaLayout.LEFT_RIGHT, chatsArea, consoleArea);
    }

    private class ChatsModel implements ListArea.Model
    {
	@Override public int getItemCount()
	{
	    return items.length;
	}
	@Override public Object getItem(int index)
	{
	    return items[index];
	}
	@Override public void refresh()
	{
	}
    }

private class ChatsListAppearance implements ListArea.Appearance
{
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
		if (item instanceof User)
	{
	    final User user = (User)item;
	    app.getLuwrain().setEventResponse(DefaultEventResponse.listItem(user.firstName + " " + user.lastName, Suggestions.LIST_ITEM));
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
		if (item instanceof User)
	{
	    final User user = (User)item;
	    return user.firstName + " " + user.lastName;
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

    private final class ConsoleAreaModel implements ConsoleArea.Model
    {
        @Override public int getConsoleItemCount()
	{
	    return messages.length;
	}
	@Override public Object getConsoleItem(int index)
	{
	    if (index < 0 || index >= messages.length)
		throw new IllegalArgumentException("index (" + index + ") must be greater or equal to zero and less than " + String.valueOf(messages.length));
	    return messages[index];
	}
    }

        private final class ConsoleAreaAppearance implements ConsoleArea.Appearance
    {
	@Override public void announceItem(Object item)
	{
	    NullCheck.notNull(item, "item");
	    if (item instanceof Message)
	    {
		final Message message = (Message)item;

		if (message.content instanceof MessageText)
		{
		    final MessageText text = (MessageText)message.content;
			    app.getLuwrain().setEventResponse(DefaultEventResponse.text(text.text.text));
		}
			    return;
	    }
	    app.getLuwrain().setEventResponse(DefaultEventResponse.text(item.toString()));
	}
	@Override public String getTextAppearance(Object item)
	{
	    NullCheck.notNull(item, "item");
	    return item.toString();
	}
    }
}
