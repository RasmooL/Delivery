package jessmw;
import jess.*;


public class funAddFact implements Userfunction {
    private static final String functionName = "AddFact";
	
    public String getName() {
		return functionName;
    }
	
    public Value call(ValueVector vv, Context c) throws JessException {
		if ((vv.size() < 8) || (vv.size() > 10)) throw new JessException(functionName,"Wrong number of arguments. Usage (AddFact varname type width factname structname server instance[ pushTime[ pushCmd]])",vv.size() - 1);
		
		XmlFact fact = new XmlFact();
		fact.setVariable(vv.get(1).stringValue(c));
		fact.setType(vv.get(2).stringValue(c));
		fact.setwidth(vv.get(3).intValue(c));
		fact.setFactName(vv.get(4).stringValue(c));
		
		VariableStruct struct = new VariableStruct(vv.get(5).stringValue(c),false, 0, "", true);
		
		SMRobject instance = SMRcomm.getByHandle(vv.get(7).stringValue(c));
		
		Server host = instance.getServerByHandle(vv.get(6).stringValue(c));
		
		struct.setHost(host);
		
		if (host.getServerName() != "mrc") {
			struct.setPushable(true); //the struct is updated via push on server
			if (vv.size() < 10)
				struct.setCmd(struct.getStructName()); //push is same as struct, i.e. gmk
			else 
				struct.setCmd(vv.get(9).stringValue(c)); //push has special cmd, i.e. gmk img = 30
			if (vv.size() > 8)
				struct.setTime(vv.get(8).floatValue(c));
			else
				struct.setTime(1.0);
		}
			
		fact.setStruct(struct);
		
		Rete engine = c.getEngine();
		try {
			if(fact.getType().toLowerCase().contains("string")) {
				engine.defclass(fact.getFactName(), "jessmw.GenericStringVar", null);
				engine.definstance(fact.getFactName(), new GenericStringVar(vv.get(7).stringValue(c), fact), true, c);
			} else if(fact.getType().toLowerCase().contains("pose")) {
				engine.defclass(fact.getFactName(), "jessmw.GenericPoseVar", null);
				engine.definstance(fact.getFactName(), new GenericPoseVar(vv.get(7).stringValue(c), fact), true, c);
			} else if(fact.getType().toLowerCase().contains("time")) {
				engine.defclass(fact.getFactName(), "jessmw.GenericTimeVar", null);
				engine.definstance(fact.getFactName(), new GenericTimeVar(vv.get(7).stringValue(c), fact), true, c);
			} else if(fact.getType().toLowerCase().contains("double")) {
				switch (fact.getwidth()) {
				case 1:
    				engine.defclass(fact.getFactName(), "jessmw.GenericDouble1Var", null);
    				engine.definstance(fact.getFactName(), new GenericDouble1Var(vv.get(7).stringValue(c), fact), true, c);
    				break;
				case 2:
					engine.defclass(fact.getFactName(), "jessmw.GenericDouble2Var", null);
    				engine.definstance(fact.getFactName(), new GenericDouble2Var(vv.get(7).stringValue(c), fact), true, c);
    				break;
				case 3:
    				engine.defclass(fact.getFactName(), "jessmw.GenericDouble3Var", null);
    				engine.definstance(fact.getFactName(), new GenericDouble3Var(vv.get(7).stringValue(c), fact), true, c);
					break;
				case 4:
    				engine.defclass(fact.getFactName(), "jessmw.GenericDouble4Var", null);
    				engine.definstance(fact.getFactName(), new GenericDouble4Var(vv.get(7).stringValue(c), fact), true, c);
					break;
				case 5:
    				engine.defclass(fact.getFactName(), "jessmw.GenericDouble5Var", null);
    				engine.definstance(fact.getFactName(), new GenericDouble5Var(vv.get(7).stringValue(c), fact), true, c);
					break;
				case 6:
    				engine.defclass(fact.getFactName(), "jessmw.GenericDouble6Var", null);
    				engine.definstance(fact.getFactName(), new GenericDouble6Var(vv.get(7).stringValue(c), fact), true, c);
					break;
				case 7:
    				engine.defclass(fact.getFactName(), "jessmw.GenericDouble7Var", null);
    				engine.definstance(fact.getFactName(), new GenericDouble7Var(vv.get(7).stringValue(c), fact), true, c);
					break;
				case 8:
    				engine.defclass(fact.getFactName(), "jessmw.GenericDouble8Var", null);
    				engine.definstance(fact.getFactName(), new GenericDouble8Var(vv.get(7).stringValue(c), fact), true, c);
					break;
				case 9:
    				engine.defclass(fact.getFactName(), "jessmw.GenericDouble9Var", null);
    				engine.definstance(fact.getFactName(), new GenericDouble9Var(vv.get(7).stringValue(c), fact), true, c);
					break;
				
				default:
    				engine.defclass(fact.getFactName(), "jessmw.GenericStringVar", null);
    				engine.definstance(fact.getFactName(), new GenericStringVar(vv.get(7).stringValue(c), fact), true, c);
				}
			}
		} catch (JessException je) {
			System.out.println(je.getDetail());
			return new Value("Added 0 fact",RU.STRING);
		} catch (CloneNotSupportedException cnse) { 
			System.out.println(cnse.toString()); 
			return new Value("Added 0 fact",RU.STRING);
		} catch (NullPointerException npe) {
			try {
				engine.defclass(fact.getFactName(), "jessmw.GenericStringVar", null);
				engine.definstance(fact.getFactName(), new GenericStringVar(vv.get(7).stringValue(c), fact), true, c);
			} catch (JessException je) {
				System.out.println(je.getDetail());
				return new Value("Added 0 fact",RU.STRING);
			} catch (CloneNotSupportedException cnse) { 
				System.out.println(cnse.toString()); 
				return new Value("Added 0 fact",RU.STRING);
			}
		
		}
		if (host.getServerName() != "mrc") {
			//initPush
			String cmd = "push cmd='" + struct.getCmd() + "' t=" + struct.getTime() ;
			instance.sendPushCommand(cmd, host.getServerName());
			struct.setInitialized(true);
			
			//initFact
			cmd = "varpush cmd='var " + fact.getStructName() + "." + fact.getVariable() +
					 " copy' struct=" + fact.getStructName();

			instance.sendPushCommand(cmd, host.getServerName());
			fact.setInitialized(true);
		} else {
			instance.addStreamvars(fact.getFactName(), fact, 0.0);
		}
	
		
		return new Value("Added 1 fact",RU.STRING);
    }
}