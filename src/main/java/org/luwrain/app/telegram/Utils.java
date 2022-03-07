//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi.Contact;

import org.luwrain.core.*;
import org.luwrain.controls.*;

final class Utils
{
    static String getContactTitle(Contact contact)
    {
	NullCheck.notNull(contact, "contact");
	final StringBuilder b = new StringBuilder();
	if (contact.lastName != null)
	    b.append(contact.lastName);
	if (contact.firstName != null)
	{
	    if (b.length() > 0)
		b.append(" ");
	    b.append(contact.firstName);
	}
	return new String(b).trim();
    }
}
