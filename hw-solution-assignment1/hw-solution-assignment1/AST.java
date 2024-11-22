import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;

public abstract class AST{
    public void error(String msg){
	System.err.println(msg);
	System.exit(-1);
    }
};

/* Expressions are similar to arithmetic expressions in the impl
   language: the atomic expressions are just Signal (similar to
   variables in expressions) and they can be composed to larger
   expressions with And (Conjunction), Or (Disjunction), and Not
   (Negation). Moreover, an expression can be using any of the
   functions defined in the definitions. */
// Task 1: 

abstract class Expr extends AST{
//We start by defining our eval method and for its subclasses
//we know its a boolean expression and take eviroment as and parameter.
public abstract Boolean eval(Environment env);

}


class Conjunction extends Expr{
    // Example: Signal1 * Signal2 
    Expr e1,e2;
    Conjunction(Expr e1,Expr e2){this.e1=e1; this.e2=e2;}
    // we override the method so its usage can be done in other classes.
    // both expression needs to be true.
    @Override
    public Boolean eval (Environment env){
        return e1.eval(env) && e2.eval(env);
    }
}

class Disjunction extends Expr{
    // Example: Signal1 + Signal2 
    Expr e1,e2;
    Disjunction(Expr e1,Expr e2){this.e1=e1; this.e2=e2;}
    // one or the other
    @Override
    public Boolean eval(Environment env){
        return e1.eval(env) || e2.eval(env);
    }
}

class Negation extends Expr{
    // Example: /Signal
    Expr e;
    Negation(Expr e){this.e=e;}
    @Override
    public Boolean eval(Environment env){
// we negate the result of inner expression
     return !e.eval(env);
    }
}

class UseDef extends Expr{
    // Using any of the functions defined by "def"
    // e.g. xor(Signal1,/Signal2) 
    String f;  // the name of the function, e.g. "xor" 
    List<Expr> args;  // arguments, e.g. [Signal1, /Signal2]
    UseDef(String f, List<Expr> args){
	this.f=f; this.args=args;
    }
    //For usedef we need tor return error as implementation is first in tast 2.
    // Now we update our eval method to implement task 2. 
    // delete error();
    @Override
    public Boolean eval(Environment env){
    // Firstly we need to fetch function definition from environment
    Def def = env.getDef(f);

    // We then create a new environment for evaluation of the body 
    Environment newEnv = new Environment(env);

    // we then bind all arguments to the formal paramtres
    for(int i = 0; i < def.args.size(); i++){
        //parameter name stored
        String pname = def.args.get(i);
        // evaluation of actual argument stored
        Boolean argvalue = args.get(i).eval(env);
        //bind the two in newly created environment
        newEnv.setVariable(pname, argvalue);

    }
    
    //Lastly we evaluate the function body in a new environment
    return def.e.eval(newEnv);

    
    }
}

class Signal extends Expr{
    String varname; // a signal is just identified by a name 
    Signal(String varname){this.varname=varname;}
    // Here we need the boolean to check if it contains the signal name or else return error
    @Override
    public Boolean eval(Environment env){
        if(!env.hasVariable(varname)){
            error("Wrong signal or undeclared signal: " + varname);
        }
        return env.getVariable(varname);
    }
}

class Def extends AST{
    // Definition of a function
    // Example: def xor(A,B) = A * /B + /A * B
    String f; // function name, e.g. "xor"
    List<String> args;  // formal arguments, e.g. [A,B]
    Expr e;  // body of the definition, e.g. A * /B + /A * B
    Def(String f, List<String> args, Expr e){
	this.f=f; this.args=args; this.e=e;
    }
    // No eval because of no userdefined functions for task1
}

// An Update is any of the lines " signal = expression "
// in the update section

class Update extends AST{
    // Example Signal1 = /Signal2 
    String name;  // Signal being updated, e.g. "Signal1"
    Expr e;  // The value it receives, e.g., "/Signal2"
    Update(String name, Expr e){this.e=e; this.name=name;}
    /*Write for this class a method eval that sets the value of the defined signal to the value that the
given expression currently yields. This method eval also takes an Environment as argument, but
returns nothing. */
 public void eval(Environment env){
       // We evaluate the current expression value that yields
    Boolean value = e.eval(env);
    // And update the signal in the enviroment
    env.setVariable(name,value);
 }
}

/* A Trace is a signal and an array of Booleans, for instance each
   line of the .simulate section that specifies the traces for the
   input signals of the circuit. It is suggested to use this class
   also for the output signals of the circuit in the second
   assignment.
*/

class Trace extends AST{
    // Example Signal = 0101010
    String signal;
    Boolean[] values;
    Trace(String signal, Boolean[] values){
	this.signal=signal;
	this.values=values;
    }
    public String getTraceValues(Boolean[] values) {
    StringBuilder sb = new StringBuilder();
    for (Boolean value : values) {
        sb.append(value != null && value ? "1" : "0");
    }
    return sb.toString();
    //Create a toString method for our output of a trace.
   /* @Override
    public String toString(){
        // fixed compiling issue forgot to Initialize StringBuilder()
        StringBuilder sb = new StringBuilder();
        // We append tµrue or false by 0 or 1 
        for(Boolean value : values){
            sb.append(value != null && value ? "1": "0");
        }
        // We here append the signal name
        sb.append(" ").append(signal);
        return sb.toString();
    }*/
    
}

}

