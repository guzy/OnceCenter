package oncecenter.views.logview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import oncecenter.views.xenconnectiontreeview.elements.VMTreeObject;

import org.eclipse.swt.graphics.Image;

public class VMEvent implements Serializable{
	public static enum eventType{info, warning};
	public static enum stateType{progressing,success,fail};
	private String description;
	private eventType type;
	private Date datetime;
	private String task;
	private VMTreeObject target;
	private String user;
	private ArrayList<VMEvent> relatedEvents;
	private stateType state;
	transient private Image image;
	
	public VMEvent(){
		relatedEvents=new ArrayList<VMEvent>();
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public VMTreeObject getTarget() {
		return target;
	}

	public void setTarget(VMTreeObject target) {
		this.target = target;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public ArrayList<VMEvent> getRelatedEvents() {
		return relatedEvents;
	}

	public void setRelatedEvents(ArrayList<VMEvent> relatedEvents) {
		this.relatedEvents = relatedEvents;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public eventType getType() {
		return type;
	}

	public void setType(eventType type) {
		this.type = type;
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}

	public stateType getState() {
		return state;
	}

	public void setState(stateType state) {
		this.state = state;
	}
}
