/**
 *  The ranked Boolean retrieval model has no parameters.
 *
 *  Copyright (c) 2014, Danni Wu.  All Rights Reserved.
 */

public class RetrievalModelRankedBoolean extends RetrievalModel {

  /**
   * Set a retrieval model parameter.
   * @param parameterName
   * @param parametervalue
   * @return Always false because this retrieval model has no parameters.
   */
  public boolean setParameter (String parameterName, double value) {
    System.err.println ("Error: Unknown parameter name for retrieval model " +
			"RankedBoolean: " +
			parameterName);
    return false;
  }

  /**
   * Set a retrieval model parameter.
   * @param parameterName
   * @param parametervalue
   * @return Always false because this retrieval model has no parameters.
   */
  public boolean setParameter (String parameterName, String value) {
    System.err.println ("Error: Unknown parameter name for retrieval model " +
			"RankedBoolean: " +
			parameterName);
    return false;
  }

}
