/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.szadowsz.gospel.core.db.lib;

import com.szadowsz.gospel.core.Prolog;
import com.szadowsz.gospel.core.data.Struct;
import com.szadowsz.gospel.core.data.Term;
import com.szadowsz.gospel.core.db.primitive.PrimitiveInfo;
import com.szadowsz.gospel.util.LoggerCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * This abstract class is the base class for developing
 * tuProlog built-in libraries, which can be dynamically
 * loaded by prolog objects.
 * <p>
 * Each library can expose to engine:
 * <ul>
 * <li> a theory (as a string assigned to theory field)
 * <li> builtin predicates: each method whose signature is
 *       boolean name_arity(Term arg0, Term arg1,...)
 *   is considered a built-in predicate provided by the library
 * <li> builtin evaluable functors: each method whose signature is
 *       Term name_arity(Term arg0, Term arg1,...)
 *   is considered a built-in functors provided by the library
 * </ul>
 * <p>
 */
public abstract class Library implements Serializable {
    protected Logger _logger = LoggerFactory.getLogger(LoggerCategory.LIB());

    private static final long serialVersionUID = 1L;
    /**
	 * prolog core which loaded the library
	 */
    protected Prolog engine;

    /**
	 * operator mapping
	 */
    private String[][] opMappingCached;

    public Library(){
        opMappingCached = getSynonymMap();
    }

    /**
     * Gets the name of the library.
     *
     * By default the name is the class name.
     *
     * @return the library name
     */
    public String getName() {
        return getClass().getName();
    }

    /**
     * Gets the theory provided with the library
     *
     * Empty theory is provided by default.
     */
    public String getTheory() {
        return "";
    }

    public String getTheory(int a) {
    	return "";
    }

    /**
     * Gets the synonym mapping, as array of
     * elements like  { synonym, original name}
     */
    public String[][] getSynonymMap() {
        return null;
    }

    /**
	 * Gets the engine to which the library is bound
	 * @return  the engine
	 */
    public Prolog getEngine() {
        return engine;
    }

    /**
	 * @param en
	 */
    public void setEngine(Prolog en) {
        engine = en;
    }

    /**
     * tries to unify two terms
     *
     * The runtime (demonstration) context currently used by the engine
     * is deployed and altered.
     */
    protected boolean unify(Term a0,Term a1) {
        return engine.unify(a0,a1);
    }

    /**
     * tries to unify two terms
     *
     * The runtime (demonstration) context currently used by the engine
     * is deployed and altered.
     */
    protected boolean match(Term a0,Term a1) {
        return engine.isMatch(a0, a1);
    }


    /**
     * Evaluates an expression. Returns null value if the argument
     * is not an evaluable expression
     *
     * The runtime (demo) context currently used by the engine
     * is deployed and altered.
     * @throws Throwable
     */
    protected Term evalExpression(Term term) throws Throwable {
        if (term == null)
            return null;
        Term val = term.getTerm();
        if (val instanceof Struct) {
            Struct t = (Struct) val;
            if (term != t)
                if (!t.isPrimitive())
                    engine.identifyFunctor(t);
            if (t.isPrimitive()) {
                PrimitiveInfo bt = t.getPrimitive();
                // check for library functors
                if (bt.isFunctor())
                    return bt.evalAsFunctor(t);
            }
        } else if (val instanceof com.szadowsz.gospel.core.data.numeric.Number) {
            return val;
        }
        return null;
    }


    /**
     * method invoked by prolog engine when library is
     * going to be removed
     */
    public void dismiss() {}

    /**
     * method invoked when the engine is going
     * to demonstrate a goal
     */
    public void onSolveBegin(Term goal) {}

    /**
     * method invoked when the engine has
     * finished a demostration
     */

    public void onSolveHalt(){}

    public void onSolveEnd() {}

