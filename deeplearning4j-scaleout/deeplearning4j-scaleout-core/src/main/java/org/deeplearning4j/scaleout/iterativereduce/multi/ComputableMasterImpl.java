package org.deeplearning4j.scaleout.iterativereduce.multi;

import java.io.DataOutputStream;

import org.deeplearning4j.scaleout.iterativereduce.ComputableMaster;




/**
 * Master results for a distributed computation for {@link org.jblas.DoubleMatrix}
 * @author Adam Gibson
 *
 */
public abstract class ComputableMasterImpl implements ComputableMaster<UpdateableImpl>{

	protected UpdateableImpl masterResults;
	
	@Override
	public void complete(DataOutputStream ds) {
		masterResults.get().write(ds);
	}

	@Override
	public UpdateableImpl getResults() {
		return masterResults;
	}

	

}
