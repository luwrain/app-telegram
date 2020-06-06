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
import org.drinkless.tdlib.TdApi.Messages;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.template.*;

final class MainLayout extends LayoutBase implements ListArea.ClickHandler, ConsoleArea.InputHandler, ConsoleArea.ClickHandler, Objects.ChatsListener
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;
    static private final int CHAT_NUM_LIMIT = 100;

    private final App app;
    private final ListArea chatsArea;
    private final ConsoleArea consoleArea;

    private Chat[] chats = new Chat[0];
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
		    /*
		    switch(event.getCode())
		    {
		    case REFRESH:
						app.getOperations().getContacts();
			app.getOperations().getMainChatList(20);
			return true;
		    }
		    */
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
	    updateActiveChatHistory();
	    consoleArea.reset(false);
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
	app.getOperations().sendMessage(activeChat, text, ()->{
		consoleArea.setInput("");
		updateActiveChatHistory();
		app.getLuwrain().playSound(Sounds.DONE);
	    });
return ConsoleArea.InputHandler.Result.OK;
    }

    @Override public boolean onConsoleClick(ConsoleArea consoleArea, int index, Object obj)
    {
	return false;
    }

    @Override public void onChatsUpdate(Chat chat)
    {
buildChatsList();
if (chat.lastMessage != null)
{
    final String text = Utils.getMessageText(chat.lastMessage);
    if (!text.trim().isEmpty())
	app.getLuwrain().speak(text.trim(), Sounds.CHAT_MESSAGE);
}
    }

    private void buildChatsList()
    {
	final Objects objects = app.getObjects();
		final List<Chat> res = new LinkedList();
		synchronized(objects) {
		    Log.debug(LOG_COMPONENT, "building");
		for(OrderedChat c: objects.mainChats)
		{
		    final Chat chat = objects.chats.get(c.chatId);
		    if (chat != null)
		    {
			Log.debug(LOG_COMPONENT, "order " + c.order);
			res.add(chat);
		    }
		}
		}
	this.chats = res.toArray(new Chat[res.size()]);
	chatsArea.refresh();Log.debug(LOG_COMPONENT, "" + res.size() + " items in main layout");
    }

    private void updateActiveChatHistory()
    {
	if (activeChat == null)
	    return;
		    app.getOperations().getChatHistory(activeChat, (messagesChat, messages)->{
		    final List<Message> res = new LinkedList();
		    res.add(messagesChat.lastMessage);
		    res.addAll(Arrays.asList(messages.messages));
		    this.messages = res.toArray(new Message[res.size()]);
		    consoleArea.refresh();
		});
    }

    private boolean actAddContact()
    {
	return true;
    }

    private ListArea.Params createChatsParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new ChatsModel();
	params.appearance = new ChatsListAppearance(app);
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

    void activate()
    {
			app.getOperations().fillMainChatList(CHAT_NUM_LIMIT);
	app.getLuwrain().setActiveArea(chatsArea);
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(AreaLayout.LEFT_RIGHT, chatsArea, consoleArea);
    }

    private class ChatsModel implements ListArea.Model
    {
	@Override public int getItemCount()
	{
	    return chats.length;
	}
	@Override public Object getItem(int index)
	{
	    return chats[index];
	}
	@Override public void refresh()
	{
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
	private final MessageAppearance messageAppearance = new MessageAppearance(app.getLuwrain());
	@Override public void announceItem(Object item)
	{
	    NullCheck.notNull(item, "item");
	    if (!(item instanceof Message))
	    {
	    app.getLuwrain().setEventResponse(DefaultEventResponse.text(item.toString()));
	    }
	    messageAppearance.announce((Message)item); 
	}
	@Override public String getTextAppearance(Object item)
	{
	    NullCheck.notNull(item, "item");

	    if (!(item instanceof Message))
		return item.toString();
	    return messageAppearance.getTextAppearance((Message)item);
	}
    }
}
