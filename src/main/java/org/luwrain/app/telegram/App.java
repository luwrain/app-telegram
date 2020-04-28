//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2020
// Copyright 2020 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;
import java.io.*;

import org.drinkless.tdlib.*;
import org.luwrain.core.Log;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.core.queries.*;
import org.luwrain.controls.*;
import org.luwrain.template.*;

final class App extends AppBase<Strings> implements MonoApp
{
    static final String LOG_COMPONENT = "telegram";

    private Conversations conv = null;
    private File tdlibDir = null;
    private Operations operations = null;
    private Objects objects = null;
    private MainLayout mainLayout = null;
    private AuthLayout authLayout = null;
    private Client client = null;

    App()
    {
	super(Strings.NAME, Strings.class);
    }

    @Override public boolean onAppInit()
    {
	this.tdlibDir = new File(getLuwrain().getAppDataDir("luwrain.telegram").toFile(), "tdlib");
	this.objects = new Objects(this);
	this.operations = newOperations();
	this.mainLayout = new MainLayout(this);
	this.authLayout = new AuthLayout(this);
	this.client = Client.create(newResultHandler(), null, null); // recreate client after previous has closed
	setAppName(getStrings().appName());
	return true;
    }

    boolean onInputEvent(Area area, KeyboardEvent event, Runnable closing)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(event, "event");
	if (super.onInputEvent(area, event))
	    return true;
	if (event.isSpecial())
	    switch(event.getSpecial())
	    {
	    case ESCAPE:
		if (closing != null)
		    closing.run(); else
		closeApp();
		return true;
	    }
	return false;
    }

    @Override public boolean onInputEvent(Area area, KeyboardEvent event)
    {
	NullCheck.notNull(area, "area");
	NullCheck.notNull(event, "event");
	return onInputEvent(area, event, null);
    }

        Conversations conv()
    {
	return this.conv;
    }

    Objects getObjects()
    {
	return this.objects;
    }

    Operations getOperations()
    {
	return this.operations;
    }

    private Client.ResultHandler newResultHandler()
    {
	return new UpdatesHandler(tdlibDir, objects){
	    @Override public void onReady()
	    {
		getLuwrain().runUiSafely(()->{
		getLayout().setBasicLayout(App.this.mainLayout.getLayout());
		App.this.mainLayout.activate();
		    });
	    }
	    @Override Client getClient()
	    {
		if (App.this.client == null)
		    Log.warning(LOG_COMPONENT, "providing a null pointer as a client to the updates handler");
		return App.this.client;
	    }
	};
    }

        private Operations newOperations()
    {
	return new Operations(this){
	    @Override Client getClient()
	    {
		if (App.this.client == null)
		    Log.warning(LOG_COMPONENT, "providing a null pointer as a client to operations");
		return App.this.client;
	    }
	};
    }



    Layouts layouts()
    {
	return new Layouts(){
	    @Override public void main()
	    {
		getLayout().setBasicLayout(mainLayout.getLayout());
	    }
	};
    }

    @Override public AreaLayout getDefaultAreaLayout()
    {
	return this.authLayout.getLayout();
    }

    @Override public void closeApp()
    {
	if (client != null)
	    client.close();
	super.closeApp();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }


}