/* The main data structure of this simulator: the entire circuit with
   its inputs, outputs, latches, definitions and updates. Additionally
   for each input signal, it has a Trace as simulation input.
   
   There are two variables that are not part of the abstract syntax
   and thus not initialized by the constructor (so far): simoutputs
   and simlength. It is suggested to use these two variables for
   assignment 2 as follows: 
 
   1. all siminputs should have the same length (this is part of the
   checks that you should implement). set simlength to this length: it
   is the number of simulation cycles that the interpreter should run.

   2. use the simoutputs to store the value of the output signals in
   each simulation cycle, so they can be displayed at the end. These
   traces should also finally have the length simlength.
*/

class Circuit extends AST{
    String name;  
    List<String> inputs; 
    List<String> outputs;
    List<String>  latches;
    List<Def> definitions;
    List<Update> updates;
    List<Trace>  siminputs;
    List<Trace>  simoutputs;
    int simlength;
    Circuit(String name,
	    List<String> inputs,
	    List<String> outputs,
	    List<String>  latches,
	    List<Def> definitions,
	    List<Update> updates,
	    List<Trace>  siminputs){
	this.name=name;
	this.inputs=inputs;
	this.outputs=outputs;
	this.latches=latches;
	this.definitions=definitions;
	this.updates=updates;
	this.siminputs=siminputs;
    }
    // Handling latches 
    /*– Write a method latchesInit of class Circuit that takes an environment as argument and
        sets all latch outputs to value 0 in this environment*/
    public void latchesInit(Environment env){

        if(latches.isEmpty()){

            System.out.println("No latches to initialize.");
            return;
        }
    
        for(String latch : latches){
            // we set latch outputs to 0.
            env.setVariable(latch + "'", false);
        }
    }

    // Create method latches update
    /* Write a method latchesUpdate of class Circuit that also takes an environment as argument
       and sets every latch output to the current value of the latch input. In the example above, it
      would write to A’ the current value of A, and similar for B and C.*/
      public void latchesUpdate(Environment env){

        for(String latch : latches){
            //current value of latch
            Boolean value = env.getVariable(latch);
            // set latch value to output
            env.setVariable(latch + "'", value);    

        }
      }
      public void initialize(Environment env){
// Initialization of simulation happens here
        /*Firsly we ensure that method stops with
an error if the siminput is not defined for any input signal, or its array has length 0.*/

if(siminputs == null || siminputs.isEmpty()) {
    error("Null error no input entered");

}
// We set our simlength to the same length as of the first siminput
simlength = siminputs.get(0).values.length;
// Then we verify that all siminputs have the same length
for(Trace input : siminputs){
    if(input.values.length != simlength){
        error("Siminputs must have same length.");
    }

}   

// Now that we are sure the have same length we load the value into our enviroment
for(Trace input : siminputs){

    if(input.values.length == 0){
        error("The siminput for the signal: " + input.signal + " has length 0.");
    }
    // We set the value at slot 0 in our enviroment
    else env.setVariable(input.signal,input.values[0]);
}

// We call the latchesinit method to initialize all outputs of latches
latchesInit(env);
// After this we run the eval method for every Update to initilization for all remaining signals.
for(Update update : updates){

    update.eval(env);

}

// task 3 implementation
 simoutputs = new ArrayList<>();
 for(String output : outputs){

Boolean[] values = new Boolean[simlength];
for(int i = 0; i < values.length; i++){
    values[i] = env.getVariable(output);
}
simoutputs.add(new Trace(output, values));

 }

// Lastly we print the enviroment on the screen by using its own toString method
System.out.println(env.toString());


 }

      
      // Now we need to implement a nextCycle method that processes the circuit for a single cycle.
      public void nextCycle(Environment env,int cycle){

        // The implemenation is similar to the initialization method.
        // But here we need to update the input signals for the cycle.
        //Check if input value is same lentgh cycle.
        for(Trace input : siminputs){
            if(cycle >= input.values.length){
                error("Input for Trace signal: " + input.signal + " missing value for cycle " + cycle);
            }
            // else set value for cycle
            env.setVariable(input.signal, input.values[cycle]);
            

        }

        //Call update latches
        latchesUpdate(env);
        // Run the eval method of every Update to update all other signals.
        for(Update update : updates){
            update.eval(env);   
        }
    // Task 3 implementation updating output traces
    for(Trace trace : simoutputs){
        String output = trace.signal;
        Boolean value = env.getVariable(output);
        trace.values[cycle] = (value != null) ? value : false;
    }

// Use the same toString to print the enviroment of the cycle
System.out.println(env.toString());

      }
     public void runSimulator(Environment env) {
    if (siminputs == null || siminputs.isEmpty()) {
        error("Null error: No inputs provided.");
    }

    // Initialize the circuit
    initialize(env);

    // Run the simulation for the number of cycles in the simulation input length
    for (int i = 1; i < simlength; i++) {
        nextCycle(env, i);
    }

    // Print formatted output for Task 3
    System.out.println("\nPrinting simulation outputs:\n");

    // Calculate the maximum signal name length for proper alignment
    int maxSignalLength = 0;
    for (Trace trace : simoutputs) {
        maxSignalLength = Math.max(maxSignalLength, trace.signal.length());
    }
    for (Trace trace : siminputs) {
        maxSignalLength = Math.max(maxSignalLength, trace.signal.length());
    }

    // Print simulation inputs first
    for (Trace trace : siminputs) {
        System.out.printf("%-" + maxSignalLength + "s %s\n",
        trace.getTraceValues(trace.values), trace.signal);

    }

    // Print simulation outputs after inputs
    for (Trace trace : simoutputs) {
        System.out.printf("%-" + maxSignalLength + "s %s\n",
        trace.getTraceValues(trace.values), trace.signal);

    }
}


}



    

