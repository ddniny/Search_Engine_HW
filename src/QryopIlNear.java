/**
 *  This class implements the NEAR operator for all retrieval models.
 *  The #NEAR/n operator is used to match names and phrases.
 *  n specifies the maximum distance between adjacent terms.
 *
 *  Copyright (c) 2014, Danni Wu.  All Rights Reserved.
 */

import java.io.*;
import java.util.*;

public class QryopIlNear extends QryopIl {

	private int distance;

	/**
	 *  It is convenient for the constructor to accept a variable number
	 *  of arguments. Thus new QryopIlNear (n, arg1, arg2, arg3, ...).
	 */
	public QryopIlNear(int n, Qryop... q) {
		distance = n;
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

		//  Initialization

		allocDaaTPtrs (r);
		syntaxCheckArgResults (this.daatPtrs);

		QryResult qryResult = new QryResult ();		
		qryResult.invertedList = this.daatPtrs.get(0).invList;

		/*
		 * For each pass, compare neighbor two words. 
		 * If satisfy the NEAR requirement, record the position of the later word and go to next round.
		 */
		
		for (int i = 1; i < args.size(); i++) {
			QryResult iResult = new QryResult ();
			iResult.invertedList = this.daatPtrs.get(i).invList;

			int qCurDoc = 0;
			int iCurDoc = 0;
			QryResult tempResult = new QryResult();
			
			// Go through inverted lists of both terms to see if there is a match of documents
			while (qCurDoc < qryResult.invertedList.df && iCurDoc < iResult.invertedList.df) {
				int qCurDocId = qryResult.invertedList.postings.get(qCurDoc).docid;
				int iCurDocId = iResult.invertedList.postings.get(iCurDoc).docid;
				if (qCurDocId < iCurDocId) {
					qCurDoc++;
				} else if (qCurDocId > iCurDocId) {
					iCurDoc++;
				} else { // same document matched
					int qCurPos = 0;
					int iCurPos = 0;

					InvList.DocPosting qPosting = qryResult.invertedList.postings.get(qCurDoc);
					InvList.DocPosting iPosting = iResult.invertedList.postings.get(iCurDoc);
					InvList tmpInv = new InvList();
					InvList.DocPosting tmpPosting = tmpInv.new DocPosting(qPosting.docid);

					// Go through position vector of both vectors to see if there are positions 
					// Satisfy the neighbor requirement 
					while (qCurPos < qPosting.tf && iCurPos < iPosting.tf) {
						if (qPosting.positions.get(qCurPos) + this.distance < iPosting.positions.get(iCurPos)) {
							qCurPos++;
						} else if (qPosting.positions.get(qCurPos) > iPosting.positions.get(iCurPos)) {
							iCurPos++;
						} else { // position matched
							tmpPosting.positions.add(iPosting.positions.get(iCurPos));
							tmpPosting.tf++;
							iCurPos++;
							qCurPos++;
						}
					}
					
					// if theres is a match in both doc and position
					if (tmpPosting.tf != 0) {
						tempResult.invertedList.appendPosting(tmpPosting.docid, tmpPosting.positions);
					}

					iCurDoc++;
					qCurDoc++;
				}
			}

			qryResult = tempResult;
			qryResult.invertedList.field = this.daatPtrs.get(i).invList.field;

		}

		freeDaaTPtrs();

		return qryResult;
	}

	/**
	 *  syntaxCheckArgResults does syntax checking that can only be done
	 *  after query arguments are evaluated.
	 *  @param ptrs A list of DaaTPtrs for this query operator.
	 *  @return True if the syntax is valid, false otherwise.
	 */
	public Boolean syntaxCheckArgResults (List<DaaTPtr> ptrs) {

		for (int i=0; i<this.args.size(); i++) {

			if (! (this.args.get(i) instanceof QryopIl)) 
				QryEval.fatalError ("Error:  Invalid argument in " +
						this.toString());
			else
				if ((i>0) &&
						(! ptrs.get(i).invList.field.equals (ptrs.get(0).invList.field)))
					QryEval.fatalError ("Error:  Arguments must be in the same field:  " +
							this.toString());
		}

		return true;
	}

	/*
	 *  Return a string version of this query operator.  
	 *  @return The string version of this query operator.
	 */
	public String toString(){

		String result = new String ();

		for (Iterator<Qryop> i = this.args.iterator(); i.hasNext(); )
			result += (i.next().toString() + " ");

		return ("#NEAR/" + String.valueOf(this.distance) + "( " + result + ")");
	}
}
