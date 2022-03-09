//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.controls.ConsoleArea.*;
import org.luwrain.controls.ConsoleUtils.*;
import org.luwrain.app.base.*;

import static org.luwrain.core.DefaultEventResponse.*;

final class SearchChatsLayout extends LayoutBase implements InputHandler
{
    static private final String
	LOG_COMPONENT = Core.LOG_COMPONENT;

    private final App app;
    final ConsoleArea<Chat> searchArea;
    private final List<Chat> items = new ArrayList<>();

    SearchChatsLayout(App app)
    {
	super(app);
	this.app = app;
	this.searchArea = new ConsoleArea<>(consoleParams((params)->{
		    params.name = "Поиск  групп и каналов";
		    params.model = new ListModel<>(items);
		    params.appearance = new Appearance();
		    params.inputHandler = this;
		    params.inputPrefix = "ПОИСК>";
		}));
	setAreaLayout(searchArea, null);
    }

    @Override public InputHandler.Result onConsoleInput(ConsoleArea area, String text)
    {
	if (text.trim().isEmpty())
	    return InputHandler.Result.REJECTED;
	app.getOperations().searchChats(text.trim(), (chats)->{
		app.message("" + chats.totalCount);
		this.items.clear();
		for(long l: chats.chatIds)
		    items.add(app.getObjects().chats.get(l));
		searchArea.refresh();
		searchArea.reset(false);
	    });
	return InputHandler.Result.OK;
    }

    private final class Appearance implements ConsoleArea.Appearance<Chat>
    {
	@Override public void announceItem(Chat chat)
	{
	    app.setEventResponse(text(chat.title));
	}
	@Override public String getTextAppearance(Chat chat)
	{
	    return chat.title;
	}
    }
}
