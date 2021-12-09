//
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi.LocalFile;
import org.drinkless.tdlib.TdApi.Chat;
import org.drinkless.tdlib.TdApi.ChatTypePrivate;
import org.drinkless.tdlib.TdApi.User;
import org.drinkless.tdlib.TdApi.Message;
import org.drinkless.tdlib.TdApi.MessageText;
import org.drinkless.tdlib.TdApi.MessageAudio;
import org.drinkless.tdlib.TdApi.MessagePhoto;
import org.drinkless.tdlib.TdApi.MessageDocument;
import org.drinkless.tdlib.TdApi.MessagePhoto;
import org.drinkless.tdlib.TdApi.MessageVoiceNote;
import org.drinkless.tdlib.TdApi.VoiceNote;
import org.drinkless.tdlib.TdApi.Messages;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

final class MainLayout extends LayoutBase implements ListArea.ClickHandler<Chat>, ConsoleArea.InputHandler, ConsoleArea.ClickHandler<Message>,
						     Objects.ChatsListener, Objects.NewMessageListener
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;
    static private final int CHAT_NUM_LIMIT = 500;

    private final App app;
    final ListArea<Chat> chatsArea;
    final ConsoleArea<Message> consoleArea;

    private List<Chat> chats = new ArrayList<>();
    private Chat activeChat = null;
    private Message[] messages = new Message[0];

    MainLayout(App app)
    {
	super(app);
	this.app = app;

	this.chatsArea = new ListArea<Chat>(listParams((params)->{
		    params.model = new ListUtils.ListModel<>(chats);
		    params.appearance = new ChatsListAppearance(app, params.context);
		    params.clickHandler = this;
		    params.name = app.getStrings().chatsAreaName();
		})){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case PROPERTIES:
			    return onChatProperties();
			}
		    return super.onSystemEvent(event);
		}
	    };

	final Actions chatsActions = actions(
					     action("contacts", app.getStrings().actionContacts(), new InputEvent(InputEvent.Special.F6), MainLayout.this::actContacts),
					     action("close-chat", app.getStrings().actionCloseChat(), new InputEvent(InputEvent.Special.DELETE), MainLayout.this::actCloseChat)
					     );

	this.consoleArea = new ConsoleArea<Message>(consoleParams((params)->{
		    params.model = new ConsoleUtils.ArrayModel<>(()->messages);
		    params.appearance = new MessageAppearance(app.getLuwrain(), app.getObjects());
		    params.name = "Беседа";
		    params.inputPos = ConsoleArea.InputPos.TOP;
		    params.inputPrefix = "";
		    params.clickHandler = this;
		    params.inputHandler = this;
		}));

	final Actions consoleActions = actions(
					       action("delete", "Удалить сообщение", new InputEvent(InputEvent.Special.DELETE), MainLayout.this::actDeleteMessage),
					       action("contacts", app.getStrings().actionContacts(), new InputEvent(InputEvent.Special.F6), MainLayout.this::actContacts)
					       );

	setAreaLayout(AreaLayout.LEFT_RIGHT, chatsArea, chatsActions, consoleArea, consoleActions);
	synchronized(app.getObjects()) {
	    app.getObjects().chatsListeners.add(this);
	    app.getObjects().newMessageListeners.add(this);
	}
    }

    @Override public boolean onListClick(ListArea listArea, int index, Chat chat)
    {
	NullCheck.notNull(chat, "chat");
	app.getOperations().openChat(chat, ()->{
		this.activeChat = chat;
		updateActiveChatHistory();
		consoleArea.setInputPrefix(chat.title + ">");
		consoleArea.reset(false);
		setActiveArea(consoleArea);
	    });
	return true;
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

    @Override public boolean onConsoleClick(ConsoleArea consoleArea, int index, Message message)
    {
	NullCheck.notNull(message, "message");
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
			app.getLuwrain().getPlayer().play(new org.luwrain.player.FixedPlaylist(new String[]{
				    org.luwrain.util.UrlUtils.fileToUrl(new java.io.File(audio.audio.audio.local.path))
			    }), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS, new Properties());
		return true;
		    }
		    app.getOperations().downloadFile(audio.audio.audio);
		    app.getLuwrain().message("Выполняется доставка файла");//FIXME:
		    return true;
	}

		if (message.content != null && message.content instanceof MessageDocument)
	{
	    final MessageDocument doc = (MessageDocument)message.content;
	    if (doc.document.document.local.isDownloadingActive)
		return false;
	    /*
	    	    if (audio.audio.audio.local.isDownloadingCompleted)
		    {
			if (audio.audio.audio.local.path == null || audio.audio.audio.local.path.isEmpty())
			    return false;
			if (app.getLuwrain().getPlayer() == null)
			    return false;
			app.getLuwrain().getPlayer().play(new org.luwrain.player.FixedPlaylist(new String[]{
				    org.luwrain.util.UrlUtils.fileToUrl(new java.io.File(audio.audio.audio.local.path))
			    }), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS, new Properties());
		return true;
		    }
	    */
		    app.getOperations().downloadFile(doc.document.document);
		    app.getLuwrain().message("Выполняется доставка файла");//FIXME:
		    return true;
	}

				if (message.content != null && message.content instanceof MessagePhoto)
	{
	    final MessagePhoto photo = (MessagePhoto)message.content;
	    if (photo.photo.sizes[3].photo.local.isDownloadingActive)
		return false;
		    app.getOperations().downloadFile(photo.photo.sizes[3].photo);
		    app.getLuwrain().message("Выполняется доставка файла");//FIXME:
		    return true;
	}

		if (message.content != null && message.content instanceof MessageVoiceNote)
	{
	    final MessageVoiceNote voiceNoteContent = (MessageVoiceNote)message.content;
	    final VoiceNote voiceNote = voiceNoteContent.voiceNote;
	    if (voiceNote.voice.local.isDownloadingActive)
		return false;
	    	    if (voiceNote.voice.local.isDownloadingCompleted)
		    {
			final LocalFile localFile = voiceNote.voice.local;
			if (localFile.path == null || localFile.path.isEmpty())
			    return false;
			if (app.getLuwrain().getPlayer() == null)
			    return false;
			app.getLuwrain().getPlayer().play(new org.luwrain.player.FixedPlaylist(new String[]{
				    org.luwrain.util.UrlUtils.fileToUrl(new java.io.File(localFile.path))
			    }), 0, 0, org.luwrain.player.Player.DEFAULT_FLAGS, new Properties());
		return true;
		    }
		    app.getOperations().downloadFile(voiceNote.voice);
		    app.getLuwrain().message("Выполняется доставка файла");//FIXME:
		    return true;
	}

	return false;
    }

    @Override public void onNewMessage(Chat chat, Message message)
    {
		if (message.content instanceof MessageText)
	{
		    final MessageText text = (MessageText)message.content;
		    getLuwrain().speak(text.text.text, Sounds.CHAT_MESSAGE);
		    return;
		}

	
    }

    @Override public void onChatsUpdate(Chat sourceChat)
    {
	final Objects objects = app.getObjects();
	final ArrayList<Chat> res = new ArrayList<>();
	synchronized(objects) {
	    res.ensureCapacity(objects.mainChats.size());
	    for(OrderedChat c: objects.mainChats)
	    {
		final Chat chat = objects.chats.get(c.chatId);
		if (chat != null)
		    res.add(chat);
	    }
	}
	chats.clear();
	chats.addAll(res);
	chatsArea.refresh();
    }

    private boolean actDeleteMessage()
    {
	if (activeChat == null)
	    return false;
	final Object obj = consoleArea.selected();
	if (obj == null || !(obj instanceof Message))
	    return false;
	app.getOperations().deleteMessage(activeChat, new Message[]{ (Message)obj}, ()->{
		app.getLuwrain().playSound(Sounds.OK);
	    });
	return true;
    }


    private void updateActiveChatHistory()
    {
	if (activeChat == null)
	    return;
	app.getOperations().getChatHistory(activeChat, (messagesChat, messages)->{
		final List<Message> res = new ArrayList<>();
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




    /*
    void activate()
    {
			app.getOperations().fillMainChatList(CHAT_NUM_LIMIT);
	setActiveArea(chatsArea);
    }
    */
}
