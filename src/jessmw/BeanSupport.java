package jessmw;

import java.beans.*;


public abstract class BeanSupport {
    protected PropertyChangeSupport my_pcs =
		new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener p) {
		my_pcs.addPropertyChangeListener(p);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener p) {
		my_pcs.removePropertyChangeListener(p);
    }

    boolean updateValues(double val[], long time) {
		return false;
    }
    
    boolean updateValues() {
		return false;
    }
    
    boolean updateStringValues(String val, long time) {
    	return false;
    }
    String remove() {
    	return "";
    }
}