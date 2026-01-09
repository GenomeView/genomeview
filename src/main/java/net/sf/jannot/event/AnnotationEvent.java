/**
 * %HEADER%
 */
package net.sf.jannot.event;

import net.sf.jannot.FeatureAnnotation;

public abstract class AnnotationEvent implements ChangeEvent {

	protected FeatureAnnotation annotation;
	
	private String msg = new String();

	public AnnotationEvent(FeatureAnnotation a) {
		this.annotation = a;
	}
	
	public AnnotationEvent(FeatureAnnotation a, String msg){
		this.annotation = a;
		this.msg = msg;
	}
	
	@Override
    public String toString(){
    	return new String("Edit annotation "+"("+msg+")");
    }
}
