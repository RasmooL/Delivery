package jessmw;
import jess.*;


public class eventHandler implements JessListener {
	public void eventHappened(JessEvent je) {
		int type = je.getType();
		switch (type) {
			case JessEvent.FACT | JessEvent.REMOVED:
				String o = je.getObject().toString() ;
				try {
					String struct = o.split("struct \"")[1].split("\"")[0];
					String var = o.split("variable \"")[1].split("\"")[0];
					SMRcomm.retractPushvar(struct+'.'+var, null);
				}
				finally {
					break;
				}
			default:
			// ignore
		}
	}
}