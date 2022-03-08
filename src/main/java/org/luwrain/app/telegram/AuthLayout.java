//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram;

import java.util.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.controls.WizardArea.*;
import org.luwrain.app.base.*;
import org.luwrain.app.telegram.UpdatesHandler.InputWaiter;

final class AuthLayout extends LayoutBase implements Objects.NewInputWaiterListener
{
    static private final String
	LOG_COMPONENT = Core.LOG_COMPONENT;

    private final App app;
    final WizardArea wizardArea;

    AuthLayout(App app)
    {
	super(app);
	this.app = app;
	this.wizardArea = new WizardArea(getControlContext());
	setAreaLayout(wizardArea, null);
	app.getObjects().newInputWaiterListeners.add(this);
    }

    @Override public void onNewInputWaiter(InputWaiter inputWaiter)
    {
	NullCheck.notNull(inputWaiter, "inputWaiter");
	switch(inputWaiter.type)
	{
	case PhoneNumber: {
	    final Frame frame = wizardArea.newFrame();
	    wizardArea.show(frame);
	    break;
	}
	}
    }

}
