//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

final class Conversations
{
    private final App app;
    private final Luwrain luwrain;
    private final Strings strings;

    Conversations(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    String newContactFirstName()
    {
	return Popups.textNotEmpty(luwrain, "Новый контакт", "Имя человека:", "");
    }

    String newContactLastName()
    {
	return Popups.textNotEmpty(luwrain, "Новый контакт", "Фамилия:", "");
    }

    String newContactPhone()
    {
	final String res = Popups.text(luwrain, "Новый контакт", "Телефон нового контакта::", "", (text)->{
		NullCheck.notNull(text, "text");
		final String phone = properPhoneValue(text);
		if (phone.length() < 2)
		{
		    luwrain.message("Введённое значение не является правильным номером телефона", Luwrain.MessageType.ERROR);
		    return false;
		}
		return true;
	    });
	if (res != null)
	    return properPhoneValue(res);
	return null;
    }

    private String properPhoneValue(String str)
    {
	NullCheck.notNull(str, "str");
	final StringBuilder b = new StringBuilder();
	for(int i = 0;i < str.length();i++)
	    if (Character.isDigit(str.charAt(i)))
		b.append(str.charAt(i));
	return new String(b);
    }
}
