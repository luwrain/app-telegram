//
// Copyright 2020-2022 Michael Pozhidaev <msp@luwrain.org>
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//

package org.luwrain.app.telegram.layouts;

import java.util.*;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.TdApi.*;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.app.base.*;
import org.luwrain.nlp.*;
import org.luwrain.app.telegram.*;

import static org.luwrain.util.TextUtils.*;

abstract class ComposeLayoutBase extends LayoutBase
{
    final App app;
    final FormArea formArea;
    final Chat chat;
    final Message message;

    protected final FormSpellChecking spellChecking;

    abstract protected boolean onOk(ActionHandler closing, Runnable afterSending);

    protected ComposeLayoutBase(App app, Chat chat, Message message, ActionHandler closing, Runnable afterSending)
    {
	super(app);
	this.app = app;
	this.chat = chat;
	this.message = message;
	this.spellChecking = new FormSpellChecking(getLuwrain());
	this.formArea = new FormArea(getControlContext()){
		@Override public boolean onSystemEvent(SystemEvent event)
		{
		    if (event.getType() == SystemEvent.Type.REGULAR)
			switch(event.getCode())
			{
			case IDLE:
			    return onIdle();
			}
		    return super.onSystemEvent(event);
		}
	    };
	this.formArea.getMultilineEditChangeListeners().add(spellChecking);
	setCloseHandler(closing);
	setOkHandler(()->onOk(closing, afterSending));
    }

    private boolean onIdle()
    {
	if (!formArea.isHotPointInMultilineEdit())
	    return false;
	final MarkedLines lines = formArea.getMultilineEditContent();
	final int
	x = formArea.getMultilineEditHotPoint().getHotPointX(),
	y = formArea.getMultilineEditHotPoint().getHotPointY();
	if (y >= lines.getLineCount())
	    return true;
	final LineMarks marks = lines.getLineMarks(y);
	if (marks == null)
	    return  true;
	final LineMarks.Mark[] atPoint = marks.findAtPos(x);
	if (atPoint == null || atPoint.length == 0)
	    return true;
	for(LineMarks.Mark m: atPoint)
	{
	    if (m.getMarkObject() == null || !(m.getMarkObject() instanceof SpellProblem))
		continue;
	    final SpellProblem p = (SpellProblem)m.getMarkObject();
	    app.message(p.getComment(), Luwrain.MessageType.ANNOUNCEMENT);
	    return true;
	}
	return true;
    }

    static String[] getText(String[] text)
    {
	if (text.length == 0 || (text.length == 1 && text[0].trim().isEmpty()))
	    return new String[0];
	final List<String> res = new ArrayList<>();
	StringBuilder b = new StringBuilder();
	for(String i: text)
	{
	    if (i.trim().isEmpty())
	    {
		if (b.length() == 0)
		    continue;
		res.add(replaceChars(new String(b).trim()));
		b = new StringBuilder();
		continue;
	    }
	    b.append(i.trim()).append(" ");
	}
	if (b.length() > 0)
	    res.add(replaceChars(new String(b).trim()));
	return res.toArray(new String[res.size()]);
    }

    static String concatText(String[] lines)
    {
	if (lines.length == 0)
	    return null;
	final StringBuilder b = new StringBuilder();
	for(String l: lines)
	    b.append(l).append("\n\n");
	return new String(b);
	 }

    static String replaceChars(String line)
    {
	return line.replaceAll("---", "—").replaceAll("<<", "«").replaceAll(">>", "»");
    }
}
