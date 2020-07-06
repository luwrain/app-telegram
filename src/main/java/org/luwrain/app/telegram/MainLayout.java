//
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
							action("contacts", app.getStrings().actionContacts(), new InputEvent(InputEvent.Special.F6), MainLayout.this::actContacts),
							action("close-chat", app.getStrings().actionCloseChat(), new InputEvent(InputEvent.Special.DELETE), MainLayout.this::actCloseChat)
							);
		@Override public boolean onInputEvent(InputEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() == EnvironmentEvent.Type.REGULAR)
		    switch(event.getCode())
		    {
		    case PROPERTIES:
			return onChatProperties();
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
		private final Actions actions = actions(
														action("contacts", app.getStrings().actionContacts(), new InputEvent(InputEvent.Special.F6), MainLayout.this::actContacts)
							);
		@Override public boolean onInputEvent(InputEvent event)
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
	app.getObjects().chatsListeners.add(this);
	}
    }

    @Override public boolean onListClick(ListArea listArea, int index, Object obj)
    {
	if (obj == null)
	    return false	;
	if (obj instanceof Chat)
	{
	    final Chat chat = (Chat)obj;
	    app.getOperations().openChat(chat, ()->{
		    this.activeChat = chat;
		    updateActiveChatHistory();
		    consoleArea.reset(false);
		    app.getLuwrain().setActiveArea(consoleArea);
		});
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
	if (obj == null || !(obj instanceof Message))
	    return false;
	final Message message = (Message)obj;
	if (message.content != null && message.content instanceof MessageAudio)
	{
	    final MessageAudio audio = (MessageAudio)message.content;
	    if (audio.audio.audio.local.isDownloadingActive)
		return false;

	    	    if (audio.audio.audio.local.isDownloadingCompleted)
		    {
			if (audio.audio.audio.local.path == null || audio.audio.audio.local.path.isEmpty())
			    return false;
			if (app.getLuwrain().getPlayer() == null)
			    return false;
			app.getLuwrain().getPlayer().play(new org.luwrain.player.Playlist(new String[]{
				    org.luwrain.util.UrlUtils.fileToUrl(new java.io.File(audio.audio.audio.local.path))
			    }), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS, new Properties());
		return true;
		    }
		    app.getOperations().downloadFile(audio.audio.audio);
		    app.getLuwrain().message("Выполняется доставка файла");//FIXME:
		    return true;
		    
	}
	return false;
    }

    @Override public void onChatsUpdate(Chat chat)
    {
buildChatsList();
if (chat.lastMessage != null)
{
    final String text = MessageAppearance.getMessageText(chat.lastMessage);
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
		if (messagesChat != null && messagesChat.lastMessage != null)
		    res.add(messagesChat.lastMessage);
		if (messages != null && messages.messages != null)
		    res.addAll(Arrays.asList(messages.messages));
		this.messages = res.toArray(new Message[res.size()]);
		app.getOperations().viewMessages(activeChat, this.messages);
		consoleArea.refresh();
	    });
    }

    private boolean actContacts()
    {
	app.layouts().contacts();
	return true;
    }

        private boolean actCloseChat()
    {
	final Object obj = chatsArea.selected();
	if (obj == null || !(obj instanceof Chat))
	    return false;
	final Chat chat = (Chat)obj;
	app.getOperations().leaveChat(chat, ()->app.getLuwrain().playSound(Sounds.OK));
	return true;
    }

    private boolean onChatProperties()
    {
	final Object obj = chatsArea.selected();
	if (obj == null || !(obj instanceof Chat))
	    return false;
	final Chat chat = (Chat)obj;
	final ChatPropertiesLayout layout = new ChatPropertiesLayout(app, chat, ()->app.layouts().main());
	app.layout(layout.getLayout());
	return true;
    }


    private ListArea.Params createChatsParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new ChatsModel();
	params.appearance = new ChatsListAppearance(app);
	params.clickHandler = this;
	params.name = app.getStrings().chatsAreaName();
	return params;
    }

        private ConsoleArea.Params createConsoleParams()
    {
	final ConsoleArea.Params params = new ConsoleArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new ConsoleAreaModel();
	params.appearance = new ConsoleAreaAppearance();
	params.name = "Беседа";
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
        @Override public int getItemCount()
	{
	    return messages.length;
	}
	@Override public Object getItem(int index)
	{
	    if (index < 0 || index >= messages.length)
		throw new IllegalArgumentException("index (" + index + ") must be greater or equal to zero and less than " + String.valueOf(messages.length));
	    return messages[index];
	}
    }

        private final class ConsoleAreaAppearance implements ConsoleArea.Appearance
    {
	private final MessageAppearance messageAppearance = new MessageAppearance(app.getLuwrain(), app.getObjects());
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
