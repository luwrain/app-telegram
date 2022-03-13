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

    ComposeTextLayout(App app, ActionHandler closing)
    {
	super(app);
	this.app = app;
	this.editArea = new EditArea(editParams((params)->{
		    params.name = app.getStrings().composeTextAreaName();
		}));
	setCloseHandler(closing);
	setOkHandler(()->onOk(closing));
	setAreaLayout(editArea, null);
    }

    private boolean onOk(ActionHandler closing)
    {
	return closing.onAction();
    }
    
}
