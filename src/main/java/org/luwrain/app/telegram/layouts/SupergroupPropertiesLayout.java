//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram.layouts;

import java.util.*;

import org.drinkless.tdlib.*;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.app.telegram.*;

public final class SupergroupPropertiesLayout extends LayoutBase
{
    static private final String
	LOG_COMPONENT = Core.LOG_COMPONENT,
	EDIT_DESCR = "description",
	EDIT_USERNAME = "username";

    private final App app;
    private final FormArea formArea;
    private final Chat chat;
    private final Supergroup supergroup;
    private final SupergroupFullInfo fullInfo;

    public SupergroupPropertiesLayout(App app, Chat chat, Supergroup supergroup, SupergroupFullInfo fullInfo, ActionHandler closing)
    {
	super(app);
	this.app = app;
	this.chat = chat;
	this.supergroup = supergroup;
	this.fullInfo = fullInfo;

	this.formArea = new FormArea(getControlContext(), chat.title);
	setCloseHandler(closing);
	setOkHandler(this::onOk);

	formArea.addStatic("Title: " + chat.title);
	if (supergroup.isChannel)
	    formArea.addStatic("Тип: канал"); else
	    formArea.addStatic("Тип: группа");
		    formArea.addEdit(EDIT_DESCR, "Описание: ", fullInfo.description);
	if (fullInfo.canSetUsername)
	    formArea.addEdit(EDIT_USERNAME, "User name: ", supergroup.username); else
	formArea.addStatic("Username: " + supergroup.username);
	formArea.addStatic("Description: " + fullInfo.description);
	formArea.addStatic("Member count: " + supergroup.memberCount);
	formArea.addStatic("Blocked: " + chat.isBlocked);
	if (fullInfo.inviteLink != null)
	    formArea.addStatic("Invite link: " + fullInfo.inviteLink.inviteLink);
	setAreaLayout(formArea, null);
    }

    private boolean onOk()
    {

		if (!formArea.getEnteredText(EDIT_DESCR).equals(fullInfo.description))
	    app.getOperations().callFunc(new SetChatDescription(chat.id, formArea.getEnteredText(EDIT_DESCR)), Ok.CONSTRUCTOR, (res)->{});

		
	if (fullInfo.canSetUsername && !formArea.getEnteredText(EDIT_USERNAME).equals(supergroup.username))
	    app.getOperations().callFunc(new SetSupergroupUsername(supergroup.id, formArea.getEnteredText(EDIT_USERNAME)), Ok.CONSTRUCTOR, (res)->{});
	return true;
	 }
}
