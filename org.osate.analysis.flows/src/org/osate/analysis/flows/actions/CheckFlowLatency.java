/*
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
* %W%
* @version %I% %H%
*/
package org.osate.analysis.flows.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.osate.aadl2.Element;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instance.SystemOperationMode;
import org.osate.aadl2.modelsupport.WriteToFile;
import org.osate.aadl2.modelsupport.errorreporting.AnalysisErrorReporterManager;
import org.osate.aadl2.util.Aadl2Util;
import org.osate.analysis.flows.FlowLatencyAnalysisSwitch;
import org.osate.analysis.flows.FlowanalysisPlugin;
import org.osate.ui.actions.AbstractInstanceOrDeclarativeModelModifyActionAction;
import org.osate.ui.dialogs.Dialog;
import org.osgi.framework.Bundle;


public final class CheckFlowLatency extends AbstractInstanceOrDeclarativeModelModifyActionAction {
	
	
	private WriteToFile csvlog ;

	protected void initPropertyReferences() {
	}
	
	protected Bundle getBundle() {
		return FlowanalysisPlugin.getDefault().getBundle();
	}
	
	protected String getActionName() {
		return "Check flow latency";
	}
	
	protected String getMarkerType() {
		return "org.osate.analysis.flows.FlowLatencyObjectMarker";
	}

	@Override
	protected void analyzeDeclarativeModel(IProgressMonitor monitor, AnalysisErrorReporterManager errManager, Element declarativeObject) {
			Dialog.showError("Flow Latency Error",
				"Please select an instance model");
	}

	@Override
	protected boolean initializeAction(NamedElement obj) {
	    	csvlog = new WriteToFile("FlowLatency", obj);
			return true;
	}

	@Override
	protected boolean finalizeAnalysis() {
			csvlog.saveToFile();
			return true;
		}

	@Override
	protected void analyzeInstanceModel(IProgressMonitor monitor, AnalysisErrorReporterManager errManager, SystemInstance root, SystemOperationMode som) {
		monitor.beginTask(getActionName(), 1);
		String header = Aadl2Util.getPrintableSOMName(som);
		if (!header.isEmpty())
			csvlog.addOutputNewline(header);
		header = "flow,model element,name,deadline or conn delay,sampling delay,partition delay,flow spec,additional, total, expected\n";
		csvlog.addOutputNewline(header);
		final FlowLatencyAnalysisSwitch flowLatencySwitch =
			new FlowLatencyAnalysisSwitch( monitor, errManager,csvlog);
		flowLatencySwitch.processPreOrderComponentInstance(root);
		// XXX: disabled async analysis until we complete handling processor bindings
		flowLatencySwitch.setIsAsynchronous();
		flowLatencySwitch.processPreOrderComponentInstance(root);
		csvlog.addOutputNewline("");
		csvlog.addOutputNewline("");
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		monitor.done();		
	}

	
	public void saveCSVContent(){
		csvlog.saveToFile();
	}
	
	public void initCSVReport(NamedElement root){
    	csvlog = new WriteToFile("FlowLatency", root);
		String header = "flow,model element,name,deadline or conn delay,sampling delay,partition delay,flow spec,additional, total, expected\n\r";
		csvlog.addOutputNewline(header);
	}



}