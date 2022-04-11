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
import org.luwrain.controls.*;

import static org.luwrain.core.DefaultEventResponse.*;

public final class UserAppearance
{
    private final Luwrain luwrain;
    private final Objects objects;

    UserAppearance(Luwrain luwrain, Objects objects)
    {
		NullCheck.notNull(luwrain, "luwrain");
	NullCheck.notNull(objects, "objects");
	this.luwrain = luwrain;
	this.objects = objects;
    }

    public void announce(User user)
    {
	final StringBuilder b = new StringBuilder();
	if (user.firstName != null && !user.firstName.isEmpty())
	    b.append(user.firstName).append(" ");
		if (user.lastName != null && !user.lastName.isEmpty())
	    b.append(user.lastName);
	luwrain.setEventResponse(listItem(Sounds.LIST_ITEM, new String(b).trim(), null));
    }

public String getTextAppearance(User user)
    {
		final StringBuilder b = new StringBuilder();
	if (user.firstName != null && !user.firstName.isEmpty())
	    b.append(user.firstName).append(" ");
		if (user.lastName != null && !user.lastName.isEmpty())
	    b.append(user.lastName);
			return new String(b);
    }


    static public final class ForList extends ListUtils.AbstractAppearance<User>
    {
	private final UserAppearance appearance;
	public ForList(App app) { this.appearance = new UserAppearance(app.getLuwrain(), app.getObjects()); }
	@Override public void announceItem(User user, Set<Flags> flags) { appearance.announce(user); }
	@Override public String getScreenAppearance(User user, Set<Flags> flags) { return appearance.getTextAppearance(user); }
    }
}
