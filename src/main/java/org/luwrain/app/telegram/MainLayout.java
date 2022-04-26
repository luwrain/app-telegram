//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.*;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;

import org.luwrain.app.telegram.layouts.*;

final class MainLayout extends LayoutBase implements ListArea.ClickHandler<Chat>, ConsoleArea.InputHandler, ConsoleArea.ClickHandler<Message>,
						     Objects.ChatsListener, Objects.NewMessageListener
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;
    static private final int CHAT_NUM_LIMIT = 500;

    private final App app;
    final ListArea<Chat> chatsArea;
    final ConsoleArea<Message> consoleArea;

    private ArrayList<Chat> chats = new ArrayList<>();
    private Chat activeChat = null;
    private Message[] messages = new Message[0];

    MainLayout(App app)
    {
	super(app);
	this.app = app;

	chats.ensureCapacity(app.getObjects().mainChats.size());
	for(OrderedChat o: app.getObjects().mainChats)
	{
	    final Chat c = app.getObjects().chats.get(o.chatId);
	    if (c != null)
		chats.add(c);
	}

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

	this.consoleArea = new ConsoleArea<Message>(consoleParams((params)->{
		    params.model = new ConsoleUtils.ArrayModel<>(()->messages);
		    params.appearance = new MessageAppearance(app.getLuwrain(), app.getObjects());
		    params.name = "Беседа";
		    params.inputPos = ConsoleArea.InputPos.TOP;
		    params.inputPrefix = "";
		    params.clickHandler = this;
		    params.inputHandler = this;
		}));

	final ActionInfo
	searchChatsAction = 					     action("search-chats", "Поиск групп и каналов", App.HOTKEY_SEARCH_CHATS, app.layouts()::searchChats),
	contactsAction = action("contacts", app.getStrings().actionContacts(), App.HOTKEY_CONTACTS, app.layouts()::contacts);

	setAreaLayout(AreaLayout.LEFT_RIGHT, chatsArea, actions(
								action("set-chat-photo", app.getStrings().actionSetChatPhoto(), this::actSetChatPhoto),
								action("chat-members", app.getStrings().actionChatMembers(), this::actChatMembers),
								action("delete-chat", app.getStrings().actionDeleteChat(), this::actDeleteChat),
								action("new-channel", app.getStrings().actionNewChannel(), this::actNewChannel),
								searchChatsAction, contactsAction),

		      consoleArea, actions(
					   action("edit-message-text", app.getStrings().actionEditMessageText(), new InputEvent(InputEvent.Special.F5, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actEditMessageText),
					   action("compose-text", app.getStrings().actionComposeText(), new InputEvent(InputEvent.Special.INSERT), MainLayout.this::actComposeText),
					   action("send-photo", app.getStrings().actionSendPhoto(), new InputEvent(InputEvent.Special.INSERT, EnumSet.of(InputEvent.Modifiers.CONTROL)), MainLayout.this::actSendPhoto),
					   					   action("send-audio", app.getStrings().actionSendAudio(), new InputEvent(InputEvent.Special.INSERT, EnumSet.of(InputEvent.Modifiers.SHIFT)), MainLayout.this::actSendAudio),
					   action("delete-message", app.getStrings().actionDeleteMessage(), new InputEvent(InputEvent.Special.DELETE), MainLayout.this::actDeleteMessage),

					   searchChatsAction, contactsAction)
		      );
	synchronized(app.getObjects()) {
	    app.getObjects().chatsListeners.add(this);
	    app.getObjects().newMessageListeners.add(this);
	}
    }

    @Override public boolean onListClick(ListArea listArea, int index, Chat chat)
    {
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
	app.getOperations().sendTextMessage(activeChat, text, ()->{
		consoleArea.setInput("");
		updateActiveChatHistory();
		getLuwrain().playSound(Sounds.DONE);
	    });
	return ConsoleArea.InputHandler.Result.OK;
    }

    private boolean actDeleteChat()
    {
	final Chat chat = chatsArea.selected();
	if (chat == null)
	    return false;
	if (!app.getConv().confirmChatDeleting(chat))
	    return true;
	app.getOperations().callFunc(new DeleteChat(chat.id), Ok.CONSTRUCTOR, (res)->{
		getLuwrain().playSound(Sounds.OK);
	    });
	return true;
    }

        private boolean actSetChatPhoto()
    {
	final Chat chat = chatsArea.selected();
	if (chat == null)
	    return false;
	app.getOperations().callFunc(new SetChatPhoto(chat.id, new InputChatPhotoStatic(new InputFileLocal(""))), Ok.CONSTRUCTOR, (res)->{
		getLuwrain().playSound(Sounds.OK);
	    });
	return true;
    }

            private boolean actChatMembers()
    {
	final Chat chat = chatsArea.selected();
	if (chat == null)
	    return false;
	/*

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
			org.luwrain.core.Log.debug(LOG_COMPONENT, user.firstName + " " + user.lastName);
			}
		    }
		});
	    return true;
	}
		return false;
	*/
	final ChatMembersLayout layout = new ChatMembersLayout(app, chat, ()->{
		app.setAreaLayout(this);
		setActiveArea(chatsArea);
		return true;
	    });
	app.setAreaLayout(layout);
	layout.showFirst();
	return true;
	
		    }


    private boolean actEditMessageText()
    {
	final Message message = consoleArea.selected();
	if (message == null || activeChat == null)
	    return false;
	if (message.content == null || !(message.content instanceof MessageText))
	    return false;
	final ComposeTextLayout compose = new ComposeTextLayout(app, activeChat, message, ()->{
		app.setAreaLayout(this);
		getLuwrain().announceActiveArea();
		return true;
	    }, ()->{
				updateActiveChatHistory();
		getLuwrain().playSound(Sounds.DONE);
	    });
	app.setAreaLayout(compose);
	getLuwrain().announceActiveArea();

	return true;
    }

    @Override public boolean onConsoleClick(ConsoleArea consoleArea, int index, Message message)
    {
	if (activeChat == null)
	    return false;
	final MessageClicks clicks = new MessageClicks(app, this);
	if (message.content instanceof MessagePinMessage)
	{
	    final MessagePinMessage pin = (MessagePinMessage)message.content;
	    app.getOperations().callFunc(new GetMessage(activeChat.id, pin.messageId), Message.CONSTRUCTOR, (res)->{
		    clicks.onMessageClick((Message)res);
		});
	    return true;
	}
	return clicks.onMessageClick(message);
    }

    private boolean actNewChannel()
    {
	final String title = app.getConv().newChannelTitle();
	if (title == null)
	    return true;
	final String descr = app.getConv().newChannelDescr();
	if (descr == null)
	    return true;
	app.getOperations().createSupergroupChat(title, descr, true, (chat)->{
		app.message(app.getStrings().channelCreated(chat.title), Luwrain.MessageType.OK);
	    });
	return true;
    }

    
    private boolean actDeleteMessage()
    {
	if (activeChat == null)
	    return false;
	final Message message = consoleArea.selected();
	if (message == null)
	    return false;
	app.getOperations().deleteMessage(activeChat, new Message[]{ message}, ()->{
		app.getLuwrain().playSound(Sounds.OK);
	    });
	return true;
    }

    private boolean actComposeText()
    {
	if (activeChat == null)
	    return false;
	final ComposeTextLayout compose = new ComposeTextLayout(app, activeChat, null, ()->{
		app.setAreaLayout(MainLayout.this);
		getLuwrain().announceActiveArea();
		return true;
	    }, ()->{
		updateActiveChatHistory();
		getLuwrain().playSound(Sounds.DONE);
	    });
	app.setAreaLayout(compose);
	getLuwrain().announceActiveArea();
	return true;
    }

        private boolean actSendPhoto()
    {
	if (activeChat == null)
	    return false;
	app.getOperations().sendPhotoMessage(activeChat, new java.io.File("/tmp/transformer/decoder.jpg"), "А это декодер.\n#NLP #лингвистика ", ()->{});
	return true;
	/*
	final ComposeTextLayout compose = new ComposeTextLayout(app, activeChat, null, ()->{
		app.setAreaLayout(MainLayout.this);
		getLuwrain().announceActiveArea();
		return true;
	    }, ()->{
		updateActiveChatHistory();
		getLuwrain().playSound(Sounds.DONE);
	    });
	app.setAreaLayout(compose);
	getLuwrain().announceActiveArea();
	return true;
	*/
    }

            private boolean actSendAudio()
    {
	if (activeChat == null)
	    return false;
	app.getOperations().sendAudioMessage(activeChat, new java.io.File("/tmp/Nocturne.mp3"), "Этому меланхолическому фортепианному размышлению уже несколько лет, но так и не может перерасти в полноценную пьесу.\n#музыка #фортепиано #неоклассика", "Michael Pozhidaev", "Nocturne", ()->{});
	return true;
	/*
	final ComposeTextLayout compose = new ComposeTextLayout(app, activeChat, null, ()->{
		app.setAreaLayout(MainLayout.this);
		getLuwrain().announceActiveArea();
		return true;
	    }, ()->{
		updateActiveChatHistory();
		getLuwrain().playSound(Sounds.DONE);
	    });
	app.setAreaLayout(compose);
	getLuwrain().announceActiveArea();
	return true;
	*/
    }



    private boolean actStat()
    {
	final Chat chat = chatsArea.selected();
	if (chat == null)
	    return false;
	app.getOperations().callFunc(new GetChatStatistics(chat.id, false), ChatStatisticsChannel.CONSTRUCTOR, (res)->{
		final ChatStatisticsLayout stat = new ChatStatisticsLayout(app, chat, (ChatStatistics)res, ()->{
			app.setAreaLayout(this);
			getLuwrain().announceActiveArea();
			return true;
		    });
		app.setAreaLayout(stat);
		getLuwrain().announceActiveArea();
		
	    });

	return true;
	    }

	

            private boolean actCloseChat()
    {
	final Chat chat = chatsArea.selected();
	if (chat == null)
	    return false;
	app.getOperations().leaveChat(chat, ()->app.getLuwrain().playSound(Sounds.OK));
	return true;
    }

    private boolean onChatProperties()
    {
	final Chat chat = chatsArea.selected();
	if (chat == null)
	    return false;

	if (chat.type instanceof ChatTypeSupergroup)
	{
	    final long supergroupId = ((ChatTypeSupergroup)chat.type).supergroupId;
	    final Supergroup supergroup = app.getObjects().supergroups.get(supergroupId);
	    if (supergroup == null)
	    {
		org.luwrain.core.Log.error(LOG_COMPONENT, "no supergroup in objects, id = " + supergroupId);
		return false;
	    }
	    app.getOperations().callFunc(new GetSupergroupFullInfo(supergroupId), SupergroupFullInfo.CONSTRUCTOR, (obj)->{
		    final SupergroupFullInfo fullInfo = (SupergroupFullInfo)obj;
		    final SupergroupPropertiesLayout props = new SupergroupPropertiesLayout(app, chat, supergroup, fullInfo, ()->{
			    app.setAreaLayout(MainLayout.this);
			    getLuwrain().announceActiveArea();
			    return true;
			});
		    app.setAreaLayout(props);
		    getLuwrain().announceActiveArea();
		});
	    return true;
	}

	return false;
	
	/*
	final ChatPropertiesLayout layout = new ChatPropertiesLayout(app, chat, ()->app.layouts().main());
	app.layout(layout.getLayout());
	return true;
	*/
    }


    @Override public void onNewMessage(Chat chat, Message message)
    {
	if (chat.type != null && chat.type instanceof ChatTypeSupergroup)
	    return;
		if (message.content instanceof MessageText)
	{
		    final MessageText text = (MessageText)message.content;
		    getLuwrain().speak(text.text.text, Sounds.CHAT_MESSAGE);
		    return;
		}
    }

    @Override public void onChatsUpdate(Chat sourceChat)
    {
	org.luwrain.core.Log.debug(LOG_COMPONENT, "updating main chat list");
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
}
