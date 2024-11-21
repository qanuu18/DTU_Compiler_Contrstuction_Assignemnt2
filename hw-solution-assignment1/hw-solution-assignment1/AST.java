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
public abstract Boolean eval(Enviroment enviroment);

}


class Conjunction extends Expr{
    // Example: Signal1 * Signal2 
    Expr e1,e2;
    Conjunction(Expr e1,Expr e2){this.e1=e1; this.e2=e2;}
    // we override the method so its usage can be done in other classes.
    // both expression needs to be true.
    @Override
    public Boolean eval (Enviroment enviroment){
        return e1.eval(enviroment) && e2.eval(enviroment);
    }
}

class Disjunction extends Expr{
    // Example: Signal1 + Signal2 
    Expr e1,e2;
    Disjunction(Expr e1,Expr e2){this.e1=e1; this.e2=e2;}
    // one or the other
    @Override
    public Boolean eval(Enviroment eviroment){
        return e1.eval(enviroment) || e2.eval(eviroment);
    }
}

class Negation extends Expr{
    // Example: /Signal
    Expr e;
    Negation(Expr e){this.e=e;}
    @Override
    public Boolean eval(Enviroment enviroment){
// we negate the result of inner expression
     return !e.eval(enviroment);
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
    @Override
    public Boolean eval(Enviroment enviroment){
    error("Implementation not done yet");
    return null;
    }
}

class Signal extends Expr{
    String varname; // a signal is just identified by a name 
    Signal(String varname){this.varname=varname;}
    // Here we need the boolean to check if it contains the signal name or else return error
    @Override
    public Boolean eval(Enviroment enviroment){
        if(!enviroment.contains(varname)){
            error("Wrong signal or undeclared signal" + varname);
        }
        return enviroment.get(varname);
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
 public Boolean eval(Enviroment enviroment){
       // We evaluate the current expression value that yields
    Boolean value = e.eval(enviroment);
    // And update the signal in the enviroment
    enviroment.set(name,value);
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
    //Create a toString method for our output of a trace.
    @Override
    public toString(){
        // We append tµrue or false by 0 or 1 
        for(Boolean value : values){
            sb.append(value ? "1": "0");
        }
        // We here append the signal name
        sb.append(" ").append(signal);
        return sb.toString();
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
    public void latchesInit(Enviroment enviroment){

        for(String latch : latches){
            // we set latch outputs to 0.
            enviroment.set(latch + "'", false);
        }
    }

    // Create method latches update
    /* Write a method latchesUpdate of class Circuit that also takes an environment as argument
       and sets every latch output to the current value of the latch input. In the example above, it
      would write to A’ the current value of A, and similar for B and C.*/
      public void latchesUpdate(Enviroment enviroment){

        for(String latch : latches){
            //current value of latch
            Boolean value = enviroment.get(latch);
            // set latch value to output
            enviroment.set(latch + "'", value);

        }
      }

    
}