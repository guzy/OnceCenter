package oncecenter.action;

import oncecenter.util.ImageRegistry;

import org.eclipse.jface.action.Action;

public class OnceAction extends Action {
	
	public OnceAction(){
		super();
	}
	
	public OnceAction(String text,String image,String disabledImage){
		super();
		setAppear(text,image,disabledImage);
	}
	
	public void setAppear(String text,String image,String disabledImage){
		setText(text);
		if(image!=null&&image.length()>0)
			setImageDescriptor(ImageRegistry.getImageDescriptor(image));
		if(disabledImage!=null&&disabledImage.length()>0)
			setDisabledImageDescriptor(ImageRegistry.getImageDescriptor(disabledImage));
	}
}
