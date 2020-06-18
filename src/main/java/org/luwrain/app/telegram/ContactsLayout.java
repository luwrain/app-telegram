//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
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
import org.luwrain.template.*;

final class ContactsLayout extends LayoutBase implements ListArea.ClickHandler, Objects.UsersListener
{
    static private final String LOG_COMPONENT = App.LOG_COMPONENT;

    private final App app;
    private final ListArea contactsArea;

    private Contact[] contacts = new Contact[0];

    ContactsLayout(App app)
    {
	NullCheck.notNull(app, "app");
	this.app = app;
	this.contactsArea = new ListArea(createContactsParams()){
		private final Actions actions = actions(
							action("chats", app.getStrings().actionChats(), new KeyboardEvent(KeyboardEvent.Special.F5), ContactsLayout.this::actChats),
																												action("new-contact", app.getStrings().actionNewContact(), new KeyboardEvent(KeyboardEvent.Special.INSERT), ContactsLayout.this::actNewContact)
							);
		@Override public boolean onInputEvent(KeyboardEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onInputEvent(this, event))
			return true;
		    return super.onInputEvent(event);
		}
		@Override public boolean onSystemEvent(EnvironmentEvent event)
		{
		    NullCheck.notNull(event, "event");
		    if (app.onSystemEvent(this, event, actions))
			return true;
		    return super.onSystemEvent(event);
		}
		@Override public boolean onAreaQuery(AreaQuery query)
		{
		    NullCheck.notNull(query, "query");
		    if (app.onAreaQuery(this, query))
			return true;
		    return super.onAreaQuery(query);
		}
		@Override public Action[] getAreaActions()
		{
		    return actions.getAreaActions();
		}
	    };
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

    private boolean actChats()
    {
	app.layouts().main();
	return true;
    }

    @Override public boolean onListClick(ListArea listArea, int index, Object obj)
    {
	if (obj == null)
	    return false	;
		return true;
    }

    void updateContactsList()
    {
	app.getOperations().getContacts(()->{
		final List<Contact> res = new LinkedList();
		for(int u: app.getObjects().getContacts())
		{
		    final User user = app.getObjects().users.get(u);
		    if (user == null)
			continue;
		    res.add(new Contact(user.phoneNumber, user.firstName, user.lastName, "", user.id));
		}
		this.contacts = res.toArray(new Contact[res.size()]);
		contactsArea.refresh();
	    });
    }

    @Override public void onUsersUpdate(User user)
    {
    }

    private ListArea.Params createContactsParams()
    {
	final ListArea.Params params = new ListArea.Params();
	params.context = new DefaultControlContext(app.getLuwrain());
	params.model = new ContactsModel();
	params.appearance = new ContactsAppearance();
	params.clickHandler = this;
	params.name = app.getStrings().contactsAreaName();
	return params;
    }

    AreaLayout getLayout()
    {
	return new AreaLayout(contactsArea);
    }

    private final class ContactsModel implements ListArea.Model
    {
	@Override public int getItemCount()
	{
	    return contacts.length;
	}
	@Override public Object getItem(int index)
	{
	    return contacts[index];
	}
	@Override public void refresh()
	{
	}
    }

    private final class ContactsAppearance implements ListArea.Appearance
    {
	@Override public void announceItem(Object item, Set<Flags> flags)
	{
	    	    NullCheck.notNull(item, "item");
		    NullCheck.notNull(flags, "flags");
		    if (item instanceof Contact)
		    {
			final Contact contact = (Contact)item;
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
											getContactTitle(contact),
											Suggestions.CLICKABLE_LIST_ITEM));
			return;
		    }
		    app.getLuwrain().setEventResponse(DefaultEventResponse.listItem(item.toString()));
	}
	@Override public String getScreenAppearance(Object item, Set<Flags> flags)
	{
	    NullCheck.notNull(item, "item");
	    NullCheck.notNull(flags, "flags");
	    if (item instanceof Contact)
		return getContactTitle((Contact)item);
	    return item.toString();
	}
	@Override public int getObservableLeftBound(Object item)
	{
	    	    NullCheck.notNull(item, "item");
	    return 0;
	}
	@Override public int getObservableRightBound(Object item)
	{
	    	    NullCheck.notNull(item, "item");
	    return getScreenAppearance(item, EnumSet.noneOf(Flags.class)).length();
	}
	private String getContactTitle(Contact contact)
	{
	    NullCheck.notNull(contact, "contact");
	    						final StringBuilder b = new StringBuilder();
			if (contact.firstName != null)
			    b.append(contact.firstName);
			b.append(" ");
			if (contact.lastName != null)
			    b.append(contact.lastName);
			return new String(b).trim();
	}
    }
    }