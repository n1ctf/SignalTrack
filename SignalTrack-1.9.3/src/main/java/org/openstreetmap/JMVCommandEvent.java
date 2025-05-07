package org.openstreetmap;

import java.util.EventObject;

public class JMVCommandEvent extends EventObject {
    private static final long serialVersionUID = 8701544867914969620L;
	private Command command;
    private Object newValue;
	
	public enum Command {
        MOVE,
        ZOOM,
        ZOOM_IN_DISABLED,
        ZOOM_OUT_DISABLED
    }

    public JMVCommandEvent(Command cmd, Object source, Object newValue) {
        super(source);

        setCommand(cmd);
        setNewValue(newValue);
    }

    public JMVCommandEvent(Object source) {
        super(source);
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
    
    public Object getNewValue() {
    	return newValue;
    }
    
    public void setNewValue(Object newValue) {
    	this.newValue = newValue;
    }
}
