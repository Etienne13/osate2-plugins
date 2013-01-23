/*
 * Created on July 7, 2004
 *
 * <copyright>
 * Copyright  2004 by Carnegie Mellon University, all rights reserved.
 *
 * Use of the Open Source AADL Tool Environment (OSATE) is subject to the terms of the license set forth
 * at http://www.eclipse.org/legal/cpl-v10.html.
 *
 * NO WARRANTY
 *
 * ANY INFORMATION, MATERIALS, SERVICES, INTELLECTUAL PROPERTY OR OTHER PROPERTY OR RIGHTS GRANTED OR PROVIDED BY
 * CARNEGIE MELLON UNIVERSITY PURSUANT TO THIS LICENSE (HEREINAFTER THE "DELIVERABLES") ARE ON AN "AS-IS" BASIS.
 * CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED AS TO ANY MATTER INCLUDING,
 * BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABILITY, INFORMATIONAL CONTENT,
 * NONINFRINGEMENT, OR ERROR-FREE OPERATION. CARNEGIE MELLON UNIVERSITY SHALL NOT BE LIABLE FOR INDIRECT, SPECIAL OR
 * CONSEQUENTIAL DAMAGES, SUCH AS LOSS OF PROFITS OR INABILITY TO USE SAID INTELLECTUAL PROPERTY, UNDER THIS LICENSE,
 * REGARDLESS OF WHETHER SUCH PARTY WAS AWARE OF THE POSSIBILITY OF SUCH DAMAGES. LICENSEE AGREES THAT IT WILL NOT
 * MAKE ANY WARRANTY ON BEHALF OF CARNEGIE MELLON UNIVERSITY, EXPRESS OR IMPLIED, TO ANY PERSON CONCERNING THE
 * APPLICATION OF OR THE RESULTS TO BE OBTAINED WITH THE DELIVERABLES UNDER THIS LICENSE.
 *
 * Licensee hereby agrees to defend, indemnify, and hold harmless Carnegie Mellon University, its trustees, officers,
 * employees, and agents from all claims or demands made against them (and any related losses, expenses, or
 * attorney's fees) arising out of, or relating to Licensee's and/or its sub licensees' negligent use or willful
 * misuse of or negligent conduct or willful misconduct regarding the Software, facilities, or other rights or
 * assistance granted by Carnegie Mellon University under this License, including, but not limited to, any claims of
 * product liability, personal injury, death, damage to property, or violation of any laws or regulations.
 *
 * Carnegie Mellon University Software Engineering Institute authored documents are sponsored by the U.S. Department
 * of Defense under Contract F19628-00-C-0003. Carnegie Mellon University retains copyrights in all material produced
 * under this contract. The U.S. Government retains a non-exclusive, royalty-free license to publish or reproduce these
 * documents, or allow others to do so, for U.S. Government purposes only pursuant to the copyright license
 * under the contract clause at 252.227.7013.
 *
 * </copyright>
 *
 *
 * @version $Id: PropertyTotals.java,v 1.1.2.11 2009-04-21 13:54:35 relief Exp $
 */
package org.osate.analysis.architecture;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.Property;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.errorreporting.AnalysisErrorReporterManager;
import org.osate.aadl2.modelsupport.modeltraversal.AadlProcessingSwitchWithProgress;
import org.osate.contribution.sei.names.SEI;
import org.osate.xtext.aadl2.properties.util.GetProperties;
import org.osate.xtext.aadl2.properties.util.PropertyUtils;

/**
 * @author phf
 */
public /* final */ class PropertyTotals extends AadlProcessingSwitchWithProgress {
	/* Counters to keep track of occurrences of different
	 * objects in the model.
	 */
	private double WeightSum = 0.0;
	private String ResultMsg = "Weight Analysis";
	
	public PropertyTotals(final IProgressMonitor monitor, AnalysisErrorReporterManager err) {
		super(monitor, PROCESS_PRE_ORDER_ALL,err);
	}
	
	protected final void initSwitches() {
	    /* We overwrite the case method for a class in the meta model
	     * specific switches.
         */
		
	}
	
	public final double calcWeight(ComponentInstance ci){
		return doCalcWeight(ci, true,"");
	}
	
	private double doCalcWeight(ComponentInstance ci, boolean needWeight, String indent){
		EList cil = ci.getComponentInstances();
		double net = GetProperties.getNetWeight(ci, 0.0);
		double weight = 0.0;
		double gross = GetProperties.getGrossWeight(ci, 0.0);
		double sublimit = 0.0;
		for(Iterator it = cil.iterator(); it.hasNext();){
			ComponentInstance subi = (ComponentInstance)it.next();
			weight += doCalcWeight(subi,(needWeight&&(gross == 0.0||net > 0.0)),indent+" ");
				sublimit += GetProperties.getWeightLimit(subi,  0.0);
		}
		if (weight == 0.0 && cil.isEmpty()){
			if (gross == 0 && net > 0){
				weight = net;
			} else {
				weight = gross;
			}
		} else {
			weight += net;
		}
		if (gross > 0.0){
			if (weight > gross){
				// problem
				warning(ci,indent+String.format(getPrintName(ci)+": Sum of weights %.3f kg exceeds grossweight %.3f kg",weight,gross));
				// Set gross weight
			} else if (weight > 0 && weight < gross){
				// problem
				warning(ci,indent+String.format(getPrintName(ci)+": Sum of weights %.3f kg less than grossweight %.3f kg (using gross weight)",weight,gross));
				weight = gross;
			}
			if (weight == 0.0){
				weight = gross;
			}
		}
		double limit = GetProperties.getWeightLimit(ci,  0.0);
		if (limit > 0.0){
			if( weight > limit){
				// problem
				ResultMsg = indent+String.format(getPrintName(ci)+": Sum of weights %.3f kg exceeds weight limit %.3f kg",weight,limit);
				error(ci,ResultMsg);
			} else {
				if (sublimit > limit){
					// problem
					warning(ci,indent+String.format(getPrintName(ci)+": Sum of subcomponent weight limits %.3f kg exceeds weight limit %.3f kg",sublimit,limit));
				}
				if ( weight < limit){
					ResultMsg = indent+String.format(getPrintName(ci)+": Sum of weights %.3f kg below weight limit %.3f kg (%.1f %% Weight slack)",
							weight, limit,(limit-weight)/limit*100);
					info(ci,ResultMsg);
				}
			}
		} else {
			if (weight > 0.0){
				ResultMsg = indent+String.format(String.format(getPrintName(ci)+": Sum of weights / Gross weight %.3f kg (no limit specified", weight));
				info(ci,ResultMsg);
			} else if (needWeight){
				ResultMsg = indent+getPrintName(ci)+": no weight total or grossweight";
				warning(ci,ResultMsg);
			}
		}
		return weight;
	}
	
	public String getPrintName(ComponentInstance ci){
		String res = null;
		if (ci instanceof SystemInstance){
			res = ci.getComponentClassifier().getName();
		} else {
			if (ci.getComponentClassifier() != null){
			res = ci.getName()+": "+ci.getComponentClassifier().getName();
			} else {
				res = ci.getName();
			}
		}
		return res;
	}

	public String getModelResult() {
		return ResultMsg;
	}
}
