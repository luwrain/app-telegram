//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram.layouts;

import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.app.telegram.*;

public final class ComposePhotoLayout extends ComposeLayoutBase
{
    static private String
	FILE = "file";

    public ComposePhotoLayout(App app, Chat chat, Message message, ActionHandler closing, Runnable afterSending)
    {
	super(app, chat, message, closing, afterSending);
	formArea.addEdit(FILE, app.getStrings().composePhotoFile(), "");
	formArea.activateMultilineEdit(app.getStrings().composePhotoComment(), new String[0]);
	setAreaLayout(formArea, null);
    }

    @Override protected boolean onOk(ActionHandler closing, Runnable afterSending)
    {
	final java.io.File file = new java.io.File(formArea.getEnteredText(FILE));
	final String text = concatText(getText(formArea.getMultilineEditText()));
	if (text == null)
	{
	    app.message(app.getStrings().composedTextEmpty(), Luwrain.MessageType.ERROR);
	    return true;
	}
	app.getOperations().sendPhotoMessage(chat, file, text, afterSending);
	return closing.onAction();
    }
}