    /**
     * gets the list of predicates defined in the library
     */
    public Map<Integer,List<PrimitiveInfo>> getPrimitives() {
        try {
            java.lang.reflect.Method[] mlist = this.getClass().getMethods();
            Map<Integer,List<PrimitiveInfo>> mapPrimitives = new HashMap<Integer, List<PrimitiveInfo>>();
            mapPrimitives.put(PrimitiveInfo.DIRECTIVE(),new ArrayList<PrimitiveInfo>());
            mapPrimitives.put(PrimitiveInfo.FUNCTOR(),new ArrayList<PrimitiveInfo>());
            mapPrimitives.put(PrimitiveInfo.PREDICATE(),new ArrayList<PrimitiveInfo>());
            //{new ArrayList<PrimitiveInfo>(), new ArrayList<PrimitiveInfo>(), new ArrayList<PrimitiveInfo>()};

            for (int i = 0; i < mlist.length; i++) {
                String name = mlist[i].getName();

                Class<?>[] clist = mlist[i].getParameterTypes();
                Class<?> rclass = mlist[i].getReturnType();
                String returnTypeName = rclass.getName();

                int type;
                if (returnTypeName.equals("boolean")) type = PrimitiveInfo.PREDICATE();
                else if (returnTypeName.equals("com.szadowsz.gospel.core.data.Term")) type = PrimitiveInfo.FUNCTOR();
                else if (returnTypeName.equals("void")) type = PrimitiveInfo.DIRECTIVE();
                else continue;

                int index=name.lastIndexOf('_');
                if (index!=-1) {
                    try {
                        int arity = Integer.parseInt(name.substring(index + 1, name.length()));
                        // check arg number
                        if (clist.length == arity) {
                            boolean valid = true;
                            for (int j=0; j<arity; j++) {
                                if (!(Term.class.isAssignableFrom(clist[j]))) {
                                    valid = false;
                                    break;
                                }
                            }
                            if (valid) {
                                String rawName = name.substring(0,index);
                                String key = rawName + "/" + arity;
                                PrimitiveInfo prim = new PrimitiveInfo(type, key, this, mlist[i], arity);
                                mapPrimitives.get(type).add(prim);
                                //
                                // adding also or synonims
                                //
                                String[] stringFormat = {"directive","predicate","functor"};
                                if (opMappingCached != null) {
                                    for (int j=0; j<opMappingCached.length; j++){
                                        String[] map = opMappingCached[j];
                                        if (map[2].equals(stringFormat[type]) && map[1].equals(rawName)){
                                            key = map[0] + "/" + arity;
                                            prim = new PrimitiveInfo(type, key, this, mlist[i], arity);
                                            mapPrimitives.get(type).add(prim);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {}
                }

            }
            return mapPrimitives;
        } catch (Exception ex) {
            return null;
        }
    }


    /**
     * Gets the method linked to a builtin (null value if
     * the builtin has not any linked service)
     */
    /*    public Method getLinkedMethod(Struct s){
     //System.out.println("get linked for "+s);

      int arity = s.getArity();
      String name = s.getName()+"_"+arity;

      // NOT found, Try with synonims
       Method m = findMethod(name,arity);
       if (m!=null){
       return m;
       }

       // try with synonims
        if (opMappingCached!=null){
        String rawName=s.getName();
        for (int j=0; j<opMappingCached.length; j++){
        String[] map=opMappingCached[j];
        if (map[0].equals(rawName)){
        return findMethod(map[1]+"_"+s.getArity(),s.getArity());
        }
        }
        }
        return null;
        }

        private Method findMethod(String name, int arity){
        Method[] mlist = this.getClass().getMethods();
        for (int i=0; i<mlist.length; i++){
        if (mlist[i].getName().equals(name)){
        Class[] parms=mlist[i].getParameterTypes();
        if (parms.length==arity){
        boolean valid=true;
        for (int j=0; j<parms.length; j++){
        if (!Term.class.isAssignableFrom(parms[j])){
        valid=false;
        }
        }
        if (valid){
        return mlist[i];
        }
        }
        }
        }
        return null;
        }
        */


}
/*
// * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License as published by the Free Software Foundation; either
// * version 2.1 of the License, or (at your option) any later version.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this library; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// */
//package com.szadowsz.gospel.core.db.lib
//
//import com.szadowsz.gospel.core.Prolog
//import com.szadowsz.gospel.core.data.{Struct, Term}
//import com.szadowsz.gospel.core.db.primitive.PrimitiveInfo
//import java.util
//
//import com.szadowsz.gospel.util.LoggerCategory
//import org.slf4j.LoggerFactory
//
//import scala.util.control.NonFatal
//
///**
//  *
//  * This abstract class is the base class for developing tuProlog built-in libraries, which can be dynamically loaded by prolog objects.
//  *
//  * Each library can expose to engine:
//  *
//  * a theory (as a string assigned to theory field)
//  *
//  * builtin predicates: each method whose signature is boolean name_arity(Term arg0, Term arg1,...) is considered a built-in predicate provided by the
//  * library.
//  *
//  * builtin evaluable functors: each method whose signature is Term name_arity(Term arg0, Term arg1,...) is considered a built-in functors provided by the
//  * library.
//  */
//abstract class Library extends Serializable {
//    protected val logger = LoggerFactory.getLogger(LoggerCategory.LIB)
//
//    private val opMappingCached: Array[Array[String]] = getSynonymMap
//
//    /**
//      * prolog core which loaded the library.
//      */
//    protected var engine: Prolog = _
//
//    def setEngine(en: Prolog) = engine = en
//
//    /**
//      * Gets the name of the library.
//      *
//      * By default the name is the class name.
//      *
//      * @return the library name
//      */
//    def getName : String = getClass.getName
//
//
//    /**
//      * Gets the theory provided with the library.
//      *
//      * Empty theory is provided by default.
//      *
//      * @return theory string.
//      */
//    def getTheory: String = ""
//
//    /**
//      * Gets the engine to which the library is bound.
//      *
//      * @return the engine.
//      */
//    def getEngine: Prolog = engine
//
//    /**
//      * Gets the synonym mapping, as array of elements like  { synonym, original name}
//      */
//    def getSynonymMap: Array[Array[String]] = null
//
//    /**
//      * method invoked by prolog engine when library is going to be removed.
//      */
//    def dismiss(): Unit = {
//    }
//
//    /**
//      * method invoked when the engine is going to demonstrate a goal.
//      */
//    def onSolveBegin(goal: Term): Unit = {
//    }
//
//    /**
//      * method invoked when the engine has halted a demonstration.
//      */
//    def onSolveHalt(): Unit = {
//    }
//
//    /**
//      * method invoked when the engine has finished a demonstration.
//      */
//    def onSolveEnd(): Unit = {
//    }
//
//    /**
//      * tries to unify two terms
//      *
//      * The runtime (demonstration) context currently used by the engine is deployed and altered.
//      */
//    protected def unify(a0: Term, a1: Term): Boolean = engine.unify(a0, a1)
//
//    /**
//      * tries to unify two terms
//      *
//      * The runtime (demonstration) context currently used by the engine is deployed and altered.
//      */
//    protected def `match`(a0: Term, a1: Term): Boolean = engine.isMatch(a0, a1)
//
//    /**
//      * Evaluates an expression. Returns null value if the argument is not an evaluable expression
//      *
//      * The runtime (demo) context currently used by the engine is deployed and altered.
//      *
//      * @throws Throwable
//      */
//    @throws[Throwable]
//    protected def evalExpression(term: Term): Term = {
//        if (term == null) return null
//        val `val`: Term = term.getTerm
//        if (`val`.isInstanceOf[Struct]) {
//            val t: Struct = `val`.asInstanceOf[Struct]
//            if (term ne t) if (!t.isPrimitive) engine.identifyFunctor(t)
//            if (t.isPrimitive) {
//                val bt: PrimitiveInfo = t.getPrimitive
//                // check for library functors
//                if (bt.isFunctor) return bt.evalAsFunctor(t)
//            }
//        }
//        else if (`val`.isInstanceOf[Number]) return `val`
//        null
//    }
//
//    /**
//      * gets the list of predicates defined in the library
//      */
//    def getPrimitives: util.Map[Integer, util.List[PrimitiveInfo]] = {
//        val methods = getClass.getMethods
//        val mapPrimitives: util.Map[Integer, util.List[PrimitiveInfo]] = new util.HashMap[Integer, util.List[PrimitiveInfo]]
//        mapPrimitives.put(PrimitiveInfo.DIRECTIVE, new util.ArrayList[PrimitiveInfo])
//        mapPrimitives.put(PrimitiveInfo.FUNCTOR, new util.ArrayList[PrimitiveInfo])
//        mapPrimitives.put(PrimitiveInfo.PREDICATE, new util.ArrayList[PrimitiveInfo])
//
//        val functorType = classOf[Term].getName
//
//        for (method <- methods) {
//            val name: String = method.getName
//
//            val clist: Array[Class[_]] = method.getParameterTypes
//            val rclass: Class[_] = method.getReturnType
//            val returnTypeName: String = rclass.getName
//
//            val rType = returnTypeName match {
//                case "boolean" => PrimitiveInfo.PREDICATE
//                case `functorType` => PrimitiveInfo.FUNCTOR
//                case "void" => PrimitiveInfo.DIRECTIVE
//                case _ => -1
//            }
//            if (rType >= 0) {
//                try {
//                    val index = name.lastIndexOf('_')
//                    if (index != -1) {
//                        val arity: Int = name.substring(index + 1, name.length).toInt
//                        // check arg number
//                        if (clist.length == arity) {
//                            if (clist.forall(classOf[Term].isAssignableFrom)) {
//                                val rawName: String = name.substring(0, index)
//                                mapPrimitives.get(rType).add(new PrimitiveInfo(rType, rawName + "/" + arity, this, method, arity))
//                                val stringFormat: Array[String] = Array("directive", "predicate", "functor")
//                                if (opMappingCached != null) {
//                                    for (map <- opMappingCached) {
//                                        if (map(2) == stringFormat(rType) && map(1) == rawName) {
//                                            mapPrimitives.get(rType).add(new PrimitiveInfo(rType, map(0) + "/" + arity, this, method, arity))
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } catch {
//                    case NonFatal(e)=> logger.debug(s"$name term unable to be added")
//                }
//            }
//        }
//        mapPrimitives
//    }
//}