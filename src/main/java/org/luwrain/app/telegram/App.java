//
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
import org.luwrain.app.base.*;

public final class App extends AppBase<Strings> implements MonoApp
{
    static final String LOG_COMPONENT = "telegram";

    private Conversations conv = null;
    private File tdlibDir = null;
    private Operations operations = null;
    private Objects objects = null;
    private MainLayout mainLayout = null;
    private ContactsLayout contactsLayout = null;
    private AuthLayout authLayout = null;
    private Client client = null;

    public App()
    {
	super(Strings.NAME, Strings.class, "luwrain.telegram");
    }

    @Override public AreaLayout onAppInit()
    {
	this.conv = new Conversations(this);
	this.tdlibDir = new File(getLuwrain().getAppDataDir("luwrain.telegram").toFile(), "tdlib");
	this.objects = new Objects(this);
	this.operations = newOperations();
	this.mainLayout = new MainLayout(this);
	this.contactsLayout = new ContactsLayout(this);
	this.authLayout = new AuthLayout(this);
	this.client = Client.create(newResultHandler(), null, null); // recreate client after previous has closed
        Client.execute(new TdApi.SetLogVerbosityLevel(0));
	final String logFile = new File(getLuwrain().getFileProperty("luwrain.dir.userhome"), "td.log").getAbsolutePath();
	Log.debug(LOG_COMPONENT, "tdlib log file is " + logFile);
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile(logFile, 1 << 27))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }
	setAppName(getStrings().appName());
	return authLayout.getLayout();
    }

        Conversations getConv()
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

    void layout(AreaLayout layout)
    {
	NullCheck.notNull(layout, "layout");
			getLayout().setBasicLayout(layout);
		getLuwrain().announceActiveArea();
    }

    Layouts layouts()
    {
	return new Layouts(){
	    @Override public void main()
	    {
		getLayout().setBasicLayout(mainLayout.getLayout());
		getLuwrain().announceActiveArea();
	    }
	    	    @Override public void contacts()
	    {
		getLayout().setBasicLayout(contactsLayout.getLayout());
		getLuwrain().announceActiveArea();
		contactsLayout.updateContactsList();
	    }
	};
    }

    @Override public boolean onEscape()
    {
	closeApp();
	return true;
    }

    @Override public void closeApp()
    {
	Log.debug(LOG_COMPONENT, "finishing the session");
	if (client != null)
	    client.close();
	Log.debug(LOG_COMPONENT, "client closed");
	super.closeApp();
    }

    @Override public MonoApp.Result onMonoAppSecondInstance(Application app)
    {
	NullCheck.notNull(app, "app");
	return MonoApp.Result.BRING_FOREGROUND;
    }

    interface Layouts
{
    void main();
    void contacts();
    }
}
