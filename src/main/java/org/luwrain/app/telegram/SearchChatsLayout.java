//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi.User;
import org.drinkless.tdlib.TdApi.Contact;
import org.drinkless.tdlib.TdApi.UserStatusOnline;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.controls.ConsoleArea.*;
import org.luwrain.controls.ConsoleUtils.*;
import org.luwrain.app.base.*;

final class SearchChatsLayout extends LayoutBase implements InputHandler
{
    static private final String
	LOG_COMPONENT = Core.LOG_COMPONENT;

    private final App app;
    final ConsoleArea searchArea;
    private final List items = new ArrayList();

    SearchChatsLayout(App app)
    {
	super(app);
	this.app = app;
	this.searchArea = new ConsoleArea(consoleParams((params)->{
		    params.name = "Поиск  групп и каналов";
		    params.model = new ListModel(items);
		    params.inputHandler = this;
		}));
	setAreaLayout(searchArea, null);
    }

    @Override public InputHandler.Result onConsoleInput(ConsoleArea area, String text)
    {
	return InputHandler.Result.OK;
    }

    }
