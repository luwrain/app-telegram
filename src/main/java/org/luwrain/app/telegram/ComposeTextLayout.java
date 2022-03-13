//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

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

final class ComposeTextLayout extends LayoutBase
{
    private final App app;
    private final EditArea editArea;
    private final Chat chat;

    ComposeTextLayout(App app, Chat chat, ActionHandler closing, Runnable afterSending)
    {
	super(app);
	this.chat = chat;
	this.app = app;
	this.editArea = new EditArea(editParams((params)->{
		    params.name = app.getStrings().composeTextAreaName();
		}));
	setCloseHandler(closing);
	setOkHandler(()->onOk(closing, afterSending));
	setAreaLayout(editArea, null);
    }

    private boolean onOk(ActionHandler closing, Runnable afterSending)
    {
	final String[] lines = editArea.getText();
	if (lines.length == 0 ||
	    (lines.length == 1 && lines[0].trim().isEmpty()))
	{
	    app.message(app.getStrings().composedTextEmpty(), Luwrain.MessageType.ERROR);
	    return true;
	}
	final StringBuilder b = new StringBuilder();
	for(String l: lines)
	    b.append(l).append("\n");
	app.getOperations().sendTextMessage(chat, new String(b), afterSending);
	return closing.onAction();
    }
}
