//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.drinkless.tdlib.TdApi.User;
import org.drinkless.tdlib.TdApi.Contact;
import org.drinkless.tdlib.TdApi.UserStatusOnline;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.controls.ListArea.*;
import org.luwrain.controls.ListUtils.*;
import org.luwrain.app.base.*;

final class ContactsLayout extends LayoutBase implements ClickHandler<Contact>, Objects.UsersListener
{
    static private final String
	LOG_COMPONENT = Core.LOG_COMPONENT;

    private final App app;
    final ListArea<Contact> contactsArea;
    private List<Contact> contacts = new ArrayList<>();

    ContactsLayout(App app)
    {
	super(app);
	this.app = app;
	this.contactsArea = new ListArea<>(listParams((params)->{
		    params.model = new ListModel<>(contacts);
		    params.appearance = new ContactsAppearance();
		    params.clickHandler = this;
		    params.name = app.getStrings().contactsAreaName();
		}));
	setAreaLayout(contactsArea, actions(
					    action("new-contact", app.getStrings().actionNewContact(), new InputEvent(InputEvent.Special.INSERT), ContactsLayout.this::actNewContact),
					    action("main-chats", app.getStrings().actionMainChats(), app.HOTKEY_MAIN, app.layouts()::main),
					    action(app.getStrings().actionSearchChats(), "search-chats", App.HOTKEY_SEARCH_CHATS, app.layouts()::searchChats)
					    ));
	synchronized(app.getObjects()) {
	    app.getObjects().usersListeners.add(this);
	}
    }

    private boolean actNewContact()
    {
	final String phone = app.getConv().newContactPhone();
	if (phone == null)
	    return true;
	final String firstName = app.getConv().newContactFirstName();
	if (firstName == null)
	    return true;
	final String lastName = app.getConv().newContactLastName();
	if (lastName == null)
	    return true;
	app.getOperations().addContact(phone, firstName, lastName, this::updateContactsList);
	return true;
    }

    @Override public boolean onListClick(ListArea listArea, int index, Contact contact)
    {
	app.getOperations().createPrivateChat(contact.userId, (chat)->{
		final ComposeTextLayout compose = new ComposeTextLayout(app, chat, null, ()->{
			app.setAreaLayout(ContactsLayout.this);
			getLuwrain().announceActiveArea();
			return true;
		    }, ()->getLuwrain().playSound(Sounds.DONE));
		app.setAreaLayout(compose);
		getLuwrain().announceActiveArea();
	    });
	return true;
    }

    void updateContactsList()
    {
	app.getOperations().getContacts(()->{
		final List<Contact> res = new ArrayList<>();
		synchronized(app.getObjects()) {
		    for(long u: app.getObjects().getContacts())
		    {
			final User user = app.getObjects().users.get(u);
			if (user == null)
			    continue;
			res.add(new Contact(user.phoneNumber, user.firstName, user.lastName, "", user.id));
		    }
		};
		this.contacts.clear();
		this.contacts.addAll(res);
		Collections.sort(contacts, new ContactsComparator());
		contactsArea.refresh();
	    });
    }

    @Override public void onUsersUpdate(User user)
    {
	//FIXME:
    }

    private final class ContactsAppearance extends AbstractAppearance<Contact>
    {
	@Override public void announceItem(Contact contact, Set<Flags> flags)
	{
	    final boolean online;
	    final User user = app.getObjects().users.get(contact.userId);
	    if (user != null)
	    {
		if (user.status != null && user.status instanceof UserStatusOnline)
		    online = true; else
		    online = false;
	    } else
		online = false;
	    app.getLuwrain().setEventResponse(DefaultEventResponse.listItem(
									    online?Sounds.SELECTED:Sounds.LIST_ITEM,
									    Utils.getContactTitle(contact),
									    Suggestions.CLICKABLE_LIST_ITEM));
	    return;
	}
	@Override public String getScreenAppearance(Contact contact, Set<Flags> flags)
	{
	    return Utils.getContactTitle(contact);
	}
    }

    static private final class ContactsComparator implements Comparator<Contact>
    {
	@Override public int compare(Contact c1, Contact c2)
	{
	    return Utils.getContactTitle(c1).compareTo(Utils.getContactTitle(c2));
	}
    }
}
