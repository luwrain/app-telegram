//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.controls.*;
import org.luwrain.popups.Popups;

final class Conv
{
    private final App app;
    private final Luwrain luwrain;
    private final Strings strings;

    Conv(App app)
    {
	this.app = app;
	this.luwrain = app.getLuwrain();
	this.strings = app.getStrings();
    }

    String newChannelTitle() { return Popups.textNotEmpty(luwrain, strings.newChannelPopupName(), strings.newChannelTitlePopupPrefix(), ""); }
    String newChannelDescr() { return Popups.text(luwrain, strings.newChannelPopupName(), strings.newChannelDescrPopupPrefix(), ""); }
    boolean confirmChatDeleting(Chat chat) { return Popups.confirmDefaultNo(luwrain, strings.chatDeletingPopupName(), strings.chatDeletingPopupText(chat.title)); }
    boolean leaveChatConfirm() { return Popups.confirmDefaultNo(luwrain, "Отписка", "Вы действительно хотите покинуть чат?"); }
    String userName(){ return Popups.text(luwrain, strings.userNamePopupName(), strings.userNamePopupPrefix(), ""); }
    String newContactFirstName() { return Popups.textNotEmpty(luwrain, "Новый контакт", "Имя человека:", ""); }
    String newContactLastName() { return Popups.textNotEmpty(luwrain, "Новый контакт", "Фамилия:", ""); }

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
	final StringBuilder b = new StringBuilder();
	for(int i = 0;i < str.length();i++)
	    if (Character.isDigit(str.charAt(i)))
		b.append(str.charAt(i));
	return new String(b);
    }
}
