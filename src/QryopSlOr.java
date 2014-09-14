/**
 *  This class implements the OR operator for all retrieval models.
 *
 *  Copyright (c) 2014, Danni Wu.  All Rights Reserved.
 */

import java.io.*;
import java.util.*;

public class QryopSlOr extends QryopSl {

	/**
	 *  It is convenient for the constructor to accept a variable number
	 *  of arguments. Thus new qryopOr (arg1, arg2, arg3, ...).
	 *  @param q A query argument (a query operator).
	 */
	public QryopSlOr(Qryop... q) {
		for (int i = 0; i < q.length; i++)
			this.args.add(q[i]);
	}

	/**
	 *  Appends an argument to the list of query operator arguments.  This
	 *  simplifies the design of some query parsing architectures.
	 *  @param {q} q The query argument (query operator) to append.
	 *  @return void
	 *  @throws IOException
	 */
	public void add (Qryop a) {
		this.args.add(a);
	}

	/**
	 *  Evaluates the query operator, including any child operators and
	 *  returns the result.
	 *  @param r A retrieval model that controls how the operator behaves.
	 *  @return The result of evaluating the query.
	 *  @throws IOException
	 */
	public QryResult evaluate(RetrievalModel r) throws IOException {

		//TODO: ranked?

		if (r instanceof RetrievalModelUnrankedBoolean || r instanceof RetrievalModelRankedBoolean)
			return (evaluateBoolean (r));

		return null;
	}

	/**
	 *  Evaluates the query operator for boolean retrieval models,
	 *  including any child operators and returns the result.
	 *  @param r A retrieval model that controls how the operator behaves.
	 *  @return The result of evaluating the query.
	 *  @throws IOException
	 */
	public QryResult evaluateBoolean (RetrievalModel r) throws IOException {

		//  Initialization

		allocDaaTPtrs (r);
		QryResult result = new QryResult ();

		while (this.daatPtrs.size() > 0) {
			int currentId = Integer.MAX_VALUE;

			// Find the minimum doc id.
			for (int i = 0; i < this.daatPtrs.size(); i++) { 
				DaaTPtr currentPtr = this.daatPtrs.get(i);
				if (currentPtr.nextDoc >= currentPtr.scoreList.scores.size()) { // No more docs in this scoreList
					this.daatPtrs.remove(i);
					i--;
					continue;
				}

				if (currentPtr.scoreList.getDocid(currentPtr.nextDoc) < currentId) {
					currentId = currentPtr.scoreList.getDocid(currentPtr.nextDoc);
				} 	
			}

			double docScore = -1.0; 
			// Examine each list that contains currentId to compute currentScore. Store the result. Advance each pointer that points to the currentId.
			for (int i = 0; i < this.daatPtrs.size(); i++) { 
				DaaTPtr currentPtr = this.daatPtrs.get(i);
				if (currentId == currentPtr.scoreList.getDocid(currentPtr.nextDoc)) {
					if (currentPtr.scoreList.getDocidScore(currentPtr.nextDoc) > docScore) { // Compute currentScore
						docScore = currentPtr.scoreList.getDocidScore(currentPtr.nextDoc);
					}
					currentPtr.nextDoc ++;
				}
			}

			if (currentId < 552682) {
				result.docScores.add (currentId, docScore);
			}
		}

		freeDaaTPtrs ();

		return result;
	}

	/*
	 *  Calculate the default score for the specified document if it
	 *  does not match the query operator.  This score is 0 for many
	 *  retrieval models, but not all retrieval models.
	 *  @param r A retrieval model that controls how the operator behaves.
	 *  @param docid The internal id of the document that needs a default score.
	 *  @return The default score.
	 */
	public double getDefaultScore (RetrievalModel r, long docid) throws IOException {

		if (r instanceof RetrievalModelUnrankedBoolean)
			return (0.0);

		return 0.0;
	}

	/*
	 *  Return a string version of this query operator.  
	 *  @return The string version of this query operator.
	 */
	public String toString(){

		String result = new String ();

		for (int i=0; i<this.args.size(); i++)
			result += this.args.get(i).toString() + " ";

		return ("#OR( " + result + ")");
	}
}
