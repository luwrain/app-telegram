//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram.props;

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
	LOG_COMPONENT = Core.LOG_COMPONENT;

    private final App app;
    final FormArea formArea;

    public SupergroupPropertiesLayout(App app, Chat chat, Supergroup supergroup, SupergroupFullInfo fullInfo, ActionHandler closing)
    {
	super(app);
	this.app = app;

	this.formArea = new FormArea(getControlContext(), chat.title);
	setCloseHandler(closing);

	formArea.addStatic("Title: " + chat.title);
	if (supergroup.isChannel)
	    formArea.addStatic("Type: channel"); else
	    formArea.addStatic("Type: group");
	formArea.addStatic("Username: " + supergroup.username);
	formArea.addStatic("Description: " + fullInfo.description);
	formArea.addStatic("Member count: " + supergroup.memberCount);
	formArea.addStatic("Blocked: " + chat.isBlocked);
	if (fullInfo.inviteLink != null)
	    formArea.addStatic("Invite link: " + fullInfo.inviteLink.inviteLink);
	setAreaLayout(formArea, null);
    }
}
