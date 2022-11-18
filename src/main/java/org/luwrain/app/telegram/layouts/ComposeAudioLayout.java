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

public final class ComposeAudioLayout extends ComposeLayoutBase
{
    static private String
	TITLE = "title",
	AUTHOR = "author",
	FILE = "file";

    public ComposeAudioLayout(App app, Chat chat, Message message, ActionHandler closing, Runnable afterSending)
    {
	super(app, chat, message, closing, afterSending);
	formArea.addEdit(FILE, app.getStrings().composeAudioFile(), "");
	formArea.addEdit(AUTHOR, app.getStrings().composeAudioAuthor(), "");
	formArea.addEdit(TITLE, app.getStrings().composeAudioTitle(), "");
	formArea.activateMultilineEdit(app.getStrings().composeAudioComment(), new String[0]);
	setAreaLayout(formArea, null);
    }

    @Override protected boolean onOk(ActionHandler closing, Runnable afterSending)
    {
	final java.io.File file = new java.io.File(formArea.getEnteredText(FILE));
	final String
	text = concatText(getText(formArea.getMultilineEditText())),
	author = formArea.getEnteredText(AUTHOR),
	title = formArea.getEnteredText(TITLE);
	if (text == null)
	{
	    app.message(app.getStrings().composedTextEmpty(), Luwrain.MessageType.ERROR);
	    return true;
	}
	app.getOperations().sendAudioMessage(chat, file, "Чем профессиональная фортепианная игра отличается от гениальной? Три предельно необъективных сравнения.\n\n#НеПоДелу #фортепиано", author, title, afterSending);
	return closing.onAction();
    }
}
