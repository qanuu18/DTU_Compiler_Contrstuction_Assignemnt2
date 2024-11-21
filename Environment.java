import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;

class Environment {
    // Map for the signals: each signal has a current value as a Boolean
    private HashMap<String,Boolean> variableValues = new HashMap<String,Boolean>();

    // Map for defined functions like "xor": for a string like "xor",
    // you can look up its definition
    private HashMap<String,Def> defs;

    // Standard constructor that does not care about definitions
    // You cannot use getDef() below when using this constructor.
    public Environment(){ this.defs=new HashMap<String,Def>();}

    // Constructor that compute the map of function definitions, given
    // a the set of definitions as available in Circuit. You can then
    // use getDef() below.
    public Environment(List<Def> listdefs) {
	defs=new HashMap<String,Def>();
	for(Def d:listdefs)
	    defs.put(d.f,d);
    }

    // This constructor can be used during eval to create a new
    // environment with the same definitions contained in an existing
    // one:
    public Environment(Environment env) { this.defs=env.defs; }

    // Lookup a definition, e.g., "xor"
    public Def getDef(String name){
	Def d=defs.get(name);
	if (d==null){ System.err.println("Function not defined: "+name); System.exit(-1); }
	return d;
    }

    // return the set of all definitions; this is helpful when
    // creating a new environment during eval: just get the defs from
    // the current environment and using it as an argument to the
    // constructor for the new environemnt
    public HashMap<String,Def> getDefs(){return defs;};
    public void setVariable(String name, Boolean value) {
	variableValues.put(name, value);
    }

    public Boolean getVariable(String name){
	Boolean value = variableValues.get(name);
	if (value == null) { System.err.println("Variable not defined: "+name); System.exit(-1); }
	return value;
    }

    public Boolean hasVariable(String name){
	Boolean v = variableValues.get(name);
	return (v != null);
    }

    public String toString() {
	String table = "";
	for (Entry<String,Boolean> entry : variableValues.entrySet()) {
	    table += entry.getKey() + "\t-> " + entry.getValue() + "\n";
	}
	return table;
    }
    /* Task 1:
    Implementation of the eval()-method for all of the variables
    */

    // we will initilize the expr themselves
    // Each expr will be derived from this abs class
    abstract class Expr extends AST {
    public abstract Boolean eval(Environment env);
    }
    class Conjunction extends Expr {
    Expr e1, e2;

    Conjunction(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    @Override
    public Boolean eval(Environment env) {
        return e1.eval(env) && e2.eval(env);
        }
    }


    class Disjunction extends Expr {
    Expr e1, e2;

    Disjunction(Expr e1, Expr e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    @Override
    public Boolean eval(Environment env) {
        return e1.eval(env) || e2.eval(env);
        }
    }

    // the negation op just involves a single expr
    class Negation extends Expr {
    Expr e;

    Negation(Expr e) {
        this.e = e;
    }

    @Override
    public Boolean eval(Environment env) {
        //returns the negated Boolean value
        return !e.eval(env);
        }
    }
    // fetches name of signal
    class Signal extends Expr {
    String varname;

    Signal(String varname) {
        this.varname = varname;
    }

    @Override
    public Boolean eval(Environment env) {
        // Ensures the signal is defined, or exits with an error.
        return env.getVariable(varname);
        }
    }
    /*
    UseDef class

    1. looks up definition of function
    2. Eval arguments of function
    3. Computes results inside func-body
    */
    class UseDef extends Expr {
    String f;           // The name of the function being used (e.g., "xor").
    List<Expr> args;    // The list of argument expressions passed to the function.

    UseDef(String f, List<Expr> args) {
        this.f = f;
        this.args = args;
        }
    }
    //EVAL LOGIC OF UseDef

    @Override
    public Boolean eval(Environment env) {
    // Retrieve the function definition from the environment
    Def definition = env.getDef(f);

    // Check if the number of arguments matches the function definition
    if (definition.args.size() != args.size()) {
        error("Argument mismatch: Function " + f + " expects " +
              definition.args.size() + " arguments but got " + args.size());
    }
    // Create a new environment for evaluating the function body
    Environment newEnv = new Environment(env);

    // Assign values to the formal parameters
    for (int i = 0; i < args.size(); i++) {
        String paramName = definition.args.get(i);  // Formal parameter name
        Boolean paramValue = args.get(i).eval(env); // Evaluate the argument
        newEnv.setVariable(paramName, paramValue);  // Set it in the new environment
    }
    // Evaluate the function body in the new environment
    return definition.e.eval(newEnv);
    }


}
